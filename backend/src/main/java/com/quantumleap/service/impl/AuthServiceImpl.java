package com.quantumleap.service.impl;

import com.quantumleap.dto.auth.LoginResponse;
import com.quantumleap.dto.auth.RegisterResponse;
import com.quantumleap.entity.User;
import com.quantumleap.repository.UserRepository;
import com.quantumleap.service.AuthService;
import com.quantumleap.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Implementation of AuthService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(String email, String password) {
        log.debug("Attempting login for user: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user.getId().toString(), user.getName());
        
        LoginResponse response = new LoginResponse();
        response.setUser(convertToUserDto(user));
        response.setAccessToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtTokenProvider.getExpiration());
        
        log.info("User {} logged in successfully", email);
        return response;
    }

    @Override
    public RegisterResponse register(String email, String password, String name) {
        log.debug("Attempting registration for user: {}", email);
        
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User with this email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setName(name);
        user.setCreatedAt(OffsetDateTime.now());

        User savedUser = userRepository.save(user);
        
        RegisterResponse response = new RegisterResponse();
        response.setUser(convertToUserDto(savedUser));
        response.setMessage("User registered successfully");
        
        log.info("User {} registered successfully", email);
        return response;
    }

    @Override
    public void logout(String token) {
        log.debug("Logging out user with token");
        // In a real application, you might want to blacklist the token
        // For now, we'll just log the logout
        log.info("User logged out");
    }

    @Override
    public boolean validateToken(String token) {
        try {
            return jwtTokenProvider.validateToken(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public User getUserById(UUID userId) {
        log.debug("Fetching user by ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Convert User entity to UserDto
     */
    private com.quantumleap.dto.auth.UserDto convertToUserDto(User user) {
        com.quantumleap.dto.auth.UserDto dto = new com.quantumleap.dto.auth.UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
