package com.microservices.order_service.service;

import com.microservices.order_service.client.UserClient;
import com.microservices.order_service.client.UserDTO;
import com.microservices.order_service.exception.ResourceNotFoundException;
import com.microservices.order_service.exception.ServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserServiceClient {

    @Autowired
    private UserClient userClient;

    @CircuitBreaker(name = "userServiceCB", fallbackMethod = "getUserFallback")
    public UserDTO getUserById(Long userId) {
        return userClient.getUserById(userId);
    }

    public UserDTO getUserFallback(Long userId, Throwable throwable) {
        if (throwable instanceof FeignException.NotFound) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        throw new ServiceUnavailableException("User Service is currently unavailable. Please try again later.");
    }
}