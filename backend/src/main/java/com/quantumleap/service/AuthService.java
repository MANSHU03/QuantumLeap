package com.quantumleap.service;

import com.quantumleap.dto.auth.LoginResponse;
import com.quantumleap.dto.auth.RegisterResponse;
import com.quantumleap.entity.User;

import java.util.UUID;

/**
 * Service interface for authentication operations
 */
public interface AuthService {

    /**
     * Authenticate user and return login response with JWT token
     *
     * @param email    user email
     * @param password user password
     * @return LoginResponse containing user info and JWT token
     * @throws RuntimeException if authentication fails
     */
    LoginResponse login(String email, String password);

    /**
     * Register a new user
     *
     * @param email    user email
     * @param password user password
     * @param name     user name
     * @return RegisterResponse containing user info
     * @throws RuntimeException if registration fails
     */
    RegisterResponse register(String email, String password, String name);

    /**
     * Logout user by invalidating token
     *
     * @param token JWT token to invalidate
     */
    void logout(String token);

    /**
     * Validate JWT token
     *
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
     */
    boolean validateToken(String token);

    /**
     * Get user by ID
     *
     * @param userId user ID
     * @return User entity
     * @throws RuntimeException if user not found
     */
    User getUserById(UUID userId);

    /**
     * Get user by email
     *
     * @param email user email
     * @return User entity or null if not found
     */
    User getUserByEmail(String email);

    /**
     * Check if user exists by email
     *
     * @param email user email
     * @return true if user exists, false otherwise
     */
    boolean userExists(String email);
}
