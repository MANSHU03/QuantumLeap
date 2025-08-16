package com.quantumleap.service.impl;

import com.quantumleap.dto.whiteboard.WhiteboardResponse;
import com.quantumleap.entity.User;
import com.quantumleap.entity.Whiteboard;
import com.quantumleap.entity.WhiteboardMember;
import com.quantumleap.repository.WhiteboardRepository;
import com.quantumleap.repository.WhiteboardMemberRepository;
import com.quantumleap.repository.UserRepository;
import com.quantumleap.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WhiteboardServiceImplTest {
    @Mock
    private WhiteboardRepository whiteboardRepository;
    @Mock
    private WhiteboardMemberRepository whiteboardMemberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private WhiteboardServiceImpl whiteboardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateWhiteboard_Success() {
        UUID userId = UUID.randomUUID();
        String boardName = "Test Board";
        User user = new User();
        user.setId(userId);
        user.setName("testuser");

        Whiteboard whiteboard = new Whiteboard();
        whiteboard.setId(UUID.randomUUID());
        whiteboard.setName(boardName);
        whiteboard.setOwner(user);
        whiteboard.setCreatedAt(OffsetDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(whiteboardRepository.save(any(Whiteboard.class))).thenReturn(whiteboard);
        when(whiteboardMemberRepository.save(any(WhiteboardMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WhiteboardResponse response = whiteboardService.createWhiteboard(boardName, userId);

        assertNotNull(response);
        assertEquals(boardName, response.getName());
        assertEquals("testuser", response.getOwnerName());
        assertTrue(response.isOwner());
        assertTrue(response.isPublic());

        verify(userRepository, times(1)).findById(userId);
        verify(whiteboardRepository, times(1)).save(any(Whiteboard.class));
        verify(whiteboardMemberRepository, times(1)).save(any(WhiteboardMember.class));
    }
}
