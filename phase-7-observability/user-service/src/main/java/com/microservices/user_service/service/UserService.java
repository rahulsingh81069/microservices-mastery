package com.microservices.user_service.service;

import com.microservices.user_service.entity.User;
import com.microservices.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.microservices.user_service.exception.DuplicateResourceException;
import com.microservices.user_service.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        Optional<User> existingUser = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(user.getEmail()))
                .findFirst();

        if (existingUser.isPresent()) {
            throw new DuplicateResourceException("Email already registered: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));  // hash karo
        user.setRole("ROLE_USER"); 

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}