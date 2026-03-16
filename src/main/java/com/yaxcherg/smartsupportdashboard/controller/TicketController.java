package com.yaxcherg.smartsupportdashboard.controller;

import com.yaxcherg.smartsupportdashboard.dto.TicketRequestDTO;
import com.yaxcherg.smartsupportdashboard.dto.TicketResponseDTO;
import com.yaxcherg.smartsupportdashboard.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
   public ResponseEntity<TicketResponseDTO> createTicket(@Valid @RequestBody TicketRequestDTO requestDTO) {
        TicketResponseDTO newTicket = ticketService.createTicket(requestDTO);
            return new ResponseEntity<>(newTicket, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<TicketResponseDTO>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Creamos la petición de paginación, ordenando por fecha de creación descendente (los más nuevos primero)
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<TicketResponseDTO> ticketsPage = ticketService.getAllTickets(pageRequest);

        return new ResponseEntity<>(ticketsPage, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable Long id) {
        Optional<TicketResponseDTO> ticket = ticketService.getTicketById(id);

        return ticket
                .map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Nuevo endpoint para marcar un ticket como resuelto
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<TicketResponseDTO> resolveTicket(@PathVariable Long id) {

        Optional<TicketResponseDTO> resolvedTicket = ticketService.resolveTicket(id);

        return resolvedTicket
                .map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // NUEVO: Endpoint para eliminar un ticket
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}