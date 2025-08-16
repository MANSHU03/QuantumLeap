package com.quantumleap.controller;

import com.quantumleap.dto.auth.LoginRequest;
import com.quantumleap.dto.auth.LoginResponse;
import com.quantumleap.dto.auth.RegisterRequest;
import com.quantumleap.dto.auth.RegisterResponse;
import com.quantumleap.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management endpoints")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getEmail());
        LoginResponse response = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration attempt for user: {}", registerRequest.getEmail());
        RegisterResponse response = authService.register(
            registerRequest.getEmail(), 
            registerRequest.getPassword(), 
            registerRequest.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user (invalidate token)")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        log.info("Logout request received");
        authService.logout(token.replace("Bearer ", ""));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate token", description = "Check if JWT token is valid")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        boolean isValid = authService.validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(isValid);
    }
}
