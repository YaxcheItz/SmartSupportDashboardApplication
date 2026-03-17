package com.yaxcherg.smartsupportdashboard.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentResponseDTO {
    private Long id;
    private String content;
    private String authorUsername;
    private String authorRole;
    private LocalDateTime createdAt;
}
