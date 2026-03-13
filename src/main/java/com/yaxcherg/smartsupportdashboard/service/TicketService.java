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
        // 1. Antes de guardar, le mandamos la descripción a la IA
        String aiResponse = geminiAiService.analyzeTicket(ticket.getDescription());

        // 2. La IA nos devolverá algo como "Soporte Técnico,Alta"
        // Vamos a separar ese texto por la coma (",")
        try {
            String[] parts = aiResponse.split(",");
            if (parts.length >= 2) {
                ticket.setAiCategory(parts[0].trim()); // Posición 0: Categoría
                ticket.setAiPriority(parts[1].trim()); // Posición 1: Prioridad
            } else {
                // Si la IA respondió mal, ponemos algo por defecto
                ticket.setAiCategory("No detectada");
                ticket.setAiPriority("Media");
            }
        } catch (Exception e) {
            ticket.setAiCategory("Error IA");
            ticket.setAiPriority("Error IA");
        }

        // 3. ¡Ahora sí, guardamos en la base de datos con la info de la IA!
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }
}