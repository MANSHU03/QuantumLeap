package com.quantumleap.dto.ws;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for cursor update events
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursorUpdateEvent {
    private UUID userId;
    private String userName;
    private String userColor;
    private double x;
    private double y;
    private OffsetDateTime timestamp;
    private String eventType; // "MOVE", "JOIN", "LEAVE"
}
