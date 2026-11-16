package com.example.framework.api;

import com.example.framework.model.ProductRequest;
import com.example.framework.specs.RequestSpecs;
import io.restassured.response.Response;

/**
 * Reusable client for the {@code /products} resource.
 *
 * <p>This layer knows endpoints and payloads - nothing about assertions. Tests
 * call these methods and decide what "correct" means, which keeps business logic
 * out of the test layer and lets many tests share one implementation.
 */
public class ProductsApi {

    private static final String PRODUCTS = "/products";
    private static final String PRODUCT_BY_ID = "/products/{id}";
    private static final String PRODUCT_ADD = "/products/add";
    private static final String PRODUCT_SEARCH = "/products/search";
    private static final String PRODUCT_CATEGORIES = "/products/categories";

    public Response list() {
        return RequestSpecs.base()
                .when()
                .get(PRODUCTS);
    }

    public Response list(int limit, int skip) {
        return RequestSpecs.base()
                .queryParam("limit", limit)
                .queryParam("skip", skip)
                .when()
                .get(PRODUCTS);
    }

    /** Requests only the named fields, for example {@code listFields(2, "title", "price")}. */
    public Response listFields(int limit, String... fields) {
        return RequestSpecs.base()
                .queryParam("limit", limit)
                .queryParam("select", String.join(",", fields))
                .when()
                .get(PRODUCTS);
    }

    public Response getById(int id) {
        return RequestSpecs.base()
                .pathParam("id", id)
                .when()
                .get(PRODUCT_BY_ID);
    }

    public Response search(String query) {
        return RequestSpecs.base()
                .queryParam("q", query)
                .when()
                .get(PRODUCT_SEARCH);
    }

    public Response categories() {
        return RequestSpecs.base()
                .when()
                .get(PRODUCT_CATEGORIES);
    }

    public Response create(ProductRequest payload) {
        return RequestSpecs.base()
                .body(payload)
                .when()
                .post(PRODUCT_ADD);
    }

    public Response update(int id, ProductRequest payload) {
        return RequestSpecs.base()
                .pathParam("id", id)
                .body(payload)
                .when()
                .put(PRODUCT_BY_ID);
    }

    public Response delete(int id) {
        return RequestSpecs.base()
                .pathParam("id", id)
                .when()
                .delete(PRODUCT_BY_ID);
    }
}
