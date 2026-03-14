package com.yaxcherg.smartsupportdashboard.controller;

import com.yaxcherg.smartsupportdashboard.dto.TicketRequestDTO;
import com.yaxcherg.smartsupportdashboard.dto.TicketResponseDTO;
import com.yaxcherg.smartsupportdashboard.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
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
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets() {
        List<TicketResponseDTO> tickets = ticketService.getAllTickets();
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable Long id) {
        Optional<TicketResponseDTO> ticket = ticketService.getTicketById(id);

        return ticket
                .map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}