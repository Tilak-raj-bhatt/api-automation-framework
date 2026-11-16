package com.example.framework.specs;

import com.example.framework.config.ConfigKeys;
import com.example.framework.config.ConfigManager;
import com.example.framework.exception.FrameworkException;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Builds the baseline request specification shared by every API call.
 *
 * <p>Keeping this in one place means timeouts, headers, logging, and reporting
 * behaviour are changed once and apply everywhere. Tests never repeat boilerplate
 * such as {@code given().contentType("application/json")}.
 */
public final class RequestSpecs {

    private RequestSpecs() {
    }

    /** Baseline JSON request with timeout, Allure capture, and optional console logging. */
    public static RequestSpecification base() {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(ConfigManager.get(ConfigKeys.BASE_URL))
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setConfig(clientConfig())
                // Captures every request/response pair in the Allure report.
                .addFilter(new AllureRestAssured());

        String basePath = ConfigManager.get(ConfigKeys.BASE_PATH, "");
        if (!basePath.isBlank()) {
            builder.setBasePath(basePath);
        }

        if (ConfigManager.getBoolean(ConfigKeys.RELAXED_HTTPS)) {
            builder.setRelaxedHTTPSValidation();
        }

        if (ConfigManager.getBoolean(ConfigKeys.LOG_REQUESTS)) {
            builder.addFilter(new RequestLoggingFilter(LogDetail.ALL));
            builder.addFilter(new ResponseLoggingFilter(LogDetail.ALL));
        }

        // Wrap the built spec in given(): REST Assured's builder returns a raw
        // specification without a response specification, which would NPE on send.
        return given().spec(builder.build());
    }

    /** Baseline request plus a Bearer token. Use for authenticated endpoints. */
    public static RequestSpecification withToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new FrameworkException("An access token is required for this request");
        }
        return base().header("Authorization", "Bearer " + accessToken);
    }

    private static RestAssuredConfig clientConfig() {
        int connectionTimeout = ConfigManager.getInt(ConfigKeys.CONNECTION_TIMEOUT_MS);
        int socketTimeout = ConfigManager.getInt(ConfigKeys.SOCKET_TIMEOUT_MS);

        // String keys keep this independent of the deprecated HttpClient constant classes.
        return RestAssured.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", connectionTimeout)
                        .setParam("http.socket.timeout", socketTimeout));
    }
}
