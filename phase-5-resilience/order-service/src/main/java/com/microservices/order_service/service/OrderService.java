package com.microservices.order_service.service;

import com.microservices.order_service.client.ProductDTO;
import com.microservices.order_service.client.UserDTO;
import com.microservices.order_service.entity.Order;
import com.microservices.order_service.exception.ResourceNotFoundException;
import com.microservices.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private ProductServiceClient productServiceClient;

    public Order createOrder(Long userId, Long productId, Integer quantity) {

        UserDTO user = userServiceClient.getUserById(userId);
        ProductDTO product = productServiceClient.getProductById(productId);

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