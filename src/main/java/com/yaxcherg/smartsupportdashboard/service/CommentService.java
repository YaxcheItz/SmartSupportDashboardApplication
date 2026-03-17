package com.yaxcherg.smartsupportdashboard.service;

import com.yaxcherg.smartsupportdashboard.dto.CommentRequestDTO;
import com.yaxcherg.smartsupportdashboard.dto.CommentResponseDTO;
import com.yaxcherg.smartsupportdashboard.model.AppUser;
import com.yaxcherg.smartsupportdashboard.model.Ticket;
import com.yaxcherg.smartsupportdashboard.model.TicketComment;
import com.yaxcherg.smartsupportdashboard.repository.CommentRepository;
import com.yaxcherg.smartsupportdashboard.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, TicketRepository ticketRepository) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
    }

    public List<CommentResponseDTO> getCommentsByTicketId(Long ticketId) {
        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CommentResponseDTO addComment(Long ticketId, CommentRequestDTO dto, AppUser author) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        TicketComment comment = new TicketComment();
        comment.setContent(dto.getContent());
        comment.setTicket(ticket);
        comment.setAuthor(author);

        TicketComment savedComment = commentRepository.save(comment);
        return mapToResponse(savedComment);
    }

    private CommentResponseDTO mapToResponse(TicketComment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setAuthorUsername(comment.getAuthor().getUsername());
        dto.setAuthorRole(comment.getAuthor().getRole());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}
