package com.quantumleap.repository;

import com.quantumleap.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Event entity
 */
@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    /**
     * Find events by whiteboard ID ordered by timestamp (newest first)
     *
     * @param whiteboardId whiteboard ID
     * @param pageable     pagination parameters
     * @return page of events
     */
    Page<Event> findByWhiteboardIdOrderByTsDesc(UUID whiteboardId, Pageable pageable);

    /**
     * Find events by whiteboard ID ordered by timestamp (oldest first) for replay
     *
     * @param whiteboardId whiteboard ID
     * @param pageable     pagination parameters
     * @return page of events
     */
    Page<Event> findByWhiteboardIdOrderByTsAsc(UUID whiteboardId, Pageable pageable);

    /**
     * Find events by whiteboard ID and event type ordered by timestamp (newest first)
     *
     * @param whiteboardId whiteboard ID
     * @param eventType    event type
     * @param pageable     pagination parameters
     * @return page of events
     */
    Page<Event> findByWhiteboardIdAndEventTypeOrderByTsDesc(UUID whiteboardId, String eventType, Pageable pageable);

    /**
     * Find events by whiteboard ID and user ID ordered by timestamp (newest first)
     *
     * @param whiteboardId whiteboard ID
     * @param userId       user ID
     * @param pageable     pagination parameters
     * @return page of events
     */
    Page<Event> findByWhiteboardIdAndUserIdOrderByTsDesc(UUID whiteboardId, UUID userId, Pageable pageable);

    /**
     * Find events by whiteboard ID and timestamp range
     *
     * @param whiteboardId whiteboard ID
     * @param startTime    start timestamp
     * @param endTime      end timestamp
     * @return list of events in the time range
     */
    List<Event> findByWhiteboardIdAndTsBetweenOrderByTsAsc(UUID whiteboardId, OffsetDateTime startTime, OffsetDateTime endTime);

    /**
     * Find events by user ID ordered by timestamp (newest first)
     *
     * @param userId   user ID
     * @param pageable pagination parameters
     * @return page of events
     */
    Page<Event> findByUserIdOrderByTsDesc(UUID userId, Pageable pageable);

    /**
     * Find events by event type ordered by timestamp (newest first)
     *
     * @param eventType event type
     * @param pageable  pagination parameters
     * @return page of events
     */
    Page<Event> findByEventTypeOrderByTsDesc(String eventType, Pageable pageable);

    /**
     * Count events by whiteboard ID
     *
     * @param whiteboardId whiteboard ID
     * @return number of events
     */
    long countByWhiteboardId(UUID whiteboardId);

    /**
     * Count events by whiteboard ID and event type
     *
     * @param whiteboardId whiteboard ID
     * @param eventType    event type
     * @return number of events
     */
    long countByWhiteboardIdAndEventType(UUID whiteboardId, String eventType);

    /**
     * Count events by whiteboard ID and user ID
     *
     * @param whiteboardId whiteboard ID
     * @param userId       user ID
     * @return number of events
     */
    long countByWhiteboardIdAndUserId(UUID whiteboardId, UUID userId);

    /**
     * Delete all events for a whiteboard
     *
     * @param whiteboardId whiteboard ID
     * @return number of deleted events
     */
    @Modifying
    @Query("DELETE FROM Event e WHERE e.whiteboard.id = :whiteboardId")
    int deleteByWhiteboardId(@Param("whiteboardId") UUID whiteboardId);

    /**
     * Delete events by whiteboard ID and event type
     *
     * @param whiteboardId whiteboard ID
     * @param eventType    event type
     * @return number of deleted events
     */
    @Modifying
    @Query("DELETE FROM Event e WHERE e.whiteboard.id = :whiteboardId AND e.eventType = :eventType")
    long deleteByWhiteboardIdAndEventType(@Param("whiteboardId") UUID whiteboardId, @Param("eventType") String eventType);

    /**
     * Find latest event for a whiteboard
     *
     * @param whiteboardId whiteboard ID
     * @return latest event or null if none exists
     */
    @Query("SELECT e FROM Event e WHERE e.whiteboard.id = :whiteboardId ORDER BY e.ts DESC LIMIT 1")
    Event findLatestEventByWhiteboardId(@Param("whiteboardId") UUID whiteboardId);

    /**
     * Find events by whiteboard ID with custom ordering
     *
     * @param whiteboardId whiteboard ID
     * @param limit        maximum number of events
     * @return list of events
     */
    @Query("SELECT e FROM Event e WHERE e.whiteboard.id = :whiteboardId ORDER BY e.ts DESC")
    List<Event> findRecentEventsByWhiteboardId(@Param("whiteboardId") UUID whiteboardId, Pageable pageable);
}
