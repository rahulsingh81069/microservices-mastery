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

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private ProductClient productClient;

    public Order createOrder(Long userId, Long productId, Integer quantity) {

        UserDTO user;
        try {
            user = userClient.getUserById(userId);
        } catch (FeignException e) {
                throw new ServiceUnavailableException("User Service is currently unavailable. Please try again later.");
        }

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