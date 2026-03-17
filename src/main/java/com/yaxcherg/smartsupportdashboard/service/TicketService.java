package com.yaxcherg.smartsupportdashboard.service;

import com.yaxcherg.smartsupportdashboard.dto.TicketRequestDTO;
import com.yaxcherg.smartsupportdashboard.dto.TicketResponseDTO;
import com.yaxcherg.smartsupportdashboard.model.Ticket;
import com.yaxcherg.smartsupportdashboard.repository.TicketRepository;
import com.yaxcherg.smartsupportdashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public TicketService(TicketRepository ticketRepository, UserRepository userRepository, GeminiAiService geminiAiService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.geminiAiService = geminiAiService;
    }

    public TicketResponseDTO createTicket(TicketRequestDTO ticketDTO, AppUser createdBy) {
        Ticket ticket = new Ticket();
        ticket.setTitle(ticketDTO.getTitle());
        ticket.setDescription(ticketDTO.getDescription());
        ticket.setCustomerEmail(ticketDTO.getCustomerEmail());
        ticket.setStatus("ABIERTO");
        ticket.setCreatedBy(createdBy); // Asignar el creador del ticket
        
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Llamar a la IA de forma asíncrona
        geminiAiService.analyzeAndSaveTicket(savedTicket.getId(), savedTicket.getDescription());
        
        return mapToResponse(savedTicket);
    }

    public Page<TicketResponseDTO> getAllTickets(Pageable pageable, AppUser user, 
                                               String title, String status, 
                                               String priority, String category) {
        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filtrado por rol (Seguridad)
            if (!user.getRole().equals("ROLE_ADMIN")) {
                predicates.add(cb.equal(root.get("createdBy"), user));
            }

            // 2. Filtros opcionales
            if (title != null && !title.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null && !priority.isEmpty()) {
                predicates.add(cb.equal(root.get("aiPriority"), priority));
            }
            if (category != null && !category.isEmpty()) {
                predicates.add(cb.equal(root.get("aiCategory"), category));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return ticketRepository.findAll(spec, pageable).map(this::mapToResponse);
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