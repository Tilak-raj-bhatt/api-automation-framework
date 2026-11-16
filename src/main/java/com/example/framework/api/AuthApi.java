package com.example.framework.api;

import com.example.framework.model.LoginRequest;
import com.example.framework.model.LoginResponse;
import com.example.framework.specs.RequestSpecs;
import io.restassured.response.Response;

/** Reusable client for authentication endpoints. */
public class AuthApi {

    private static final String LOGIN = "/auth/login";
    private static final String ME = "/auth/me";

    public Response login(String username, String password) {
        return login(new LoginRequest(username, password));
    }

    public Response login(LoginRequest credentials) {
        return RequestSpecs.base()
                .body(credentials)
                .when()
                .post(LOGIN);
    }

    /**
     * Convenience variant that deserializes a successful response.
     * Only use when credentials are expected to be valid - use {@link #login(String, String)}
     * for negative tests so the raw response can be asserted.
     */
    public LoginResponse loginAs(String username, String password) {
        return login(username, password).as(LoginResponse.class);
    }

    public Response me(String accessToken) {
        return RequestSpecs.withToken(accessToken)
                .when()
                .get(ME);
    }
}
