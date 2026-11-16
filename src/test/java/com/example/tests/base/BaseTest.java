package com.example.tests.base;

import com.example.framework.api.AuthApi;
import com.example.framework.api.ProductsApi;
import com.example.framework.auth.AuthToken;
import com.example.framework.config.ConfigKeys;
import com.example.framework.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;

/**
 * Parent of every test class.
 *
 * <p>Provides the API clients so subclasses never construct them, and exposes the
 * access token through a cached provider. Configuration is intentionally not
 * re-read here: {@link com.example.framework.config.ConfigManager} is a singleton
 * and is ready as soon as the first class touches it.
 */
public abstract class BaseTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final ProductsApi productsApi = new ProductsApi();
    protected final AuthApi authApi = new AuthApi();

    @BeforeSuite(alwaysRun = true, description = "Confirm configuration before the suite starts")
    public void verifyEnvironment() {
        log.info("Test suite starting against {} (env={})",
                ConfigManager.get(ConfigKeys.BASE_URL),
                ConfigManager.environment());
    }

    /** Returns a valid access token, fetching it once per JVM. */
    protected String accessToken() {
        return AuthToken.get();
    }
}
