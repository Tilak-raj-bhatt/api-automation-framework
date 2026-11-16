package com.example.framework.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Physical dimensions of a product, nested inside the product payload. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dimensions {

    private Double width;
    private Double height;
    private Double depth;

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getDepth() {
        return depth;
    }

    public void setDepth(Double depth) {
        this.depth = depth;
    }
}
