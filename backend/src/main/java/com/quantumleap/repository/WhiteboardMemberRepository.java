package com.quantumleap.repository;

import com.quantumleap.entity.WhiteboardMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for WhiteboardMember entity
 */
@Repository
public interface WhiteboardMemberRepository extends JpaRepository<WhiteboardMember, UUID> {

    /**
     * Find all memberships for a user
     *
     * @param userId user ID
     * @return list of whiteboard memberships for the user
     */
    List<WhiteboardMember> findByUserId(UUID userId);

    /**
     * Find all members of a whiteboard
     *
     * @param whiteboardId whiteboard ID
     * @return list of whiteboard members
     */
    List<WhiteboardMember> findByWhiteboardId(UUID whiteboardId);

    /**
     * Find membership by whiteboard ID and user ID
     *
     * @param whiteboardId whiteboard ID
     * @param userId       user ID
     * @return Optional containing membership if found
     */
    Optional<WhiteboardMember> findByWhiteboardIdAndUserId(UUID whiteboardId, UUID userId);

    /**
     * Check if membership exists by whiteboard ID and user ID
     *
     * @param whiteboardId whiteboard ID
     * @param userId       user ID
     * @return true if membership exists, false otherwise
     */
    boolean existsByWhiteboardIdAndUserId(UUID whiteboardId, UUID userId);

    /**
     * Find all owners of a whiteboard
     *
     * @param whiteboardId whiteboard ID
     * @return list of owner memberships
     */
    List<WhiteboardMember> findByWhiteboardIdAndOwnerTrue(UUID whiteboardId);

    /**
     * Find all non-owner members of a whiteboard
     *
     * @param whiteboardId whiteboard ID
     * @return list of non-owner memberships
     */
    List<WhiteboardMember> findByWhiteboardIdAndOwnerFalse(UUID whiteboardId);

    /**
     * Count members of a whiteboard
     *
     * @param whiteboardId whiteboard ID
     * @return number of members
     */
    long countByWhiteboardId(UUID whiteboardId);

    /**
     * Delete all memberships for a whiteboard
     *
     * @param whiteboardId whiteboard ID
     */
    @Modifying
    @Query("DELETE FROM WhiteboardMember wm WHERE wm.whiteboard.id = :whiteboardId")
    void deleteByWhiteboardId(@Param("whiteboardId") UUID whiteboardId);

    /**
     * Delete membership by whiteboard ID and user ID
     *
     * @param whiteboardId whiteboard ID
     * @param userId       user ID
     */
    @Modifying
    @Query("DELETE FROM WhiteboardMember wm WHERE wm.whiteboard.id = :whiteboardId AND wm.user.id = :userId")
    void deleteByWhiteboardIdAndUserId(@Param("whiteboardId") UUID whiteboardId, @Param("userId") UUID userId);
}
