package com.quantumleap.dto.auth;

import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private UUID id;
    private String email;
    private String name;
    private OffsetDateTime createdAt;
}
