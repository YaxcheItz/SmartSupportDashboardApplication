package com.yaxcherg.smartsupportdashboard.service;

import com.yaxcherg.smartsupportdashboard.dto.TicketRequestDTO;
import com.yaxcherg.smartsupportdashboard.dto.TicketResponseDTO;
import com.yaxcherg.smartsupportdashboard.model.Ticket;
import com.yaxcherg.smartsupportdashboard.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        // 2. La IA analiza la descripción
        String aiResponseJson = geminiAiService.analyzeTicket(ticket.getDescription());

        try {
            tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
            tools.jackson.databind.JsonNode aiData = mapper.readTree(aiResponseJson);

            ticket.setAiCategory(aiData.path("category").asText("No detectada"));
            ticket.setAiPriority(aiData.path("priority").asText("Media"));
            ticket.setAiTone(aiData.path("tone").asText("Neutral"));
            ticket.setAiSummary(aiData.path("summary").asText("Sin resumen"));

        } catch (Exception e) {
            System.err.println("Error al leer el JSON de la IA: " + e.getMessage());
            ticket.setAiCategory("Error IA");
            ticket.setAiPriority("Error IA");
            ticket.setAiTone("Error IA");
            ticket.setAiSummary("Error IA");
        }

        // 3. Guardamos en la BD
        Ticket savedTicket = ticketRepository.save(ticket);

        // 4. Convertimos la Entidad guardada a un Response DTO para devolverlo
        return mapToResponse(savedTicket);
    }

    // Ahora devolvemos una lista de ResponseDTOs
    public List<TicketResponseDTO> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Ahora devolvemos un Optional de ResponseDTO
    public Optional<TicketResponseDTO> getTicketById(Long id) {
        return ticketRepository.findById(id)
                .map(this::mapToResponse);
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