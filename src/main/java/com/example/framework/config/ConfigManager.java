package com.example.framework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Single entry point for reading configuration.
 *
 * <p>Loads {@code config/default.properties} first, then overlays
 * {@code config/<env>.properties}. Anything can be overridden at runtime by a
 * system property or an environment variable, which is how CI/CD injects
 * environment-specific values without rebuilding.
 *
 * <p>Usage:
 * <pre>{@code
 *   String baseUrl = ConfigManager.get(ConfigKeys.BASE_URL);
 *   int timeout   = ConfigManager.getInt(ConfigKeys.SOCKET_TIMEOUT_MS);
 *   boolean retry = ConfigManager.getBoolean(ConfigKeys.RETRY_ENABLED);
 * }</pre>
 */
public final class ConfigManager {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigManager.class);

    private static final String DEFAULT_ENV = "qa";
    private static final String DEFAULT_CONFIG = "config/default.properties";
    private static final String ENV_CONFIG_TEMPLATE = "config/%s.properties";

    private static final String SYSTEM_PROPERTY_PREFIX = "framework.";
    private static final String ENV_VARIABLE_PREFIX = "FRAMEWORK_";

    private static final ConfigManager INSTANCE = new ConfigManager();

    private final Properties properties = new Properties();
    private final String environment;

    private ConfigManager() {
        this.environment = resolveEnvironment();
        loadRequired(DEFAULT_CONFIG);

        String envConfig = String.format(ENV_CONFIG_TEMPLATE, environment);
        if (resourceExists(envConfig)) {
            loadOptional(envConfig);
        } else {
            LOG.warn("No environment file found at '{}'. Using defaults only.", envConfig);
        }

        // Use the instance method here: the static INSTANCE field is not assigned yet.
        LOG.info("Configuration ready | env={} | baseUrl={}", environment, resolve(ConfigKeys.BASE_URL));
    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    /** @return the active environment name, for example {@code qa}. */
    public static String environment() {
        return INSTANCE.environment;
    }

    /**
     * Reads a required value.
     *
     * @throws IllegalStateException if the key is not defined anywhere
     */
    public static String get(String key) {
        return INSTANCE.resolve(key);
    }

    /** Reads an optional value, returning {@code defaultValue} when the key is absent. */
    public static String get(String key, String defaultValue) {
        String value = INSTANCE.resolveOrNull(key);
        return value == null ? defaultValue : value;
    }

    public static int getInt(String key) {
        String value = get(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Configuration key '" + key + "' must be an integer but was '" + value + "'", e);
        }
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key, "false"));
    }

    private String resolve(String key) {
        String value = resolveOrNull(key);
        if (value == null) {
            throw new IllegalStateException(
                    "Missing required configuration key '" + key + "'. "
                            + "Define it in config/default.properties, config/" + environment + ".properties, "
                            + "or pass -D" + SYSTEM_PROPERTY_PREFIX + key + "=...");
        }
        return value;
    }

    private String resolveOrNull(String key) {
        String systemValue = System.getProperty(SYSTEM_PROPERTY_PREFIX + key);
        if (isPresent(systemValue)) {
            return systemValue.trim();
        }

        String envValue = System.getenv(toEnvironmentVariable(key));
        if (isPresent(envValue)) {
            return envValue.trim();
        }

        String fileValue = properties.getProperty(key);
        return isPresent(fileValue) ? fileValue.trim() : null;
    }

    private static String resolveEnvironment() {
        String fromProperty = System.getProperty("env");
        if (isPresent(fromProperty)) {
            return fromProperty.trim();
        }
        String fromEnvironment = System.getenv("ENV");
        if (isPresent(fromEnvironment)) {
            return fromEnvironment.trim();
        }
        return DEFAULT_ENV;
    }

    /** {@code base.url} becomes {@code FRAMEWORK_BASE_URL}. */
    private static String toEnvironmentVariable(String key) {
        return ENV_VARIABLE_PREFIX + key.toUpperCase().replace('.', '_').replace('-', '_');
    }

    private void loadRequired(String classpathResource) {
        if (!loadOptional(classpathResource)) {
            throw new IllegalStateException("Required configuration file missing from classpath: " + classpathResource);
        }
    }

    private boolean loadOptional(String classpathResource) {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(classpathResource)) {
            if (stream == null) {
                return false;
            }
            properties.load(stream);
            return true;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read configuration file: " + classpathResource, e);
        }
    }

    private boolean resourceExists(String classpathResource) {
        return getClass().getClassLoader().getResource(classpathResource) != null;
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
