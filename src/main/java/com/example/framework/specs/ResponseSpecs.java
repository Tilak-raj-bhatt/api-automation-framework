package com.example.framework.specs;

import io.restassured.http.ContentType;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.expect;

/**
 * Reusable response expectations.
 *
 * <p>Attaching one of these with {@code .then().spec(...)} documents intent in the
 * test and keeps status/content-type checks consistent across the suite.
 */
public final class ResponseSpecs {

    private ResponseSpecs() {
    }

    public static ResponseSpecification ok() {
        return status(200);
    }

    public static ResponseSpecification created() {
        return status(201);
    }

    public static ResponseSpecification badRequest() {
        return status(400);
    }

    public static ResponseSpecification notFound() {
        return status(404);
    }

    public static ResponseSpecification noContent() {
        return expect().statusCode(204);
    }

    /** Any status code, still asserting the response is JSON. */
    public static ResponseSpecification status(int expectedStatus) {
        return expect()
                .statusCode(expectedStatus)
                .contentType(ContentType.JSON);
    }
}
