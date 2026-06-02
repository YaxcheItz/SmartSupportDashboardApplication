package com.yaxcherg.smartsupportdashboard.controller;

import com.yaxcherg.smartsupportdashboard.dto.TicketRequestDTO;
import com.yaxcherg.smartsupportdashboard.dto.TicketResponseDTO;
import com.yaxcherg.smartsupportdashboard.dto.CommentRequestDTO;
import com.yaxcherg.smartsupportdashboard.dto.CommentResponseDTO;
import com.yaxcherg.smartsupportdashboard.service.CommentService;
import com.yaxcherg.smartsupportdashboard.model.AppUser;
import com.yaxcherg.smartsupportdashboard.repository.UserRepository;
import com.yaxcherg.smartsupportdashboard.service.TicketService;
import com.yaxcherg.smartsupportdashboard.service.GeminiAiService;
import com.yaxcherg.smartsupportdashboard.service.SupabaseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UserRepository userRepository;
    private final GeminiAiService geminiAiService;
    private final CommentService commentService;
    private final SupabaseStorageService storageService;

    @Autowired
    public TicketController(TicketService ticketService, UserRepository userRepository, 
                            GeminiAiService geminiAiService, CommentService commentService,
                            SupabaseStorageService storageService) {
        this.ticketService = ticketService;
        this.userRepository = userRepository;
        this.geminiAiService = geminiAiService;
        this.commentService = commentService;
        this.storageService = storageService;
    }

    // --- ENDPOINT PARA SUBIR ARCHIVOS ---
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String url = storageService.uploadFile(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // --- ENDPOINTS DE COMENTARIOS ---

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponseDTO>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentsByTicketId(id));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponseDTO> addComment(@PathVariable Long id, @Valid @RequestBody CommentRequestDTO dto) {
        AppUser user = getCurrentUser();
        return new ResponseEntity<>(commentService.addComment(id, dto, user), HttpStatus.CREATED);
    }

    // --- ENDPOINTS DE INTELIGENCIA ARTIFICIAL ---

    @GetMapping("/prevent")
    public ResponseEntity<Map<String, String>> preventTicket(@RequestParam String title) {
        String suggestion = geminiAiService.getQuickSolution(title);
        if (suggestion != null) {
            return ResponseEntity.ok(Map.of("suggestion", suggestion));
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/suggest-response")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Map<String, String>> suggestResponse(@PathVariable Long id) {
        return ticketService.getTicketById(id).map(ticket -> {
            String suggestion = geminiAiService.generateResponseSuggestion(ticket.getTitle(), ticket.getDescription());
            return ResponseEntity.ok(Map.of("suggestion", suggestion));
        }).orElse(ResponseEntity.notFound().build());
    }

    private AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
    }

    @PostMapping
    public ResponseEntity<TicketResponseDTO> createTicket(@Valid @RequestBody TicketRequestDTO ticketDTO) {
        AppUser user = getCurrentUser();
        return new ResponseEntity<>(ticketService.createTicket(ticketDTO, user), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<TicketResponseDTO>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category) {
        AppUser user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ticketService.getAllTickets(pageable, user, title, status, priority, category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable Long id) {
        return ticketService.getTicketById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<TicketResponseDTO> resolveTicket(@PathVariable Long id) {
        return ticketService.resolveTicket(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

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