package com.yaxcherg.smartsupportdashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_attachments")
@Data
@NoArgsConstructor
public class TicketAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String fileName; // Guardamos el path/nombre interno de Supabase

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    public TicketAttachment(String fileName, Ticket ticket) {
        this.fileName = fileName;
        this.ticket = ticket;
    }
}
