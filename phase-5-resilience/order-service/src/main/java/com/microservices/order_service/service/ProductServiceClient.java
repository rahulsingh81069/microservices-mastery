package com.microservices.order_service.service;

import com.microservices.order_service.client.ProductClient;
import com.microservices.order_service.client.ProductDTO;
import com.microservices.order_service.exception.ResourceNotFoundException;
import com.microservices.order_service.exception.ServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductServiceClient {

    @Autowired
    private ProductClient productClient;

    @CircuitBreaker(name = "productServiceCB", fallbackMethod = "getProductFallback")
    public ProductDTO getProductById(Long productId) {
        return productClient.getProductById(productId);
    }

    public ProductDTO getProductFallback(Long productId, Throwable throwable) {
        if (throwable instanceof FeignException.NotFound) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        throw new ServiceUnavailableException("Product Service is currently unavailable. Please try again later.");
    }
}