package com.microservices.user_service.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String role;
    // password field bilkul nahi hai yahan
}