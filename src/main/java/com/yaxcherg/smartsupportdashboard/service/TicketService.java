package com.yaxcherg.smartsupportdashboard.service;

import com.yaxcherg.smartsupportdashboard.model.Ticket;
import com.yaxcherg.smartsupportdashboard.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final GeminiAiService geminiAiService; // ¡Añadimos a nuestro experto en IA!

    @Autowired
    public TicketService(TicketRepository ticketRepository, GeminiAiService geminiAiService) {
        this.ticketRepository = ticketRepository;
        this.geminiAiService = geminiAiService; // Lo inyectamos aquí
    }

    public Ticket createTicket(Ticket ticket) {
        // 1. La IA nos devuelve ahora un texto en formato JSON
        String aiResponseJson = geminiAiService.analyzeTicket(ticket.getDescription());

        try {
            // 2. Usamos Jackson (que ya viene con Spring) para convertir ese texto en un objeto que podamos leer
            tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
            tools.jackson.databind.JsonNode aiData = mapper.readTree(aiResponseJson);

            // 3. Asignamos los 4 valores leídos de la IA a nuestro Ticket
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

        // 4. Guardamos en la base de datos
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }
}