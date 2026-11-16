package com.example.framework.validation;

import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Contract validation against JSON Schema files in {@code src/test/resources/schemas}.
 *
 * <p>Schema validation answers a different question than a field assertion:
 * <em>does the response still have the shape consumers depend on?</em> A single
 * change to a required field fails every schema test at once, which is exactly
 * the early warning a contract test should give.
 */
public final class SchemaValidator {

    private SchemaValidator() {
    }

    public static void validate(Response response, String classpathSchema) {
        response.then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath(classpathSchema));
    }
}
