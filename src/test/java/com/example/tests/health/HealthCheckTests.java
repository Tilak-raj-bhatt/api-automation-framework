package com.example.tests.health;

import com.example.framework.assertions.ApiAssert;
import com.example.framework.specs.RequestSpecs;
import com.example.tests.base.BaseTest;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * The first test to run in any pipeline.
 *
 * <p>A health check answers "is the environment reachable?" before dozens of real
 * tests fail for the same reason. When this fails, the correct response is to stop
 * and fix infrastructure, not to investigate the failures that follow.
 */
public class HealthCheckTests extends BaseTest {

    @Test(groups = {"smoke", "regression"},
            description = "Health endpoint reports that the API is available")
    public void apiIsHealthy() {
        Response response = given()
                .spec(RequestSpecs.base())
                .when()
                .get("/test");

        ApiAssert.status(response, 200);
        response.then()
                .body("status", equalTo("ok"))
                .body("method", equalTo("GET"));
    }
}
