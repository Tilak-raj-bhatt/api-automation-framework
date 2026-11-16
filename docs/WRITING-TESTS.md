# Writing Tests

A cookbook. Every recipe follows the same five steps:

1. endpoint method in an API client,
2. POJO if the payload shape is new,
3. test class extending `BaseTest`,
4. assertions + group tag,
5. run it.

---

## Recipe 1 – Test an existing endpoint

`ProductsApi` already has `search(String)`. The test is three lines of intent:

```java
package com.example.tests.products;

import com.example.framework.assertions.ApiAssert;
import com.example.tests.base.BaseTest;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.greaterThan;

public class ProductSearchTests extends BaseTest {

    @Test(groups = "regression",
            description = "Searching for a category keyword returns results")
    public void searchByCategoryKeywordReturnsResults() {
        Response response = productsApi.search("laptop");

        ApiAssert.status(response, 200);
        response.then().body("total", greaterThan(0));
    }
}
```

Run it:

```powershell
.\mvnw.cmd test -Dtest=ProductSearchTests
```

---

## Recipe 2 – Add a new endpoint to a client

Say you want `GET /products/category/{category}`.

```java
// src/main/java/com/example/framework/api/ProductsApi.java

private static final String PRODUCTS_BY_CATEGORY = "/products/category/{category}";

public Response byCategory(String category) {
    return RequestSpecs.base()
            .pathParam("category", category)
            .when()
            .get(PRODUCTS_BY_CATEGORY);
}
```

Then test it:

```java
@Test(groups = "regression")
public void productsBelongToRequestedCategory() {
    Response response = productsApi.byCategory("beauty");

    ApiAssert.status(response, 200);
    response.then().body("products.category", everyItem(equalTo("beauty")));
}
```

Note `everyItem`: assert on **every** element, not just the first one. This is the
single most common mistake in API assertions — `products[0].category` passes while
the rest of the page is wrong.

---

## Recipe 3 – Negative test

Never happy-path only. Errors are part of the contract.

```java
@Test(groups = "regression",
        description = "Creating a product without a title is rejected")
public void createWithoutTitleIsRejected() {
    ProductRequest invalid = ProductRequest.builder()
            .price(10.0)
            .category("automation")
            .build();                       // title deliberately missing

    Response response = productsApi.create(invalid);

    ApiAssert.status(response, 400);
    response.then().body("message", not(isEmptyOrNullString()));
}
```

Test these routinely: missing required field, wrong type, invalid enum, unknown
id (404), unauthorized (401), forbidden (403), rate limit (429), malformed JSON.

---

## Recipe 4 – Data-driven test

```java
@DataProvider(name = "categorySlugs")
public Object[][] categorySlugs() {
    return new Object[][] {
            {"beauty"},
            {"fragrances"},
            {"furniture"}
    };
}

@Test(dataProvider = "categorySlugs", groups = "regression")
public void eachCategoryReturnsProducts(String slug) {
    Response response = productsApi.byCategory(slug);
    ApiAssert.status(response, 200);
    response.then().body("products.size()", greaterThan(0));
}
```

Name the data provider after the data, not after the test.

---

## Recipe 5 – Contract test with JSON Schema

1. Save the schema in `src/test/resources/schemas/`.
2. Call it by classpath path (no leading slash).

```java
@Test(groups = "regression")
public void categoryMatchesSchema() {
    Response response = productsApi.byCategory("beauty");

    ApiAssert.status(response, 200);
    SchemaValidator.validate(response, "schemas/product-list-schema.json");
}
```

Keep schemas strict about **required** fields and permissive about extras
(`"additionalProperties": true`). A schema that breaks whenever the provider adds
a field will be disabled by the team within a month.

---

## Recipe 6 – Assert a value deeply

```java
Response response = productsApi.getById(1);

// Dot paths
response.then()
        .body("dimensions.width", greaterThan(0f))
        .body("tags", hasItem("mascara"))
        .body("images.size()", greaterThan(0));

// Extract and use Java
Product product = response.as(Product.class);
assertThat(product.getTitle(), not(isEmptyString()));
```

`response.as(Class)` is the bridge from raw JSON to a typed object. Use it when
you need real logic; use JSONPath when a single field check is enough.

---

## Recipe 7 – Authenticated endpoint

```java
Response response = authApi.me(accessToken());
ApiAssert.status(response, 200);
```

`accessToken()` logs in once per JVM and caches the token. If you need a second
identity, call `AuthApi.login(...)` with that user's credentials and pass its
token explicitly.

---

## Recipe 8 – Soft assertions

```java
SoftAssertions softly = new SoftAssertions();
softly.status(response, 200);
softly.body(response, "email", containsString("@"));
softly.body(response, "role", not(isEmptyOrNullString()));
softly.assertAll();   // required: without this, failures are silently dropped
```

Use for a *diagnostic sweep* of one response. Do not use soft assertions to mask a
precondition — if the response is not 200, the rest of the checks are noise.

---

## Recipe 9 – Measure response time

```java
@Test(groups = "regression")
public void listRespondsWithinBudget() {
    Response response = productsApi.list(10, 0);

    ApiAssert.status(response, 200);
    assertThat("Response took " + response.time() + " ms",
            response.time(), lessThan(3000L));
}
```

Performance thresholds in CI are unforgiving; keep them generous and trend them
rather than treating them as hard SLAs.

---

## Naming and structure conventions

| Thing | Convention | Example |
|---|---|---|
| Test class | `<Resource><Aspect>Tests` | `ProductQueryTests` |
| Test method | behaviour sentence, present tense | `unknownProductReturnsNotFound` |
| API method | verb implied by HTTP, noun by resource | `getById`, `search`, `create` |
| Data provider | plural of the data | `productIds` |
| Schema file | kebab-case resource | `product-list-schema.json` |
| Groups | `smoke`, `regression` | `groups = {"smoke", "regression"}` |

Each test class should answer one question: "does querying work?", "does writing
work?", "is the contract intact?"

---

## Checklist before opening a pull request

- [ ] Test fails for the right reason when the API is broken (verify by hand once).
- [ ] No hard-coded IDs or strings that belong in configuration.
- [ ] No dependency on another test's data.
- [ ] Assertions check a value, not just a status code.
- [ ] Negative case covered where an error path exists.
- [ ] Group tag present (`smoke` and/or `regression`).
- [ ] `description` explains intent in plain English.
- [ ] Runs green locally: `.\mvnw.cmd test -Dtest=YourNewTest`
