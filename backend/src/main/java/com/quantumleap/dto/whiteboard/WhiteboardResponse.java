package com.quantumleap.dto.whiteboard;

import lombok.*;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhiteboardResponse {
    private String id;
    private String name;
    private String ownerName;
    private OffsetDateTime createdAt;
    private boolean owner;
    private boolean isPublic;
}
