package com.example.tests.contract;

import com.example.framework.assertions.ApiAssert;
import com.example.framework.validation.SchemaValidator;
import com.example.tests.base.BaseTest;
import io.restassured.response.Response;
import org.testng.annotations.Test;

/**
 * Contract tests answer one question: <em>does the response still have the shape
 * consumers depend on?</em>
 *
 * <p>They are intentionally separate from behaviour tests. A missing field is a
 * breaking change for every client, and you want that to be obvious in the report
 * rather than buried inside a value assertion.
 */
public class ContractTests extends BaseTest {

    @Test(groups = "regression", description = "A single product conforms to the published schema")
    public void productMatchesSchema() {
        Response response = productsApi.getById(1);

        ApiAssert.status(response, 200);
        SchemaValidator.validate(response, "schemas/product-schema.json");
    }

    @Test(groups = "regression", description = "A product collection conforms to the published schema")
    public void productListMatchesSchema() {
        Response response = productsApi.list(10, 0);

        ApiAssert.status(response, 200);
        SchemaValidator.validate(response, "schemas/product-list-schema.json");
    }
}
