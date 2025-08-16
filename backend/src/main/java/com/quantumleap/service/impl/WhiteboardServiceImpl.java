package com.quantumleap.service.impl;

import com.quantumleap.dto.whiteboard.WhiteboardResponse;
import com.quantumleap.entity.Whiteboard;
import com.quantumleap.entity.WhiteboardMember;
import com.quantumleap.entity.User;
import com.quantumleap.repository.WhiteboardRepository;
import com.quantumleap.repository.WhiteboardMemberRepository;
import com.quantumleap.repository.UserRepository;
import com.quantumleap.repository.EventRepository;
import com.quantumleap.service.WhiteboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of WhiteboardService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WhiteboardServiceImpl implements WhiteboardService {

    private final WhiteboardRepository whiteboardRepository;
    private final WhiteboardMemberRepository whiteboardMemberRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<WhiteboardResponse> getUserWhiteboards(UUID userId) {
        log.debug("Fetching all whiteboards for user: {}", userId);
        
        // Get all whiteboards since they're all public
        List<Whiteboard> allWhiteboards = whiteboardRepository.findAll();
        
        return allWhiteboards.stream()
                .map(whiteboard -> {
                    // Check if user is a member and if they're the owner
                    WhiteboardMember membership = whiteboardMemberRepository.findByWhiteboardIdAndUserId(whiteboard.getId(), userId)
                            .orElse(null);
                    boolean isOwner = membership != null && membership.isOwner();
                    
                    return convertToWhiteboardResponse(whiteboard, isOwner);
                })
                .collect(Collectors.toList());
    }

    @Override
    public WhiteboardResponse createWhiteboard(String name, UUID userId) {
        log.debug("Creating whiteboard '{}' for user: {}", name, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Whiteboard whiteboard = new Whiteboard();
        whiteboard.setName(name);
        whiteboard.setOwner(user);
        whiteboard.setCreatedAt(OffsetDateTime.now());

        Whiteboard savedWhiteboard = whiteboardRepository.save(whiteboard);

        // Add owner as member
        WhiteboardMember member = new WhiteboardMember();
        member.setWhiteboard(savedWhiteboard);
        member.setUser(user);
        member.setOwner(true);
        member.setJoinedAt(OffsetDateTime.now());
        whiteboardMemberRepository.save(member);

        log.info("Whiteboard '{}' created successfully by user: {}", name, userId);
        return convertToWhiteboardResponse(savedWhiteboard, true);
    }

    @Override
    public WhiteboardResponse getWhiteboardById(UUID boardId, UUID userId) {
        log.debug("Fetching whiteboard: {} for user: {}", boardId, userId);
        
        Whiteboard whiteboard = getWhiteboardById(boardId);
        
        // Check if user is already a member
        WhiteboardMember membership = whiteboardMemberRepository.findByWhiteboardIdAndUserId(boardId, userId)
                .orElse(null);
        
        boolean isOwner = false;
        
        if (membership == null) {
            // User is not a member, add them automatically since all whiteboards are public
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            membership = new WhiteboardMember();
            membership.setWhiteboard(whiteboard);
            membership.setUser(user);
            membership.setOwner(false);
            membership.setJoinedAt(OffsetDateTime.now());
            whiteboardMemberRepository.save(membership);
            
            log.info("User {} automatically added to public whiteboard: {}", userId, boardId);
        } else {
            isOwner = membership.isOwner();
        }

        return convertToWhiteboardResponse(whiteboard, isOwner);
    }

    @Override
    public WhiteboardResponse joinWhiteboard(UUID boardId, UUID userId) {
        log.debug("User {} joining whiteboard: {}", userId, boardId);
        
        Whiteboard whiteboard = getWhiteboardById(boardId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if already a member
        if (whiteboardMemberRepository.existsByWhiteboardIdAndUserId(boardId, userId)) {
            throw new RuntimeException("User is already a member of this whiteboard");
        }

        // Add user as member
        WhiteboardMember member = new WhiteboardMember();
        member.setWhiteboard(whiteboard);
        member.setUser(user);
        member.setOwner(false);
        member.setJoinedAt(OffsetDateTime.now());
        whiteboardMemberRepository.save(member);

        log.info("User {} joined whiteboard: {}", userId, boardId);
        return convertToWhiteboardResponse(whiteboard, false);
    }

    @Override
    public void deleteWhiteboard(UUID boardId, UUID userId) {
        log.debug("User {} deleting whiteboard: {}", userId, boardId);
        
        // First check if whiteboard exists
        Whiteboard whiteboard = whiteboardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Whiteboard not found"));
        
        WhiteboardMember membership = whiteboardMemberRepository.findByWhiteboardIdAndUserId(boardId, userId)
                .orElseThrow(() -> new RuntimeException("Access denied to whiteboard"));

        if (!membership.isOwner()) {
            throw new RuntimeException("Only whiteboard owner can delete the whiteboard");
        }

        try {
            // Delete all events first (they reference the whiteboard)
            int deletedEvents = eventRepository.deleteByWhiteboardId(boardId);
            log.debug("Deleted {} events for whiteboard: {}", deletedEvents, boardId);
            
            // Delete all memberships
            whiteboardMemberRepository.deleteByWhiteboardId(boardId);
            
            // Finally delete the whiteboard
            whiteboardRepository.deleteById(boardId);

            log.info("Whiteboard {} deleted successfully by user: {}", boardId, userId);
        } catch (Exception e) {
            log.error("Error deleting whiteboard {}: {}", boardId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete whiteboard: " + e.getMessage());
        }
    }

    @Override
    public List<String> getWhiteboardMembers(UUID boardId, UUID userId) {
        log.debug("Fetching members for whiteboard: {}", boardId);
        
        if (!hasAccessToWhiteboard(boardId, userId)) {
            throw new RuntimeException("Access denied to whiteboard");
        }

        List<WhiteboardMember> memberships = whiteboardMemberRepository.findByWhiteboardId(boardId);
        
        return memberships.stream()
                .map(membership -> membership.getUser().getName())
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasAccessToWhiteboard(UUID boardId, UUID userId) {
        // All whiteboards are public - any user can access any whiteboard
        return true;
    }

    @Override
    public Whiteboard getWhiteboardById(UUID boardId) {
        return whiteboardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Whiteboard not found"));
    }

    /**
     * Convert Whiteboard entity to WhiteboardResponse
     */
    private WhiteboardResponse convertToWhiteboardResponse(Whiteboard whiteboard, boolean isOwner) {
        WhiteboardResponse response = new WhiteboardResponse();
        response.setId(whiteboard.getId().toString());
        response.setName(whiteboard.getName());
        response.setOwnerName(whiteboard.getOwner().getName());
        response.setCreatedAt(whiteboard.getCreatedAt());
        response.setOwner(isOwner);
        response.setPublic(true); // All whiteboards are public
        return response;
    }
}
