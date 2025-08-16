package com.quantumleap.service.impl;

import com.quantumleap.config.JwtTokenProvider;
import com.quantumleap.dto.auth.LoginResponse;
import com.quantumleap.dto.auth.RegisterResponse;
import com.quantumleap.entity.User;
import com.quantumleap.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin_Success() {
        String email = "test@example.com";
        String password = "password";
        String encodedPassword = "encodedPassword";
        String token = "jwt-token";
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setPasswordHash(encodedPassword);
        user.setName("Test User");
        user.setCreatedAt(OffsetDateTime.now());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtTokenProvider.generateToken(userId.toString(), user.getName())).thenReturn(token);
        when(jwtTokenProvider.getExpiration()).thenReturn(3600L);

        LoginResponse response = authService.login(email, password);

        assertNotNull(response);
        assertEquals(token, response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        assertEquals(email, response.getUser().getEmail());
        assertEquals("Test User", response.getUser().getName());
    }

    @Test
    void testLogin_Failure_InvalidPassword() {
        String email = "test@example.com";
        String password = "wrongpassword";
        String encodedPassword = "encodedPassword";
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash(encodedPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(email, password));
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void testRegister_Success() {
        String email = "new@example.com";
        String password = "password";
        String encodedPassword = "encodedPassword";
        String name = "New User";
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash(encodedPassword);
        user.setName(name);
        user.setCreatedAt(OffsetDateTime.now());

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        RegisterResponse response = authService.register(email, password, name);

        assertNotNull(response);
        assertEquals(email, response.getUser().getEmail());
        assertEquals(name, response.getUser().getName());
        assertEquals("User registered successfully", response.getMessage());
    }

    @Test
    void testRegister_Failure_EmailExists() {
        String email = "existing@example.com";
        String password = "password";
        String name = "Existing User";

        when(userRepository.existsByEmail(email)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(email, password, name));
        assertEquals("User with this email already exists", ex.getMessage());
    }
}
