package com.example.tests.products;

import com.example.framework.assertions.ApiAssert;
import com.example.framework.model.ProductRequest;
import com.example.framework.utils.TestDataFactory;
import com.example.tests.base.BaseTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Write endpoints.
 *
 * <p><strong>Important:</strong> DummyJSON is a read-only demo API. It echoes the
 * payload you send and simulates the effect, but it does not persist changes. These
 * tests therefore assert the <em>response contract</em> and never assume that a
 * created product can be read back afterwards.
 *
 * <p>That is a deliberate lesson. Tests that depend on data created by an earlier
 * test are order-dependent and fail in parallel CI runs. If your API does persist
 * data, create it in {@code @BeforeMethod} and clean it up in {@code @AfterMethod}
 * so each test owns its fixtures.
 */
public class ProductCrudTests extends BaseTest {

    @Test(groups = "smoke", description = "POST /products/add echoes the created resource")
    public void createProductEchoesPayload() {
        ProductRequest payload = TestDataFactory.newProduct();

        Response response = productsApi.create(payload);

        ApiAssert.status(response, 201);
        response.then()
                .body("title", equalTo(payload.getTitle()))
                .body("category", equalTo(payload.getCategory()))
                .body("id", notNullValue());

        float echoedPrice = response.jsonPath().getFloat("price");
        Assert.assertEquals(echoedPrice, payload.getPrice().floatValue(), 0.01f);
    }

    @Test(groups = "regression", description = "PUT /products/{id} applies the submitted change")
    public void updateProductAppliesChanges() {
        String newTitle = "Updated by automation " + TestDataFactory.uniqueSuffix();

        Response response = productsApi.update(1, ProductRequest.withTitle(newTitle));

        ApiAssert.status(response, 200);
        response.then()
                .body("id", equalTo(1))
                .body("title", equalTo(newTitle));
    }

    @Test(groups = "regression", description = "DELETE /products/{id} marks the resource as deleted")
    public void deleteProductFlagsResourceAsDeleted() {
        Response response = productsApi.delete(1);

        ApiAssert.status(response, 200);
        response.then()
                .body("id", equalTo(1))
                .body("isDeleted", equalTo(true))
                .body("deletedOn", not(blankOrNullString()));
    }

    @Test(groups = "regression", description = "A newly created product has a positive identifier")
    public void createdProductReceivesIdentifier() {
        Response response = productsApi.create(TestDataFactory.newProduct());

        ApiAssert.status(response, 201);
        response.then()
                .body("id", greaterThan(0));
    }
}
