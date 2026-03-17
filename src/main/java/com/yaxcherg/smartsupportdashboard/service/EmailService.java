package com.yaxcherg.smartsupportdashboard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    private final RestTemplate restTemplate;

    public EmailService() {
        this.restTemplate = new RestTemplate();
    }

    @Async
    public void sendResolutionEmail(String toEmail, String ticketTitle) {
        try {
            String url = "https://api.resend.com/emails";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);

            String htmlContent = "<html><body style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>" +
                    "<div style='max-width: 600px; margin: 0 auto; border: 1px solid #eee; padding: 20px; border-radius: 10px;'>" +
                    "<h2 style='color: #2563eb;'>¡Tu ticket ha sido resuelto!</h2>" +
                    "<p>Hola,</p>" +
                    "<p>Nos alegra informarte que nuestro equipo de soporte ha marcado tu ticket como <strong>RESUELTO</strong>.</p>" +
                    "<div style='background: #f9fafb; padding: 15px; border-radius: 8px; margin: 20px 0;'>" +
                    "<strong>Asunto:</strong> " + ticketTitle +
                    "</div>" +
                    "<p>Puedes entrar a tu portal para ver los detalles o dejar un comentario si necesitas más ayuda.</p>" +
                    "<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>" +
                    "<p style='font-size: 0.8rem; color: #666;'>Este es un mensaje automático de Smart Support Dashboard.</p>" +
                    "</div></body></html>";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("from", "Smart Support <" + fromEmail + ">");
            requestBody.put("to", toEmail);
            requestBody.put("subject", "✅ Ticket Resuelto: " + ticketTitle);
            requestBody.put("html", htmlContent);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            restTemplate.postForObject(url, requestEntity, String.class);

        } catch (Exception e) {
            System.err.println("Error al enviar correo vía Resend: " + e.getMessage());
        }
    }
}
