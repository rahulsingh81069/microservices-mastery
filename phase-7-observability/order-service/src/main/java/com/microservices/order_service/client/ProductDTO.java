package com.microservices.order_service.client;

import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private String productName;
    private Double price;
    private Integer stockQuantity;
}