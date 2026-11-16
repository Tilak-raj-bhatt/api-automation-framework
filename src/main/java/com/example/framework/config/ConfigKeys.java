package com.example.framework.config;

/**
 * Central registry of every configuration key the framework understands.
 *
 * <p>Why constants instead of raw strings? A typo in a property name becomes a
 * compile error instead of a mysterious runtime failure, and IDEs can find every
 * usage in one click.
 *
 * <p>Every key here can be overridden in three escalating levels of precedence:
 *
 * <pre>
 *   1. JVM system property   -Dframework.base.url=https://staging.example.com
 *   2. Environment variable  FRAMEWORK_BASE_URL=https://staging.example.com
 *   3. Properties file       src/test/resources/config/&lt;env&gt;.properties
 * </pre>
 *
 * This is the "12-factor" approach: the same build artifact runs in every
 * environment, and CI/CD injects the target by setting an environment variable.
 */
public final class ConfigKeys {

    /** Root URL of the API under test, for example {@code https://dummyjson.com}. */
    public static final String BASE_URL = "base.url";

    /** Optional prefix applied to every path, for example {@code /api/v1}. Empty by default. */
    public static final String BASE_PATH = "base.path";

    /** TCP connection timeout in milliseconds. */
    public static final String CONNECTION_TIMEOUT_MS = "timeout.connection.ms";

    /** Time to wait for the server to send data, in milliseconds. */
    public static final String SOCKET_TIMEOUT_MS = "timeout.socket.ms";

    /** When true, every request and response is printed to the console. */
    public static final String LOG_REQUESTS = "log.requests";

    /** Relax TLS certificate validation. Only ever enable this for test servers with self-signed certs. */
    public static final String RELAXED_HTTPS = "relaxed.https";

    /** Enable automatic retries of failed tests (useful for flaky network conditions). */
    public static final String RETRY_ENABLED = "retry.enabled";

    /** Number of additional attempts after the first failure. */
    public static final String RETRY_COUNT = "retry.count";

    /** Pause between retry attempts, in milliseconds. */
    public static final String RETRY_DELAY_MS = "retry.delay.ms";

    /** Username used to obtain an authentication token. */
    public static final String AUTH_USERNAME = "auth.username";

    /** Password used to obtain an authentication token. */
    public static final String AUTH_PASSWORD = "auth.password";

    private ConfigKeys() {
        // Constants only
    }
}
