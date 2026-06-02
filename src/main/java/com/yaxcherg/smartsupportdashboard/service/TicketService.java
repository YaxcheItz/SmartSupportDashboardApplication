package com.yaxcherg.smartsupportdashboard.service;

import com.yaxcherg.smartsupportdashboard.dto.TicketRequestDTO;
import com.yaxcherg.smartsupportdashboard.dto.TicketResponseDTO;
import com.yaxcherg.smartsupportdashboard.model.AppUser;
import com.yaxcherg.smartsupportdashboard.model.Ticket;
import com.yaxcherg.smartsupportdashboard.repository.TicketRepository;
import com.yaxcherg.smartsupportdashboard.repository.UserRepository;
import com.yaxcherg.smartsupportdashboard.model.enums.TicketStatus;
import com.yaxcherg.smartsupportdashboard.model.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final EmailService emailService;
    private final SupabaseStorageService storageService;

    @Autowired
    public TicketService(TicketRepository ticketRepository, UserRepository userRepository, 
                         GeminiAiService geminiAiService, EmailService emailService,
                         SupabaseStorageService storageService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.geminiAiService = geminiAiService;
        this.emailService = emailService;
        this.storageService = storageService;
    }

    @CacheEvict(value = "categoryStats", allEntries = true)
    public TicketResponseDTO createTicket(TicketRequestDTO ticketDTO, AppUser createdBy) {
        Ticket ticket = new Ticket();
        ticket.setTitle(ticketDTO.getTitle());
        ticket.setDescription(ticketDTO.getDescription());
        ticket.setCustomerEmail(ticketDTO.getCustomerEmail());
        ticket.setStatus(TicketStatus.ABIERTO);
        ticket.setCreatedBy(createdBy);

        if (ticketDTO.getAttachmentFileNames() != null) {
            for (String fileName : ticketDTO.getAttachmentFileNames()) {
                ticket.addAttachment(fileName);
            }
        }
        
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
            if (user.getRole() != UserRole.ROLE_ADMIN && user.getRole() != UserRole.ROLE_EMPLOYEE) {
                predicates.add(cb.equal(root.get("createdBy"), user));
            }

            // 2. Filtros opcionales
            if (title != null && !title.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            if (status != null && !status.isEmpty()) {
                try {
                    predicates.add(cb.equal(root.get("status"), TicketStatus.valueOf(status.toUpperCase())));
                } catch(IllegalArgumentException e){}
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

    @CacheEvict(value = "categoryStats", allEntries = true)
    public Optional<TicketResponseDTO> resolveTicket(Long id) {
        return ticketRepository.findById(id).map(ticket -> {
            ticket.setStatus(TicketStatus.RESUELTO);
            Ticket savedTicket = ticketRepository.save(ticket);
            
            // ENVIAR CORREO DE RESOLUCIÓN AL CLIENTE
            emailService.sendResolutionEmail(savedTicket.getCustomerEmail(), savedTicket.getTitle());
            
            return mapToResponse(savedTicket);
        });
    }

    // NUEVO: Método para asignar un ticket a un agente
    @CacheEvict(value = "categoryStats", allEntries = true)
    public Optional<TicketResponseDTO> assignTicket(Long ticketId, String username) {
        return ticketRepository.findById(ticketId).flatMap(ticket -> 
            userRepository.findByUsername(username).map(user -> {
                ticket.setAssignedTo(user);
                ticket.setStatus(TicketStatus.EN_PROGRESO);
                return mapToResponse(ticketRepository.save(ticket));
            })
        );
    }

    // OPTIMIZADO: Obtener estadísticas por categoría directamente desde la DB con Caché
    @Cacheable(value = "categoryStats")
    public Map<String, Long> getCategoryStats() {
        List<Object[]> results = ticketRepository.countTicketsByCategory();
        Map<String, Long> stats = new HashMap<>();
        for (Object[] result : results) {
            stats.put((String) result[0], (Long) result[1]);
        }
        return stats;
    }

    @CacheEvict(value = "categoryStats", allEntries = true)
    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }

    public TicketResponseDTO mapToResponse(Ticket ticket) {
        TicketResponseDTO dto = new TicketResponseDTO();
        dto.setId(ticket.getId());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setCustomerEmail(ticket.getCustomerEmail());
        dto.setAiCategory(ticket.getAiCategory());
        dto.setAiPriority(ticket.getAiPriority());
        dto.setAiTone(ticket.getAiTone());
        dto.setAiSummary(ticket.getAiSummary());
        
        // Convertir todos los adjuntos a URLs firmadas
        List<String> signedUrls = new ArrayList<>();
        if (ticket.getAttachments() != null && !ticket.getAttachments().isEmpty()) {
            for (com.yaxcherg.smartsupportdashboard.model.TicketAttachment att : ticket.getAttachments()) {
                String signed = storageService.getSignedUrl(att.getFileName());
                if (signed != null) signedUrls.add(signed);
            }
        }
        dto.setAttachmentUrls(signedUrls);

        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setStatus(ticket.getStatus() != null ? ticket.getStatus().name() : null);
        
        if (ticket.getAssignedTo() != null) {
            dto.setAssignedToUsername(ticket.getAssignedTo().getUsername());
        }
        
        return dto;
    }

}