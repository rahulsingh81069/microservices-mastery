package com.microservices.order_service.client;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
}