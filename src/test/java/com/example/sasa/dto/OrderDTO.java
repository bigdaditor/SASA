package com.example.sasa.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {
    @NotNull(message = "Order ID is required")
    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Products list is required")
    @Size(min = 1, message = "Order must contain at least one product")
    @Valid
    private List<ProductDTO> products;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", message = "Total amount must be positive")
    @Digits(integer = 12, fraction = 2, message = "Total amount must have at most 12 integer digits and 2 decimal places")
    private BigDecimal totalAmount;

    @NotNull(message = "Order status is required")
    @Pattern(regexp = "^(PENDING|PROCESSING|COMPLETED|CANCELLED|SHIPPED)$",
             message = "Status must be one of: PENDING, PROCESSING, COMPLETED, CANCELLED, SHIPPED")
    private String status;

    @NotNull(message = "Creation date is required")
    @PastOrPresent(message = "Creation date cannot be in the future")
    private LocalDateTime createdAt;

    public OrderDTO() {
    }

    public OrderDTO(Long id, Long userId, List<ProductDTO> products, BigDecimal totalAmount, String status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.products = products;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<ProductDTO> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDTO> products) {
        this.products = products;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
