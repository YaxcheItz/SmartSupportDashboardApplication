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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // ... (keep current methods)

    // NUEVO: Endpoint para eliminar un ticket
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // NUEVO: Asignar un ticket al usuario actual
    @PatchMapping("/{id}/assign/{username}")
    public ResponseEntity<TicketResponseDTO> assignTicket(@PathVariable Long id, @PathVariable String username) {
        return ticketService.assignTicket(id, username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // NUEVO: Obtener estadísticas de categorías para Chart.js
    @GetMapping("/stats/categories")
    public ResponseEntity<Map<String, Long>> getCategoryStats() {
        return ResponseEntity.ok(ticketService.getCategoryStats());
    }
}