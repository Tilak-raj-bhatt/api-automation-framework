package com.example.framework.listeners;

import com.example.framework.config.ConfigKeys;
import com.example.framework.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Console logging for suite and test lifecycle events.
 *
 * <p>CI logs are often the only evidence available when a pipeline fails at 2am.
 * This listener writes a compact, greppable trail with timings and failure reasons
 * so a failing build can be diagnosed without downloading artifacts.
 */
public class TestListener implements ITestListener, ISuiteListener {

    private static final Logger LOG = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void onStart(ISuite suite) {
        LOG.info("");
        LOG.info("==========================================================");
        LOG.info("SUITE START : {}", suite.getName());
        LOG.info("Environment : {}", ConfigManager.environment());
        LOG.info("Target      : {}", ConfigManager.get(ConfigKeys.BASE_URL));
        LOG.info("==========================================================");
    }

    @Override
    public void onFinish(ISuite suite) {
        LOG.info("==========================================================");
        LOG.info("SUITE END   : {}", suite.getName());
        LOG.info("==========================================================");
    }

    @Override
    public void onStart(ITestContext context) {
        LOG.info("--- Test group '{}' started", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        LOG.info("--- Test group '{}' finished | passed={} failed={} skipped={}",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
    }

    @Override
    public void onTestStart(ITestResult result) {
        LOG.info("START  {}", label(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOG.info("PASS   {} ({} ms)", label(result), duration(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOG.error("FAIL   {} ({} ms)", label(result), duration(result));
        Throwable failure = result.getThrowable();
        if (failure != null) {
            LOG.error("Reason : {}", failure.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOG.warn("SKIP   {}", label(result));
    }

    private String label(ITestResult result) {
        String description = result.getMethod().getDescription();
        if (description != null && !description.isBlank()) {
            return description;
        }
        return result.getTestClass().getName() + "#" + result.getMethod().getMethodName();
    }

    private long duration(ITestResult result) {
        return Math.max(0L, result.getEndMillis() - result.getStartMillis());
    }
}
