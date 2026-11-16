package com.example.framework.exception;

/**
 * Unchecked exception for framework-level failures (bad configuration, JSON that
 * cannot be parsed, missing test data, and so on).
 *
 * <p>Wrapping checked exceptions keeps test methods readable: a test should fail
 * with a clear domain message, not a raw {@code JsonProcessingException}.
 */
public class FrameworkException extends RuntimeException {

    public FrameworkException(String message) {
        super(message);
    }

    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
