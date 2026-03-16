package com.yaxcherg.smartsupportdashboard.service;

import com.yaxcherg.smartsupportdashboard.dto.TicketRequestDTO;
import com.yaxcherg.smartsupportdashboard.dto.TicketResponseDTO;
import com.yaxcherg.smartsupportdashboard.model.Ticket;
import com.yaxcherg.smartsupportdashboard.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final GeminiAiService geminiAiService;

    @Autowired
    public TicketService(TicketRepository ticketRepository, GeminiAiService geminiAiService) {
        this.ticketRepository = ticketRepository;
        this.geminiAiService = geminiAiService;
    }

    // Ahora recibimos un RequestDTO y devolvemos un ResponseDTO
    public TicketResponseDTO createTicket(TicketRequestDTO request) {

        // 1. Convertimos el Request DTO a nuestra Entidad Ticket (BD)
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCustomerEmail(request.getCustomerEmail());
        
        // 2. Establecemos valores por defecto mientras la IA analiza
        ticket.setAiCategory("Analizando...");
        ticket.setAiPriority("Media");
        ticket.setAiTone("Neutral");
        ticket.setAiSummary("La IA está analizando este ticket...");

        // 3. Guardamos en la BD inmediatamente
        Ticket savedTicket = ticketRepository.save(ticket);

        // 4. Disparamos el análisis de IA de forma ASÍNCRONA (en segundo plano)
        geminiAiService.analyzeAndSaveTicket(savedTicket.getId(), savedTicket.getDescription());

        // 5. Convertimos la Entidad guardada a un Response DTO para devolverlo
        return mapToResponse(savedTicket);
    }

    // Ahora devolvemos una lista de ResponseDTOs
    // Nuevo método con paginación
    public Page<TicketResponseDTO> getAllTickets(Pageable pageable) {

        // findAll(pageable) devuelve un Page<Ticket>, y usamos .map() para convertir cada Ticket a DTO
        return ticketRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    // Ahora devolvemos un Optional de ResponseDTO
    public Optional<TicketResponseDTO> getTicketById(Long id) {
        return ticketRepository.findById(id)
                .map(this::mapToResponse);
    }

    // Nuevo método para marcar un ticket como resuelto
    public Optional<TicketResponseDTO> resolveTicket(Long id) {

        return ticketRepository.findById(id).map(ticket -> {

            ticket.setStatus("CERRADO");

            Ticket updatedTicket = ticketRepository.save(ticket);

            return mapToResponse(updatedTicket);
        });
    }

    // NUEVO: Método para eliminar un ticket
    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }

    // Método auxiliar (Mapeo manual de Entidad a DTO)
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
        return dto;
    }

}