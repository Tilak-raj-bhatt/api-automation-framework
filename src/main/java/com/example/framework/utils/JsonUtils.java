package com.example.framework.utils;

import com.example.framework.exception.FrameworkException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;

/**
 * Thin, opinionated wrapper around Jackson.
 *
 * <p>One shared {@link ObjectMapper} is used everywhere so that serialization
 * behaviour is identical in framework code, tests, and REST Assured
 * deserialization. Unknown JSON properties are ignored so that adding a field to
 * an API response never breaks existing tests.
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    private JsonUtils() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new FrameworkException("Unable to deserialize JSON into " + type.getSimpleName(), e);
        }
    }

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new FrameworkException("Unable to serialize " + value.getClass().getSimpleName() + " to JSON", e);
        }
    }

    /** Reads a JSON file from {@code src/test/resources} (or {@code src/main/resources}). */
    public static JsonNode readTree(String classpathResource) {
        try (InputStream stream = JsonUtils.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (stream == null) {
                throw new FrameworkException("Classpath resource not found: " + classpathResource);
            }
            return MAPPER.readTree(stream);
        } catch (IOException e) {
            throw new FrameworkException("Unable to read JSON resource: " + classpathResource, e);
        }
    }

    /** Re-indents raw JSON for readable report attachments and logs. */
    public static String pretty(String json) {
        try {
            Object parsed = MAPPER.readValue(json, Object.class);
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(parsed);
        } catch (JsonProcessingException e) {
            // Not valid JSON (HTML error page, plain text, empty body) - return as-is.
            return json;
        }
    }
}
