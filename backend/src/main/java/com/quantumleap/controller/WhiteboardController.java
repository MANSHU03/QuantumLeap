package com.quantumleap.controller;

import com.quantumleap.dto.whiteboard.CreateWhiteboardRequest;
import com.quantumleap.dto.whiteboard.WhiteboardResponse;
import com.quantumleap.service.WhiteboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/whiteboards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Whiteboard", description = "Whiteboard management endpoints")
@CrossOrigin(origins = "*")
public class WhiteboardController {

    private final WhiteboardService whiteboardService;

    @GetMapping
    @Operation(summary = "Get user's whiteboards", description = "Retrieve all whiteboards for the authenticated user")
    public ResponseEntity<List<WhiteboardResponse>> getUserWhiteboards(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Fetching whiteboards for user: {}", userId);
        List<WhiteboardResponse> whiteboards = whiteboardService.getUserWhiteboards(userId);
        return ResponseEntity.ok(whiteboards);
    }

    @PostMapping
    @Operation(summary = "Create whiteboard", description = "Create a new whiteboard")
    public ResponseEntity<WhiteboardResponse> createWhiteboard(
            @Valid @RequestBody CreateWhiteboardRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Creating whiteboard '{}' for user: {}", request.getName(), userId);
        WhiteboardResponse whiteboard = whiteboardService.createWhiteboard(request.getName(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(whiteboard);
    }

    @GetMapping("/{boardId}")
    @Operation(summary = "Get whiteboard", description = "Retrieve whiteboard by ID")
    public ResponseEntity<WhiteboardResponse> getWhiteboard(
            @Parameter(description = "Whiteboard ID") @PathVariable UUID boardId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Fetching whiteboard: {} for user: {}", boardId, userId);
        WhiteboardResponse whiteboard = whiteboardService.getWhiteboardById(boardId, userId);
        return ResponseEntity.ok(whiteboard);
    }

    @PostMapping("/{boardId}/join")
    @Operation(summary = "Join whiteboard", description = "Join an existing whiteboard")
    public ResponseEntity<WhiteboardResponse> joinWhiteboard(
            @Parameter(description = "Whiteboard ID") @PathVariable UUID boardId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("User {} joining whiteboard: {}", userId, boardId);
        WhiteboardResponse whiteboard = whiteboardService.joinWhiteboard(boardId, userId);
        return ResponseEntity.ok(whiteboard);
    }

    @DeleteMapping("/{boardId}")
    @Operation(summary = "Delete whiteboard", description = "Delete whiteboard (owner only)")
    public ResponseEntity<Void> deleteWhiteboard(
            @Parameter(description = "Whiteboard ID") @PathVariable UUID boardId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("User {} deleting whiteboard: {}", userId, boardId);
        whiteboardService.deleteWhiteboard(boardId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{boardId}/members")
    @Operation(summary = "Get whiteboard members", description = "Retrieve all members of a whiteboard")
    public ResponseEntity<List<String>> getWhiteboardMembers(
            @Parameter(description = "Whiteboard ID") @PathVariable UUID boardId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("Fetching members for whiteboard: {}", boardId);
        List<String> members = whiteboardService.getWhiteboardMembers(boardId, userId);
        return ResponseEntity.ok(members);
    }
}
