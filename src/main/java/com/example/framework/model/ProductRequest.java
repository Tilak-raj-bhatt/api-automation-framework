package com.example.framework.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Request payload for creating or updating a product.
 *
 * <p>A builder is used because these payloads have many optional fields and
 * object construction should read like a sentence in the test. Fields that are
 * {@code null} are omitted from the JSON, which matters for PATCH-style APIs
 * where "absent" and "explicitly null" mean different things.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductRequest {

    private String title;
    private String description;
    private String category;
    private String brand;
    private Double price;
    private Integer stock;

    public ProductRequest() {
    }

    private ProductRequest(Builder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.category = builder.category;
        this.brand = builder.brand;
        this.price = builder.price;
        this.stock = builder.stock;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Convenience factory for a minimal update payload. */
    public static ProductRequest withTitle(String title) {
        return builder().title(title).build();
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getBrand() {
        return brand;
    }

    public Double getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public static final class Builder {
        private String title;
        private String description;
        private String category;
        private String brand;
        private Double price;
        private Integer stock;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public Builder price(Double price) {
            this.price = price;
            return this;
        }

        public Builder stock(Integer stock) {
            this.stock = stock;
            return this;
        }

        public ProductRequest build() {
            return new ProductRequest(this);
        }
    }
}
