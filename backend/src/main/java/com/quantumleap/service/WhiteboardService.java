package com.quantumleap.service;

import com.quantumleap.dto.whiteboard.WhiteboardResponse;
import com.quantumleap.entity.Whiteboard;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for whiteboard operations
 */
public interface WhiteboardService {

    /**
     * Get all whiteboards for a user
     *
     * @param userId user ID
     * @return list of whiteboard responses
     */
    List<WhiteboardResponse> getUserWhiteboards(UUID userId);

    /**
     * Create a new whiteboard
     *
     * @param name   whiteboard name
     * @param userId owner user ID
     * @return created whiteboard response
     * @throws RuntimeException if creation fails
     */
    WhiteboardResponse createWhiteboard(String name, UUID userId);

    /**
     * Get whiteboard by ID
     *
     * @param boardId whiteboard ID
     * @param userId  requesting user ID
     * @return whiteboard response
     * @throws RuntimeException if whiteboard not found or access denied
     */
    WhiteboardResponse getWhiteboardById(UUID boardId, UUID userId);

    /**
     * Join an existing whiteboard
     *
     * @param boardId whiteboard ID
     * @param userId  user ID joining
     * @return whiteboard response
     * @throws RuntimeException if whiteboard not found or access denied
     */
    WhiteboardResponse joinWhiteboard(UUID boardId, UUID userId);

    /**
     * Delete whiteboard (owner only)
     *
     * @param boardId whiteboard ID
     * @param userId  requesting user ID
     * @throws RuntimeException if whiteboard not found, access denied, or deletion fails
     */
    void deleteWhiteboard(UUID boardId, UUID userId);

    /**
     * Get whiteboard members
     *
     * @param boardId whiteboard ID
     * @param userId  requesting user ID
     * @return list of member names
     * @throws RuntimeException if whiteboard not found or access denied
     */
    List<String> getWhiteboardMembers(UUID boardId, UUID userId);

    /**
     * Check if user has access to whiteboard
     *
     * @param boardId whiteboard ID
     * @param userId  user ID
     * @return true if user has access, false otherwise
     */
    boolean hasAccessToWhiteboard(UUID boardId, UUID userId);

    /**
     * Get whiteboard entity by ID
     *
     * @param boardId whiteboard ID
     * @return whiteboard entity
     * @throws RuntimeException if whiteboard not found
     */
    Whiteboard getWhiteboardById(UUID boardId);
}
