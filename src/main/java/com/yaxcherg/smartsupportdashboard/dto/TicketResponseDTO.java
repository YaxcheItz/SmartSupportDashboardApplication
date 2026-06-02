package com.yaxcherg.smartsupportdashboard.dto;

import lombok.Data;
import java.time.LocalDateTime;

import java.util.List;

@Data
public class TicketResponseDTO {

    private Long id;
    private String title;
    private String description;
    private String customerEmail;

    // Datos de la IA
    private String aiCategory;
    private String aiPriority;
    private String aiTone;
    private String aiSummary;
    private List<String> attachmentUrls;

    // Metadatos
    private LocalDateTime createdAt;
    private String status;
    private String assignedToUsername;
}