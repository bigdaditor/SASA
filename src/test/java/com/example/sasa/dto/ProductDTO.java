package com.example.sasa.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class ProductDTO {
    @NotNull(message = "Product ID is required")
    private Long id;

    @NotNull(message = "Product name is required")
    @Size(min = 1, max = 200, message = "Product name must be between 1 and 200 characters")
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal price;

    @NotNull(message = "Category is required")
    @Pattern(regexp = "^[A-Za-z]+$", message = "Category must contain only letters")
    private String category;

    @NotNull(message = "Availability status is required")
    private Boolean available;

    @NotNull(message = "Tags cannot be null")
    @Size(min = 0, max = 10, message = "Tags list must contain at most 10 items")
    private List<String> tags;

    public ProductDTO() {
    }

    public ProductDTO(Long id, String name, BigDecimal price, String category, Boolean available, List<String> tags) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.available = available;
        this.tags = tags;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
