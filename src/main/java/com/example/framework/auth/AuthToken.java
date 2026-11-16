package com.example.framework.auth;

import com.example.framework.api.AuthApi;
import com.example.framework.config.ConfigKeys;
import com.example.framework.config.ConfigManager;
import com.example.framework.exception.FrameworkException;
import com.example.framework.model.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lazy, thread-safe access-token cache.
 *
 * <p>Logging in before every test would be slow and would make the auth service a
 * bottleneck in CI. The token is fetched once per JVM and reused. {@link #reset()}
 * exists so a test can deliberately start from a clean state.
 *
 * <p>For a real product you would also honour token expiry: compare the JWT
 * {@code exp} claim with the current time, and refresh shortly before it lapses.
 * DummyJSON tokens last 60 minutes, which is plenty for a test run.
 */
public final class AuthToken {

    private static final Logger LOG = LoggerFactory.getLogger(AuthToken.class);

    private static volatile String cachedToken;

    private AuthToken() {
    }

    public static String get() {
        String token = cachedToken;
        if (token != null) {
            return token;
        }
        synchronized (AuthToken.class) {
            if (cachedToken == null) {
                cachedToken = fetchToken();
            }
            return cachedToken;
        }
    }

    public static void reset() {
        cachedToken = null;
    }

    private static String fetchToken() {
        String username = ConfigManager.get(ConfigKeys.AUTH_USERNAME);
        String password = ConfigManager.get(ConfigKeys.AUTH_PASSWORD);

        LOG.info("Requesting access token for user '{}'", username);
        LoginResponse response = new AuthApi().loginAs(username, password);

        String token = response.getAccessToken();
        if (token == null || token.isBlank()) {
            throw new FrameworkException("Login for user '" + username + "' returned no accessToken");
        }
        return token;
    }
}
