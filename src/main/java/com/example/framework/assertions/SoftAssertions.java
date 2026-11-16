package com.example.framework.assertions;

import io.restassured.response.Response;
import org.hamcrest.Matcher;
import org.testng.asserts.SoftAssert;

/**
 * Collects several failures before failing the test.
 *
 * <p>Use soft assertions when you want to see every problem in one run instead of
 * fixing them one at a time. Always finish with {@link #assertAll()}, otherwise the
 * failures are recorded but never reported.
 *
 * <pre>{@code
 *   SoftAssertions softly = new SoftAssertions();
 *   softly.status(response, 200);
 *   softly.body(response, "id", equalTo(1));
 *   softly.assertAll();
 * }</pre>
 */
public class SoftAssertions {

    private final SoftAssert softAssert = new SoftAssert();

    public void status(Response response, int expectedStatus) {
        softAssert.assertEquals(response.statusCode(), expectedStatus,
                ApiAssert.describe(response, "Unexpected HTTP status"));
    }

    public <T> void body(Response response, String jsonPath, Matcher<? super T> matcher) {
        T actual = response.path(jsonPath);
        softAssert.assertTrue(
                matcher.matches(actual),
                "JSON path '" + jsonPath + "' was <" + actual + ">, expected " + matcher
                        + " (response " + response.getStatusLine() + ")");
    }

    public void assertAll() {
        softAssert.assertAll();
    }
}
