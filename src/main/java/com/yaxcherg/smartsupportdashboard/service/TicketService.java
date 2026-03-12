package com.yaxcherg.smartsupportdashboard.service;

import com.yaxcherg.smartsupportdashboard.model.Ticket;
import com.yaxcherg.smartsupportdashboard.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service // Le dice a Spring "¡Hey, yo soy el Chef! Inyéctame donde me necesiten."
public class TicketService {

    // 1. Llamamos al despensero
    private final TicketRepository ticketRepository;

    // 2. Inyección de Dependencias: Spring nos da automáticamente el repositorio aquí
    @Autowired
    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    // --- MÉTODOS DEL CHEF ---

    // Crear un nuevo ticket
    public Ticket createTicket(Ticket ticket) {
        // En el futuro: Aquí llamaremos a la IA antes de guardar
        // ticket.setAiCategory( ia.getCategoria(ticket.getDescription()) );
        return ticketRepository.save(ticket);
    }

    // Obtener todos los tickets
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    // Obtener un ticket por su ID
    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }
}