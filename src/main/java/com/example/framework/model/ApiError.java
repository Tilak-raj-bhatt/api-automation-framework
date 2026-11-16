package com.example.framework.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Standard error envelope: {@code {"message": "..."}}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiError {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ApiError{message='" + message + "'}";
    }
}
