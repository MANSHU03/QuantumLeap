package com.quantumleap.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "whiteboard_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhiteboardMember {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "whiteboard_id", nullable = false)
    private Whiteboard whiteboard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean owner;

    @CreationTimestamp
    private OffsetDateTime joinedAt;

    public boolean isOwner() {
        return owner;
    }
    public void setOwner(boolean owner) {
        this.owner = owner;
    }
}
