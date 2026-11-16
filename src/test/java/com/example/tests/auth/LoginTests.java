package com.example.tests.auth;

import com.example.framework.assertions.ApiAssert;
import com.example.framework.assertions.SoftAssertions;
import com.example.framework.config.ConfigKeys;
import com.example.framework.config.ConfigManager;
import com.example.framework.validation.SchemaValidator;
import com.example.tests.base.BaseTest;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Authentication contract: the token issued here is reused by every
 * authenticated test through {@link com.example.framework.auth.AuthToken}.
 */
public class LoginTests extends BaseTest {

    @Test(groups = {"smoke", "regression"},
            description = "Valid credentials return a JWT access token")
    public void validCredentialsReturnAccessToken() {
        String username = ConfigManager.get(ConfigKeys.AUTH_USERNAME);
        String password = ConfigManager.get(ConfigKeys.AUTH_PASSWORD);

        Response response = authApi.login(username, password);

        ApiAssert.status(response, 200);
        SchemaValidator.validate(response, "schemas/login-schema.json");

        response.then()
                .body("username", equalTo(username))
                .body("accessToken", not(blankOrNullString()))
                .body("email", containsString("@"))
                .body("id", notNullValue());
    }

    @Test(groups = "regression",
            description = "Invalid credentials are rejected with 400 and a clear message")
    public void invalidCredentialsAreRejected() {
        Response response = authApi.login(
                ConfigManager.get(ConfigKeys.AUTH_USERNAME),
                "definitely-not-the-right-password");

        ApiAssert.status(response, 400);
        SchemaValidator.validate(response, "schemas/error-schema.json");

        response.then()
                .body("message", equalTo("Invalid credentials"));
    }

    @Test(groups = {"smoke", "regression"},
            description = "A valid token grants access to the authenticated profile")
    public void tokenGrantsAccessToProfile() {
        Response response = authApi.me(accessToken());

        ApiAssert.status(response, 200);
        response.then()
                .body("username", equalTo(ConfigManager.get(ConfigKeys.AUTH_USERNAME)))
                .body("email", containsString("@"))
                .body("id", notNullValue());
    }

    @Test(groups = "regression",
            description = "Soft assertions report several profile problems in a single run")
    public void profileHasExpectedShape() {
        Response response = authApi.me(accessToken());

        // Each check is recorded; assertAll() throws once with every failure listed.
        SoftAssertions softly = new SoftAssertions();
        softly.status(response, 200);
        softly.body(response, "id", notNullValue());
        softly.body(response, "email", containsString("@"));
        softly.body(response, "username", not(blankOrNullString()));
        softly.assertAll();
    }
}
