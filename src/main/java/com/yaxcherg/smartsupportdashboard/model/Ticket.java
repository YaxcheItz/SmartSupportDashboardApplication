package com.yaxcherg.smartsupportdashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity // Le dice a Spring que esta clase será una tabla en la base de datos
@Data // Magia de Lombok: Crea getters, setters y constructores automáticamente por detrás
@Table(name = "tickets") // Opcional, pero buena práctica para nombrar la tabla en plural
public class Ticket {

    @Id // Esta es la llave primaria (Primary Key)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincrementable (1, 2, 3...)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    private String customerEmail;

    // Estos campos los llenará la IA más adelante
    private String aiCategory;
    private String aiPriority;
    private String aiTone;
    @Column(length = 500)
    private String aiSummary;

    // Para saber cuándo se creó el ticket
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Estado del ticket: ABIERTO, EN_PROGRESO, CERRADO
    @Column(nullable = false)
    private String status = "ABIERTO";

    @ManyToOne
    @JoinColumn(name = "assigned_to_id")
    private AppUser assignedTo;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private AppUser createdBy;
}