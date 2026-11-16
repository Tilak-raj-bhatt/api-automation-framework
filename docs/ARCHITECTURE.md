# Architecture

How a single test travels through the framework, and why each layer exists.

## Layer map

```
                        ┌───────────────────────────┐
                        │        TEST CLASSES       │  src/test/java/com/example/tests
                        │  intent + assertions      │
                        └─────────────┬─────────────┘
                                      │
              ┌───────────────────────┼────────────────────────┐
              ▼                       ▼                        ▼
     ┌────────────────┐     ┌──────────────────┐     ┌──────────────────┐
     │   API CLIENTS  │     │      SPECS       │     │   ASSERTIONS     │
     │ ProductsApi    │     │ RequestSpecs     │     │ ApiAssert        │
     │ AuthApi        │     │ ResponseSpecs    │     │ SoftAssertions   │
     └────────┬───────┘     └────────┬─────────┘     └──────────────────┘
              │                      │
              └──────────┬───────────┘
                         ▼
        ┌────────────────────────────────────┐
        │   CROSS-CUTTING SERVICES            │
        │  ConfigManager  AuthToken           │
        │  JsonUtils  TestDataFactory         │
        │  SchemaValidator  Listeners         │
        └────────────────────────────────────┘
```

Dependencies only point downward. Nothing below knows that tests exist.

## Request lifecycle

Take this test:

```java
Response response = productsApi.getById(1);
ApiAssert.status(response, 200);
```

1. **Test** calls `ProductsApi.getById(1)`.
2. **ProductsApi** takes the baseline spec from `RequestSpecs.base()`, adds the
   `id` path parameter, and issues `GET /products/{id}`.
3. **RequestSpecs** has already applied:
   - `base.url` from `ConfigManager` (properties → env var → system property),
   - JSON `Content-Type` and `Accept`,
   - connection and read timeouts,
   - `AllureRestAssured` so the report captures the exchange,
   - optional console logging when `log.requests=true`.
4. **REST Assured** serializes, sends, and returns a `Response`.
5. **ApiAssert** checks the status. On failure it prints the status line, content
   type, and body — not just `expected 200 but found 500`.
6. **TestListener** logs START/PASS/FAIL with duration.
7. **Allure** writes a result file to `target/allure-results`.

## Why each package exists

### `api/`
Endpoint knowledge lives in exactly one place. If the path changes from
`/products` to `/v2/products`, one constant changes and every test follows.

Methods return raw `Response`, never assertions. That keeps clients reusable for
negative tests:
`productsApi.getById(999_999)` is a legitimate call whose expected status is 404.

### `specs/`
A request specification is the "defaults" of every call: base URI, headers,
timeouts, filters, TLS settings. `ResponseSpecs` does the same for expected
status and content type.

> **Gotcha this project already handles:** `RequestSpecBuilder.build()` returns a
> raw specification with no response specification in REST Assured 5.5, which
> throws `Cannot get property 'assertionClosure' on null object` the moment you
> send. `RequestSpecs.base()` wraps it in `given().spec(...)` so the request has a
> response specification attached.

### `model/`
POJOs make tests readable and refactorable. They are deliberately lenient
(`@JsonIgnoreProperties(ignoreUnknown = true)`) so a new provider field does not
break compilation or parsing.

Strictness belongs in `schemas/`, not in POJOs.

### `config/`
`ConfigManager` is a singleton that resolves a key through four sources. The
environment name is the only thing a pipeline usually changes.

`ConfigKeys` prevents typos: `ConfigKeys.BASE_URL` is compile-checked;
`"base.url"` is not.

### `assertions/`
Assertions should explain *why* a test failed. `ApiAssert` and `SoftAssertions`
attach the response body to every failure. Hard assertion for a precondition, soft
assertions when you want one run to reveal several problems.

### `auth/`
`AuthToken` logs in once per JVM and caches the bearer token. Logging in inside
every test multiplies runtime and turns the auth service into a CI bottleneck.

For a real API, extend it to read the JWT `exp` claim and refresh before expiry.

### `validation/`
JSON Schema validation is a contract test: *does the response still have the shape
consumers depend on?* It fails loudly when a field is removed or changes type,
independently of any value assertion.

### `listeners/`
- `TestListener` writes a greppable lifecycle trail for CI logs.
- `RetryAnalyzer` / `RetryTransformer` implement opt-in retries, wired through
  configuration rather than annotations on individual tests.

## Test data strategy

- `TestDataFactory` generates unique values with a UUID suffix.
- Tests must not depend on data created by another test.
- Write tests assume a **stateless** API: assert the echoed response, not
  persistence.
- On a real API, do this instead:

```java
private int productId;

@BeforeMethod(alwaysRun = true)
public void createFixture() {
    productId = productsApi.create(TestDataFactory.newProduct())
            .then().extract().path("id");
}

@AfterMethod(alwaysRun = true)
public void removeFixture() {
    productsApi.delete(productId);
}
```

## Concurrency

`testng.xml` runs `parallel="none"` so a beginner sees deterministic output.
Because every test owns its data and no state is shared except the immutable
config and the cached token, switching to:

```xml
<suite name="API Regression Suite" parallel="methods" thread-count="5">
```

should not require any test changes. Verify before enabling it in CI.

## Extension points

| Need | Where to add |
|---|---|
| New endpoint | method on an existing `*Api`, or a new client class |
| New resource type | POJO in `model/` + schema in `schemas/` |
| New environment | `config/<name>.properties` (no code change) |
| Auth scheme change | `AuthToken` and `RequestSpecs.withToken` |
| API key instead of OAuth | a `RequestSpecs` variant that sets the header from config |
| Custom assertion | `assertions/` |
| Report enrichment | a REST Assured `Filter` registered in `RequestSpecs` |

## What this framework deliberately does NOT do

- **No page-object-style over-abstraction.** Endpoint methods map 1:1 to routes.
- **No assertions in clients.** Tests decide what is correct.
- **No shared mutable static state** beyond configuration and the token cache.
- **No hard-coded credentials in tests.** They come from configuration.
