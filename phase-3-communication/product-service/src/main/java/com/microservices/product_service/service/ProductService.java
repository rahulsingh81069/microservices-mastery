package com.microservices.product_service.service;

import com.microservices.product_service.entity.Product;
import com.microservices.product_service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.microservices.product_service.exception.ResourceNotFoundException;
import com.microservices.product_service.exception.InvalidDataException;


import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Product createProduct(Product product) {

        if (product.getStockQuantity() < 0) {
           throw new InvalidDataException("Stock quantity cannot be negative");
      }
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}