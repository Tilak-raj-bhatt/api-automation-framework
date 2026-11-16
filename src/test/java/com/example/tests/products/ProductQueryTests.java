package com.example.tests.products;

import com.example.framework.assertions.ApiAssert;
import com.example.framework.validation.SchemaValidator;
import com.example.tests.base.BaseTest;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;

/** Read-only product endpoints: single resource, pagination, search, categories. */
public class ProductQueryTests extends BaseTest {

    @DataProvider(name = "productIds")
    public Object[][] productIds() {
        return new Object[][] {
                {1},
                {2},
                {3},
                {10},
                {50}
        };
    }

    @Test(dataProvider = "productIds", groups = "regression",
            description = "GET /products/{id} returns the product that was requested")
    public void productByIdMatchesRequestedId(int id) {
        Response response = productsApi.getById(id);

        ApiAssert.status(response, 200);
        SchemaValidator.validate(response, "schemas/product-schema.json");

        response.then()
                .body("id", equalTo(id))
                .body("title", not(blankOrNullString()))
                .body("price", greaterThan(0f));
    }

    @Test(groups = "regression",
            description = "Pagination parameters are echoed and cap the page size")
    public void paginationIsRespected() {
        Response response = productsApi.list(5, 10);

        ApiAssert.status(response, 200);
        response.then()
                .body("limit", equalTo(5))
                .body("skip", equalTo(10))
                .body("total", greaterThan(0))
                .body("products.size()", lessThanOrEqualTo(5));
    }

    @Test(groups = "regression",
            description = "Search returns at least one product for a common term")
    public void searchReturnsMatches() {
        Response response = productsApi.search("phone");

        ApiAssert.status(response, 200);
        SchemaValidator.validate(response, "schemas/product-list-schema.json");

        response.then()
                .body("products.size()", greaterThan(0))
                .body("total", greaterThan(0));
    }

    @Test(groups = "regression",
            description = "Field selection returns only the requested fields")
    public void fieldSelectionReturnsOnlyRequestedFields() {
        Response response = productsApi.listFields(2, "title", "price");

        ApiAssert.status(response, 200);
        response.then()
                .body("products.size()", equalTo(2))
                .body("products[0].title", not(blankOrNullString()))
                .body("products[0]", not(hasKey("description")));
    }

    @Test(groups = "regression",
            description = "Category list is available and non-empty")
    public void categoriesAreAvailable() {
        Response response = productsApi.categories();

        ApiAssert.status(response, 200);
        response.then()
                .body("size()", greaterThan(0))
                .body("[0].slug", not(blankOrNullString()))
                .body("[0].name", not(blankOrNullString()));
    }

    @Test(groups = "regression",
            description = "An unknown product id returns 404 with a descriptive error")
    public void unknownProductReturnsNotFound() {
        Response response = productsApi.getById(999_999);

        ApiAssert.status(response, 404);
        SchemaValidator.validate(response, "schemas/error-schema.json");

        response.then()
                .body("message", containsString("not found"));
    }
}
