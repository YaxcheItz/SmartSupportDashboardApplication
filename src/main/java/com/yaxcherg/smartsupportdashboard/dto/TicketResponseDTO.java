package com.yaxcherg.smartsupportdashboard.dto;

import lombok.Data;
import java.time.LocalDateTime;

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

    // Metadatos
    private LocalDateTime createdAt;
    private String status;
}