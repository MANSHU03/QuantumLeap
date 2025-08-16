package com.quantumleap.entity;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity to track cursor positions for users on whiteboards
 * This is not persisted to database, only used for real-time cursor tracking
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursorPosition {
    private UUID userId;
    private String userName;
    private String userColor;
    private double x;
    private double y;
    private OffsetDateTime lastUpdated;
    private boolean isActive;
}
