package com.yaxcherg.smartsupportdashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaxcherg.smartsupportdashboard.model.Ticket;
import com.yaxcherg.smartsupportdashboard.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiAiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiAiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String generateResponseSuggestion(String ticketTitle, String ticketDescription) {
        try {
            String url = apiUrl + "?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = "Eres un agente de soporte técnico experto. Genera una respuesta profesional, amable y concisa para un cliente que reportó este problema:\n" +
                    "Título: " + ticketTitle + "\n" +
                    "Descripción: " + ticketDescription + "\n\n" +
                    "Instrucciones:\n" +
                    "1. Saluda al cliente.\n" +
                    "2. Proporciona una posible solución o pasos a seguir.\n" +
                    "3. Mantén un tono empático.\n" +
                    "4. Máximo 100 palabras.\n" +
                    "Responde SOLO con el texto de la respuesta, sin etiquetas ni metadatos.";

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(url, requestEntity, String.class);
            JsonNode rootNode = objectMapper.readTree(response);

            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText()
                    .trim();

        } catch (Exception e) {
            return "Lo siento, no pude generar una sugerencia en este momento. Por favor, intenta redactar una respuesta manualmente.";
        }
    }

    @Async
    public void analyzeAndSaveTicket(Long ticketId, String ticketDescription) {
        try {
            String url = apiUrl + "?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = "Eres un asistente experto en soporte técnico. Lee esta descripción de un problema de un cliente: '" + ticketDescription + "'.\n" +
                    "Tu ÚNICO trabajo es responder con un objeto JSON válido, sin usar bloques de código Markdown, solo el texto JSON plano.\n" +
                    "Estructura:\n" +
                    "{\n" +
                    "  \"category\": \"(Facturación, Soporte Técnico, Ventas, Queja, Spam, Otro)\",\n" +
                    "  \"priority\": \"(Baja, Media, Alta, Urgente)\",\n" +
                    "  \"tone\": \"(Enojado, Frustrado, Preocupado, Neutral, Feliz)\",\n" +
                    "  \"summary\": \"(resumen en máximo 15 palabras)\"\n" +
                    "}";

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(url, requestEntity, String.class);
            JsonNode rootNode = objectMapper.readTree(response);

            String aiResponseRaw = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText()
                    .trim();

            JsonNode aiJson = objectMapper.readTree(aiResponseRaw);
            
            ticketRepository.findById(ticketId).ifPresent(ticket -> {
                ticket.setAiCategory(aiJson.path("category").asText("Otro"));
                ticket.setAiPriority(aiJson.path("priority").asText("Baja"));
                ticket.setAiTone(aiJson.path("tone").asText("Neutral"));
                ticket.setAiSummary(aiJson.path("summary").asText("Sin resumen"));
                Ticket savedTicket = ticketRepository.save(ticket);
                
                // NOTIFICAR AL FRONTEND VIA WEBSOCKETS
                messagingTemplate.convertAndSend("/topic/tickets", savedTicket);
            });

        } catch (Exception e) {
            System.err.println("Error al contactar con la IA: " + e.getMessage());
            ticketRepository.findById(ticketId).ifPresent(ticket -> {
                ticket.setAiCategory("Error");
                ticket.setAiSummary("Error de conexión con IA");
                Ticket savedTicket = ticketRepository.save(ticket);
                messagingTemplate.convertAndSend("/topic/tickets", savedTicket);
            });
        }
    }
}
