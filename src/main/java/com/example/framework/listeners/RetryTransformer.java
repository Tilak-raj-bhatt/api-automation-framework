package com.example.framework.listeners;

import com.example.framework.config.ConfigKeys;
import com.example.framework.config.ConfigManager;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Applies {@link RetryAnalyzer} to every {@code @Test} method when retries are
 * enabled, so individual tests never need to declare it.
 */
public class RetryTransformer implements IAnnotationTransformer {

    @Override
    @SuppressWarnings("rawtypes")
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        if (ConfigManager.getBoolean(ConfigKeys.RETRY_ENABLED)) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
        }
    }
}
