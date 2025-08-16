package com.quantumleap.dto.auth;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private com.quantumleap.dto.auth.UserDto user;
    private String accessToken;
    private String tokenType;
    private long expiresIn;

    public void setUser(com.quantumleap.dto.auth.UserDto user) {
        this.user = user;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
