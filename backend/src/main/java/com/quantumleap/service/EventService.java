package com.quantumleap.service;

import com.quantumleap.dto.ws.EventEnvelope;
import com.quantumleap.entity.Event;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for event processing operations
 */
public interface EventService {

    /**
     * Process and persist an event
     *
     * @param eventEnvelope event envelope to process
     * @param userId        user ID who triggered the event
     * @return processed event envelope
     * @throws RuntimeException if event processing fails
     */
    EventEnvelope processEvent(EventEnvelope eventEnvelope, UUID userId);

    /**
     * Get events for a specific whiteboard
     *
     * @param boardId whiteboard ID
     * @param limit   maximum number of events to return
     * @return list of events
     */
    List<EventEnvelope> getEventsForBoard(UUID boardId, int limit);

    /**
     * Get events for a whiteboard with pagination
     *
     * @param boardId whiteboard ID
     * @param offset  number of events to skip
     * @param limit   maximum number of events to return
     * @return list of events
     */
    List<EventEnvelope> getEventsForBoard(UUID boardId, int offset, int limit);

    /**
     * Get event by ID
     *
     * @param eventId event ID
     * @return event envelope
     * @throws RuntimeException if event not found
     */
    EventEnvelope getEventById(UUID eventId);

    /**
     * Get events by type for a whiteboard
     *
     * @param boardId   whiteboard ID
     * @param eventType type of events to retrieve
     * @param limit     maximum number of events to return
     * @return list of events of the specified type
     */
    List<EventEnvelope> getEventsByType(UUID boardId, String eventType, int limit);

    /**
     * Delete events for a whiteboard
     *
     * @param boardId whiteboard ID
     */
    void deleteEventsForBoard(UUID boardId);

    /**
     * Get event count for a whiteboard
     *
     * @param boardId whiteboard ID
     * @return number of events
     */
    long getEventCountForBoard(UUID boardId);
}
