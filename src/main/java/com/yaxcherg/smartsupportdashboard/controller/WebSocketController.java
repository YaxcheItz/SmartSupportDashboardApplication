package com.yaxcherg.smartsupportdashboard.controller;

import com.yaxcherg.smartsupportdashboard.dto.TypingIndicatorDTO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/ticket/{ticketId}/typing")
    public void handleTypingIndicator(@DestinationVariable Long ticketId, @Payload TypingIndicatorDTO indicator) {
        // Retransmite el evento de escritura solo a los suscriptores de este ticket en específico
        messagingTemplate.convertAndSend("/topic/ticket/" + ticketId + "/typing", indicator);
    }
}
