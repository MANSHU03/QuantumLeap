package com.quantumleap.repository;

import com.quantumleap.entity.Whiteboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Whiteboard entity
 */
@Repository
public interface WhiteboardRepository extends JpaRepository<Whiteboard, UUID> {

    /**
     * Find whiteboards by owner ID
     *
     * @param ownerId owner user ID
     * @return list of whiteboards owned by the user
     */
    List<Whiteboard> findByOwnerId(UUID ownerId);

    /**
     * Find whiteboards by name containing (case-insensitive)
     *
     * @param name name to search for
     * @return list of whiteboards with matching names
     */
    @Query("SELECT w FROM Whiteboard w WHERE LOWER(w.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Whiteboard> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Find whiteboards created after a specific date
     *
     * @param date date to search from
     * @return list of whiteboards created after the date
     */
    List<Whiteboard> findByCreatedAtAfter(OffsetDateTime date);

    /**
     * Find whiteboards by owner ID ordered by creation date (newest first)
     *
     * @param ownerId owner user ID
     * @return list of whiteboards ordered by creation date
     */
    @Query("SELECT w FROM Whiteboard w WHERE w.owner.id = :ownerId ORDER BY w.createdAt DESC")
    List<Whiteboard> findByOwnerIdOrderByCreatedAtDesc(@Param("ownerId") UUID ownerId);

    /**
     * Count whiteboards by owner ID
     *
     * @param ownerId owner user ID
     * @return count of whiteboards owned by the user
     */
    long countByOwnerId(UUID ownerId);

    Optional<Whiteboard> findByName(String name);
}
