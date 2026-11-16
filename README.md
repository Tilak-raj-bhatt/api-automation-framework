# API Automation Framework

A learning project for **REST Assured + TestNG** that is structured the way a real
enterprise API test suite is structured: layered, configuration-driven, reportable,
and ready to plug into CI/CD without rewiring.

It tests [DummyJSON](https://dummyjson.com), a free public REST API with
authentication, pagination, search, and full CRUD endpoints.

---

## What you get

| Concern | Tool | Why it is here |
|---|---|---|
| HTTP client & assertions | REST Assured 5 | Industry standard for Java API testing |
| Test runner | TestNG | Groups, data providers, parallel execution, suite XML |
| Reporting | Allure | Attaches every request/response to the test report |
| Serialization | Jackson | Typed request/response POJOs |
| Contract checks | JSON Schema | Catches breaking response changes early |
| Logging | SLF4J + Logback | Greppable CI logs |
| Test data | Java Faker | Unique, realistic values per run |
| Build | Maven Wrapper | No Maven installation required |

---

## Prerequisites

- **JDK 17 or newer** (`java -version`)
- **Git** (recommended)
- That is all. `mvnw` downloads Maven 3.9 on first run.

---

## Quick start

**Windows (PowerShell)**

```powershell
cd api-automation-framework
.\mvnw.cmd test
```

**macOS / Linux**

```bash
cd api-automation-framework
chmod +x mvnw
./mvnw test
```

Expected result: `Tests run: 21, Failures: 0, Errors: 0`.

If PowerShell blocks the script:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

---

## Project layout

```
api-automation-framework/
├── pom.xml                       # dependencies, surefire config, profiles
├── mvnw / mvnw.cmd               # Maven wrapper (no local Maven needed)
├── testng.xml                    # full regression suite
├── testng-smoke.xml              # smoke-only suite
│
├── src/main/java/com/example/framework/     # FRAMEWORK - reused by all tests
│   ├── api/           ProductsApi, AuthApi           -> endpoint knowledge
│   ├── assertions/    ApiAssert, SoftAssertions      -> readable failures
│   ├── auth/          AuthToken                       -> cached bearer token
│   ├── config/        ConfigManager, ConfigKeys       -> env-aware settings
│   ├── exception/     FrameworkException              -> framework failures
│   ├── listeners/     TestListener, Retry*            -> lifecycle logging/retries
│   ├── model/         Product, LoginResponse, ...     -> request/response POJOs
│   ├── specs/         RequestSpecs, ResponseSpecs     -> shared HTTP behaviour
│   ├── utils/         JsonUtils, TestDataFactory      -> helpers
│   └── validation/    SchemaValidator                 -> contract validation
│
├── src/test/java/com/example/tests/         # TESTS - thin, readable
│   ├── base/          BaseTest
│   ├── auth/          LoginTests
│   ├── contract/      ContractTests
│   ├── health/        HealthCheckTests
│   └── products/      ProductQueryTests, ProductCrudTests
│
├── src/test/resources/
│   ├── config/        default.properties, qa.properties, dev.properties
│   ├── schemas/       *.json contract definitions
│   └── logback-test.xml
│
├── docs/              ARCHITECTURE.md, WRITING-TESTS.md
├── .github/workflows/ api-tests.yml
├── Jenkinsfile
└── Dockerfile
```

### The one rule that keeps this maintainable

```
tests  ->  api / specs  ->  config / utils
```

Arrows only point **down**. A test may call `ProductsApi`; `ProductsApi` never
knows a test exists. `RequestSpecs` never imports a test. This is why you can add
fifty tests without touching framework code, and why changing the base URL or
timeout is a one-line edit.

---

## Running tests

```powershell
# Everything
.\mvnw.cmd test

# Smoke only (fast sanity check after a deployment)
.\mvnw.cmd -Psmoke test

# A single class
.\mvnw.cmd test -Dtest=ProductQueryTests

# A single method
.\mvnw.cmd test -Dtest=ProductQueryTests#paginationIsRespected

# Against the dev profile (verbose HTTP logging)
.\mvnw.cmd test -Denv=dev

# Force a different target at runtime, no rebuild
.\mvnw.cmd test -Dframework.base.url=https://staging.example.com

# Rebuild the Allure HTML report (requires Allure CLI, see Reporting)
allure serve target/allure-results
```

Bash users: replace `.\mvnw.cmd` with `./mvnw`.

### How the test set is chosen

`testng.xml` is the source of truth for a full run. `-Dtest=...` still works and
takes precedence, which is convenient while writing a new test.

---

## Configuration

Nothing is hard-coded. Values are resolved in this order, highest first:

| # | Source | Example |
|---|---|---|
| 1 | JVM system property | `-Dframework.base.url=https://x` |
| 2 | Environment variable | `FRAMEWORK_BASE_URL=https://x` |
| 3 | `config/<env>.properties` | `config/qa.properties` |
| 4 | `config/default.properties` | shared defaults |

The active environment is chosen with `-Denv=qa` or the `ENV` environment
variable (default `qa`). CI injects it; the artifact never changes.

**Rule:** secrets never live in a properties file that is committed. Use
environment variables in the pipeline, or a secret manager.

To point the suite at your own API, edit `base.url` (and `base.path` if you have
a version prefix such as `/api/v1`).

---

## Reporting

Every test run writes Allure results to `target/allure-results`. The
`AllureRestAssured` filter captures each request and response, so a failure in CI
can be inspected without reproducing it locally.

```bash
# One-time install (either tool works)
npm install -g allure-commandline
# or: scoop install allure

allure serve target/allure-results      # generate + open locally
allure generate target/allure-results -o allure-report --clean
```

Surefire also writes plain-text and XML reports to `target/surefire-reports`.

---

## CI/CD

### GitHub Actions

`.github/workflows/api-tests.yml` runs on push, pull request, nightly schedule,
and manual dispatch. It:

1. builds against Java 17 and 21 in parallel,
2. runs the suite against the selected environment,
3. uploads Allure results, the HTML report, and Surefire reports as artifacts,
4. fails the build when a test fails.

### Jenkins

`Jenkinsfile` is a declarative pipeline with an `ENVIRONMENT` parameter. It
publishes the TestNG results and archives the Allure output.

### Docker

```bash
docker build -t api-tests .
docker run --rm -e ENV=qa api-tests                  # full regression
docker run --rm api-tests -Psmoke                    # smoke only
docker compose up --build                            # results land in ./target
```

---

## Design decisions worth learning

**Why a wrapper around REST Assured?**
Raw `given().get(...)` scattered across tests creates duplication and inconsistent
timeouts. One `RequestSpecs.base()` means every request shares headers, timeouts,
logging, and report capture.

**Why POJOs *and* JSON Schema?**
POJOs make tests readable (`product.getPrice()`). Schemas catch contract drift
(`a required field disappeared`). They answer different questions.

**Why not retry by default?**
Retries hide defects. `retry.enabled=false` is deliberate. Turn it on only for
genuinely unstable infrastructure, and treat "passes on retry" as a bug report.

**Why no assertions inside `ProductsApi`?**
An API client that asserts cannot be reused for negative testing. Tests own the
expectations; clients own the protocol.

**Why unique test data?**
`TestDataFactory` appends a UUID. Parallel CI runs and shared environments collide
when tests hard-code values.

**Why is there no `login()` in every test?**
`AuthToken` caches the token per JVM. One login, not fifty.

**Important:** DummyJSON is read-only. `POST`, `PUT`, and `DELETE` echo a
simulated result but do not persist. The CRUD tests assert the *response contract*
and never assume the change can be read back. If your API does persist, create
fixtures in `@BeforeMethod` and delete them in `@AfterMethod`.

---

## Adding your own tests

See [`docs/WRITING-TESTS.md`](docs/WRITING-TESTS.md) for a step-by-step cookbook.
The short version:

1. Add an endpoint method to `ProductsApi` (or a new `*Api` class).
2. Add a POJO in `model/` if the response shape is new.
3. Write the test in `src/test/java/com/example/tests/...`, extending `BaseTest`.
4. Tag it `groups = {"smoke"}` or `"regression"`.
5. Run it: `.\mvnw.cmd test -Dtest=YourNewTest`.

No changes to `pom.xml`, listeners, or configuration are needed.

---

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `mvnw : cannot be loaded because running scripts is disabled` | `Set-ExecutionPolicy -Scope Process Bypass` |
| `Connect timed out` | Increase `timeout.socket.ms`, or check outbound network access |
| `Missing required configuration key` | Add the key to `config/default.properties` or pass `-Dframework.<key>=...` |
| Schema test fails but values look fine | The provider changed a required field – that is the contract test doing its job |
| Login test fails | DummyJSON demo credentials may have changed; update `auth.username` / `auth.password` |
| `401` on every authenticated test | Token expired or credentials invalid; check `AuthToken` / config |

---

## Suggested learning path

**Week 1 – Read and run.** Run the suite. Read `HealthCheckTests`, then
`ProductQueryTests`, then trace one call: test → `ProductsApi` → `RequestSpecs` →
`ConfigManager`. Break an assertion on purpose and read the failure message.

**Week 2 – Add tests.** Add `GET /products/category/{category}` coverage. Add a
negative test for `POST /products/add` with a missing field. Add a data provider
for categories. Write a schema for the category response.

**Week 3 – Harden.** Turn on `-Denv=dev` and read raw HTTP. Add a test that
asserts response time. Add a negative auth test. Practise soft assertions.

**Week 4 – Pipeline.** Push to GitHub and watch Actions run. Add a
`@BeforeMethod` that creates data and an `@AfterMethod` that removes it. Enable
parallel execution in `testng.xml` (`parallel="methods" thread-count="5"`) and
prove the tests are independent.

---

## Reference

- REST Assured: <https://rest-assured.io/>
- TestNG: <https://testng.org/doc/>
- Allure: <https://allurereport.org/docs/>
- DummyJSON: <https://dummyjson.com/docs>
- JSON Schema: <https://json-schema.org/>
