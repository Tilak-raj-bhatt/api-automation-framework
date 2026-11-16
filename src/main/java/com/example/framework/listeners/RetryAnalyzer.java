package com.example.framework.listeners;

import com.example.framework.config.ConfigKeys;
import com.example.framework.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failed test a configurable number of times.
 *
 * <p>Retries hide real defects, so this is disabled by default. Enable it only for
 * genuinely unstable infrastructure, and treat a test that only passes on retry as
 * a defect to investigate, not as a success.
 *
 * <p>Controlled by {@code retry.enabled}, {@code retry.count}, and
 * {@code retry.delay.ms} in the configuration files.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger LOG = LoggerFactory.getLogger(RetryAnalyzer.class);

    private final boolean enabled = ConfigManager.getBoolean(ConfigKeys.RETRY_ENABLED);
    private final int maxRetries = ConfigManager.getInt(ConfigKeys.RETRY_COUNT);
    private final int delayMs = ConfigManager.getInt(ConfigKeys.RETRY_DELAY_MS);

    private int attempts;

    @Override
    public boolean retry(ITestResult result) {
        if (!enabled) {
            return false;
        }
        if (attempts >= maxRetries) {
            LOG.error("No retries left for '{}' after {} attempt(s)", result.getName(), attempts);
            return false;
        }
        attempts++;
        LOG.warn("Retrying '{}' - attempt {}/{} in {} ms", result.getName(), attempts, maxRetries, delayMs);
        pause(delayMs);
        return true;
    }

    private void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
