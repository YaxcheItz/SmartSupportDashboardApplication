package com.yaxcherg.smartsupportdashboard.controller;

import com.yaxcherg.smartsupportdashboard.model.Ticket;
import com.yaxcherg.smartsupportdashboard.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController // Le dice a Spring que esta clase recibirá peticiones HTTP y devolverá JSON
@RequestMapping("/api/tickets") // La URL base para todos los métodos de esta clase
@CrossOrigin(origins = "*") // Permite que un frontend (como Angular o React) se conecte sin bloqueos de seguridad
public class TicketController {

    // El mesero necesita comunicarse con el chef
    private final TicketService ticketService;

    @Autowired // Inyectamos el servicio
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // ENDPOINT 1: Crear un ticket (Recibe un POST)
    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket) {
        // @RequestBody agarra el JSON que llega de internet y lo convierte en un objeto Java "Ticket"
        Ticket newTicket = ticketService.createTicket(ticket);
        return new ResponseEntity<>(newTicket, HttpStatus.CREATED); // Devuelve código 201 (Creado)
    }

    // ENDPOINT 2: Obtener todos los tickets (Recibe un GET)
    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        List<Ticket> tickets = ticketService.getAllTickets();
        return new ResponseEntity<>(tickets, HttpStatus.OK); // Devuelve código 200 (OK)
    }

    // ENDPOINT 3: Obtener un ticket específico por su ID (Recibe un GET a /api/tickets/{id})
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        // @PathVariable extrae el número "id" de la URL
        Optional<Ticket> ticket = ticketService.getTicketById(id);

        // Si el ticket existe, lo devolvemos con un 200 OK. Si no, devolvemos un 404 Not Found.
        return ticket.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}