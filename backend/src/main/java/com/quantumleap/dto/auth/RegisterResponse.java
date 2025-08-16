package com.quantumleap.dto.auth;

import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
    private UUID userId;
    private String email;
    private String name;
    private OffsetDateTime createdAt;
    private com.quantumleap.dto.auth.UserDto user;
    private String message;

    public void setUser(UserDto user) {
        this.user = user;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
