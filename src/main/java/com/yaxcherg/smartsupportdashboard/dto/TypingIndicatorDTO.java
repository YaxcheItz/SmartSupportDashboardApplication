package com.yaxcherg.smartsupportdashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypingIndicatorDTO {
    private Long ticketId;
    private String username;
    private boolean isTyping;
}
