package com.yaxcherg.smartsupportdashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity 
@Data 
@Table(name = "tickets")
public class Ticket {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    private String customerEmail;

    private String aiCategory;
    private String aiPriority;
    private String aiTone;
    @Column(length = 500)
    private String aiSummary;

    @Column(length = 1000)
    private String attachmentUrl;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketAttachment> attachments = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.yaxcherg.smartsupportdashboard.model.enums.TicketStatus status = com.yaxcherg.smartsupportdashboard.model.enums.TicketStatus.ABIERTO;

    @ManyToOne
    @JoinColumn(name = "assigned_to_id")
    private AppUser assignedTo;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private AppUser createdBy;

    public void addAttachment(String fileName) {
        attachments.add(new TicketAttachment(fileName, this));
    }
}
