package com.yaxcherg.smartsupportdashboard.service;

import com.yaxcherg.smartsupportdashboard.dto.TicketRequestDTO;
import com.yaxcherg.smartsupportdashboard.dto.TicketResponseDTO;
import com.yaxcherg.smartsupportdashboard.model.Ticket;
import com.yaxcherg.smartsupportdashboard.repository.TicketRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final GeminiAiService geminiAiService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public TicketService(TicketRepository ticketRepository, UserRepository userRepository, 
                         GeminiAiService geminiAiService, SimpMessagingTemplate messagingTemplate) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.geminiAiService = geminiAiService;
        this.messagingTemplate = messagingTemplate;
    }

    public TicketResponseDTO createTicket(TicketRequestDTO ticketDTO, AppUser createdBy) {
        Ticket ticket = new Ticket();
        ticket.setTitle(ticketDTO.getTitle());
        ticket.setDescription(ticketDTO.getDescription());
        ticket.setCustomerEmail(ticketDTO.getCustomerEmail());
        ticket.setStatus("ABIERTO");
        ticket.setCreatedBy(createdBy); // Asignar el creador del ticket

        Ticket savedTicket = ticketRepository.save(ticket);
        TicketResponseDTO responseDTO = mapToResponse(savedTicket);

        // NOTIFICAR AL FRONTEND VIA WEBSOCKETS (Nuevo ticket recibido)
        messagingTemplate.convertAndSend("/topic/tickets", savedTicket);

        // Llamar a la IA de forma asíncrona
        geminiAiService.analyzeAndSaveTicket(savedTicket.getId(), savedTicket.getDescription());

        return responseDTO;
    }
    public Page<TicketResponseDTO> getAllTickets(Pageable pageable, AppUser user) {
        // Lógica de roles: Si es ADMIN ve todo, si es USER solo lo suyo
        if (user.getRole().equals("ROLE_ADMIN")) {
            return ticketRepository.findAll(pageable).map(this::mapToResponse);
        } else {
            return ticketRepository.findByCreatedBy(user, pageable).map(this::mapToResponse);
        }
    }

    public Optional<TicketResponseDTO> getTicketById(Long id) {
        return ticketRepository.findById(id).map(this::mapToResponse);
    }

    public Optional<TicketResponseDTO> resolveTicket(Long id) {
        return ticketRepository.findById(id).map(ticket -> {
            ticket.setStatus("RESUELTO");
            return mapToResponse(ticketRepository.save(ticket));
        });
    }

    // NUEVO: Método para asignar un ticket a un agente
    public Optional<TicketResponseDTO> assignTicket(Long ticketId, String username) {
        return ticketRepository.findById(ticketId).flatMap(ticket -> 
            userRepository.findByUsername(username).map(user -> {
                ticket.setAssignedTo(user);
                ticket.setStatus("EN_PROGRESO");
                return mapToResponse(ticketRepository.save(ticket));
            })
        );
    }

    // NUEVO: Obtener estadísticas por categoría para las gráficas
    public Map<String, Long> getCategoryStats() {
        List<Ticket> allTickets = ticketRepository.findAll();
        return allTickets.stream()
                .filter(t -> t.getAiCategory() != null)
                .collect(Collectors.groupingBy(Ticket::getAiCategory, Collectors.counting()));
    }

    // NUEVO: Obtener estadísticas por prioridad
    public Map<String, Long> getPriorityStats() {
        List<Ticket> allTickets = ticketRepository.findAll();
        return allTickets.stream()
                .filter(t -> t.getAiPriority() != null)
                .collect(Collectors.groupingBy(Ticket::getAiPriority, Collectors.counting()));
    }

    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }

    private TicketResponseDTO mapToResponse(Ticket ticket) {
        TicketResponseDTO dto = new TicketResponseDTO();
        dto.setId(ticket.getId());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setCustomerEmail(ticket.getCustomerEmail());
        dto.setAiCategory(ticket.getAiCategory());
        dto.setAiPriority(ticket.getAiPriority());
        dto.setAiTone(ticket.getAiTone());
        dto.setAiSummary(ticket.getAiSummary());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setStatus(ticket.getStatus());
        
        if (ticket.getAssignedTo() != null) {
            dto.setAssignedToUsername(ticket.getAssignedTo().getUsername());
        }
        
        return dto;
    }

}