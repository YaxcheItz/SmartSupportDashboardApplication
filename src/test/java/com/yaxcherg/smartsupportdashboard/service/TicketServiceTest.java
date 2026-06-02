package com.yaxcherg.smartsupportdashboard.service;

import com.yaxcherg.smartsupportdashboard.dto.TicketRequestDTO;
import com.yaxcherg.smartsupportdashboard.dto.TicketResponseDTO;
import com.yaxcherg.smartsupportdashboard.model.AppUser;
import com.yaxcherg.smartsupportdashboard.model.Ticket;
import com.yaxcherg.smartsupportdashboard.model.enums.TicketStatus;
import com.yaxcherg.smartsupportdashboard.repository.TicketRepository;
import com.yaxcherg.smartsupportdashboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GeminiAiService geminiAiService;

    @Mock
    private EmailService emailService;
    
    @Mock
    private SupabaseStorageService storageService;

    @InjectMocks
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTicket_ShouldReturnTicketResponseDTO() {
        // Arrange
        TicketRequestDTO requestDTO = new TicketRequestDTO();
        requestDTO.setTitle("Test Title");
        requestDTO.setDescription("Test Description");
        
        AppUser user = new AppUser();
        user.setUsername("testuser");

        Ticket savedTicket = new Ticket();
        savedTicket.setId(1L);
        savedTicket.setTitle("Test Title");
        savedTicket.setDescription("Test Description");
        savedTicket.setCreatedBy(user);
        savedTicket.setStatus(TicketStatus.ABIERTO);

        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        // Act
        TicketResponseDTO responseDTO = ticketService.createTicket(requestDTO, user);

        // Assert
        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals("ABIERTO", responseDTO.getStatus());
        verify(geminiAiService, times(1)).analyzeAndSaveTicket(eq(1L), anyString());
    }

    @Test
    void resolveTicket_ShouldUpdateStatusAndSendEmail() {
        // Arrange
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setCustomerEmail("client@example.com");
        ticket.setTitle("Issue");
        ticket.setStatus(TicketStatus.ABIERTO);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        // Act
        Optional<TicketResponseDTO> response = ticketService.resolveTicket(1L);

        // Assert
        assertTrue(response.isPresent());
        assertEquals("RESUELTO", response.get().getStatus());
        verify(emailService, times(1)).sendResolutionEmail(eq("client@example.com"), eq("Issue"));
    }
}
