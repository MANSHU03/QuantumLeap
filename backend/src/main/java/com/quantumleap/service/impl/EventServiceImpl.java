package com.quantumleap.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantumleap.dto.ws.EventEnvelope;
import com.quantumleap.entity.Event;
import com.quantumleap.entity.Whiteboard;
import com.quantumleap.entity.User;
import com.quantumleap.repository.EventRepository;
import com.quantumleap.service.EventService;
import com.quantumleap.service.WhiteboardService;
import com.quantumleap.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of EventService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final WhiteboardService whiteboardService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    public EventEnvelope processEvent(EventEnvelope eventEnvelope, UUID userId) {
        try {
            log.debug("Processing event of type: {} for user: {}", eventEnvelope.getType(), userId);
            
            UUID boardId = UUID.fromString(eventEnvelope.getBoardId());
            
            // Verify user has access to whiteboard
            if (!whiteboardService.hasAccessToWhiteboard(boardId, userId)) {
                throw new RuntimeException("User does not have access to this whiteboard");
            }

            // Create and persist event
            Event event = new Event();
            event.setWhiteboard(whiteboardService.getWhiteboardById(boardId));
            event.setUser(authService.getUserById(userId));
            event.setEventType(eventEnvelope.getType());
            event.setPayload(objectMapper.writeValueAsString(eventEnvelope.getData()));
            event.setTs(eventEnvelope.getTs());

            Event savedEvent = eventRepository.save(event);

            // Convert back to envelope
            EventEnvelope processedEvent = convertToEventEnvelope(savedEvent);
            processedEvent.setTempId(eventEnvelope.getTempId());

            log.info("Event processed successfully: {} for whiteboard: {}", eventEnvelope.getType(), boardId);
            return processedEvent;

        } catch (Exception e) {
            log.error("Failed to process event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process event", e);
        }
    }

    @Override
    public List<EventEnvelope> getEventsForBoard(UUID boardId, int limit) {
        log.debug("Fetching {} events for whiteboard: {}", limit, boardId);
        
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "ts"));
        List<Event> events = eventRepository.findByWhiteboardIdOrderByTsAsc(boardId, pageable).getContent();
        
        return events.stream()
                .map(this::convertToEventEnvelope)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventEnvelope> getEventsForBoard(UUID boardId, int offset, int limit) {
        log.debug("Fetching {} events for whiteboard: {} starting from offset: {}", limit, boardId, offset);
        
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.ASC, "ts"));
        List<Event> events = eventRepository.findByWhiteboardIdOrderByTsAsc(boardId, pageable).getContent();
        
        return events.stream()
                .map(this::convertToEventEnvelope)
                .collect(Collectors.toList());
    }

    @Override
    public EventEnvelope getEventById(UUID eventId) {
        log.debug("Fetching event by ID: {}", eventId);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        return convertToEventEnvelope(event);
    }

    @Override
    public List<EventEnvelope> getEventsByType(UUID boardId, String eventType, int limit) {
        log.debug("Fetching {} events of type '{}' for whiteboard: {}", limit, eventType, boardId);
        
        Pageable pageable = PageRequest.of(0, limit);
        List<Event> events = eventRepository.findByWhiteboardIdAndEventTypeOrderByTsDesc(boardId, eventType, pageable).getContent();
        
        return events.stream()
                .map(this::convertToEventEnvelope)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteEventsForBoard(UUID boardId) {
        log.debug("Deleting all events for whiteboard: {}", boardId);
        
        long deletedCount = eventRepository.deleteByWhiteboardId(boardId);
        log.info("Deleted {} events for whiteboard: {}", deletedCount, boardId);
    }

    @Override
    public long getEventCountForBoard(UUID boardId) {
        return eventRepository.countByWhiteboardId(boardId);
    }

    /**
     * Convert Event entity to EventEnvelope
     */
    private EventEnvelope convertToEventEnvelope(Event event) {
        EventEnvelope envelope = new EventEnvelope();
        envelope.setId(event.getId().toString());
        envelope.setType(event.getEventType());
        envelope.setBoardId(event.getWhiteboard().getId().toString());
        envelope.setUserId(event.getUser().getId().toString());
        envelope.setTs(event.getTs());
        
        try {
            // Parse the JSON payload back to Object
            Object data = objectMapper.readValue(event.getPayload(), Object.class);
            envelope.setData(data);
        } catch (Exception e) {
            log.warn("Failed to parse event payload: {}", e.getMessage());
            envelope.setData(null);
        }
        
        return envelope;
    }
}
