package com.microservices.order_service.service;

import com.microservices.order_service.client.ProductClient;
import com.microservices.order_service.client.ProductDTO;
import com.microservices.order_service.client.UserClient;
import com.microservices.order_service.client.UserDTO;
import com.microservices.order_service.entity.Order;
import com.microservices.order_service.exception.ResourceNotFoundException;
import com.microservices.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import feign.FeignException;
import com.microservices.order_service.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository; 


    @Autowired
    private ProductClient productClient;


    @Autowired
    private UserClient userClient;

    @CircuitBreaker(name = "userServiceCB", fallbackMethod = "getUserFallback")
    public UserDTO getUserWithCircuitBreaker(Long userId) {
        return userClient.getUserById(userId);
    }

    public UserDTO getUserFallback(Long userId, Throwable throwable) {

        if (throwable instanceof FeignException.NotFound) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        throw new ServiceUnavailableException("User Service is currently unavailable. Please try again later.");
    }



    public Order createOrder(Long userId, Long productId, Integer quantity) {

        UserDTO user=getUserWithCircuitBreaker(userId);
        
        ProductDTO product;
        try {
            product = productClient.getProductById(productId);
        } catch (FeignException e) {
            throw new ServiceUnavailableException("Product Service is currently unavailable. Please try again later.");
        }


        Double totalPrice = product.getPrice() * quantity;

        Order order = new Order();
        order.setUserId(user.getId());
        order.setProductId(product.getId());
        order.setQuantity(quantity);
        order.setTotalPrice(totalPrice);

        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }
}