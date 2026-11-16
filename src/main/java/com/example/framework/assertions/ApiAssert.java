package com.example.framework.assertions;

import io.restassured.response.Response;
import org.testng.Assert;

/**
 * Assertion helpers that produce a useful failure message.
 *
 * <p>Out of the box, {@code assertEquals(response.statusCode(), 200)} tells you the
 * numbers but not the URL or the body. These helpers attach the method, URI, and
 * response body to the failure so a CI log is often enough to diagnose the issue.
 */
public final class ApiAssert {

    private ApiAssert() {
    }

    public static void status(Response response, int expectedStatus) {
        Assert.assertEquals(response.statusCode(), expectedStatus,
                describe(response, "Unexpected HTTP status"));
    }

    public static void statusIsSuccessful(Response response) {
        int actual = response.statusCode();
        Assert.assertTrue(actual >= 200 && actual < 300,
                describe(response, "Expected a 2xx status but received " + actual));
    }

    public static void headerContains(Response response, String headerName, String expectedFragment) {
        String headerValue = response.header(headerName);
        Assert.assertNotNull(headerValue,
                describe(response, "Expected header '" + headerName + "' to be present"));
        Assert.assertTrue(headerValue.contains(expectedFragment),
                describe(response, "Expected header '" + headerName + "' to contain '" + expectedFragment
                        + "' but was '" + headerValue + "'"));
    }

    /** Builds a diagnostic message that is safe to call even when the body is empty or binary. */
    public static String describe(Response response, String label) {
        return String.format("%s%n--> %s%nContent-Type: %s%nResponse body:%n%s",
                label,
                response.getStatusLine(),
                response.getContentType(),
                safeBody(response));
    }

    private static String safeBody(Response response) {
        try {
            String body = response.asString();
            return body == null || body.isBlank() ? "<empty>" : body;
        } catch (RuntimeException e) {
            return "<unreadable body: " + e.getMessage() + ">";
        }
    }
}
