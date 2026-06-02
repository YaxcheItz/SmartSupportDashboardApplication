package com.yaxcherg.smartsupportdashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaxcherg.smartsupportdashboard.model.Faq;
import com.yaxcherg.smartsupportdashboard.model.Ticket;
import com.yaxcherg.smartsupportdashboard.repository.FaqRepository;
import com.yaxcherg.smartsupportdashboard.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
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

    private final TicketRepository ticketRepository;
    private final FaqRepository faqRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final TicketService ticketService;

    @Autowired
    public GeminiAiService(TicketRepository ticketRepository, 
                          FaqRepository faqRepository, 
                          SimpMessagingTemplate messagingTemplate,
                          RestClient.Builder restClientBuilder,
                          @org.springframework.context.annotation.Lazy TicketService ticketService) {
        this.ticketRepository = ticketRepository;
        this.faqRepository = faqRepository;
        this.messagingTemplate = messagingTemplate;
        this.restClient = restClientBuilder.build();
        this.objectMapper = new ObjectMapper();
        this.ticketService = ticketService;
    }

    public String generateResponseSuggestion(String ticketTitle, String ticketDescription) {
        try {
            String url = apiUrl + "?key=" + apiKey;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON); 

            String prompt = "Eres un agente de soporte técnico experto. Genera una respuesta profesional para un cliente:\n" +
                    "Título: " + ticketTitle + "\n" +
                    "Descripción: " + ticketDescription + "\n\n" +
                    "Responde SOLO con el texto de la respuesta.";

            return callGemini(url, headers, prompt);
        } catch (Exception e) {
            return "No pude generar una sugerencia en este momento.";
        }
    }

    public String getQuickSolution(String issueTitle) {
        try {
            List<Faq> faqs = faqRepository.findAll();
            StringBuilder knowledgeBase = new StringBuilder("Base de Conocimiento SSD:\n");
            for (Faq faq : faqs) {
                knowledgeBase.append("- P: ").append(faq.getQuestion()).append("\n  R: ").append(faq.getAnswer()).append("\n");
            }

            String url = apiUrl + "?key=" + apiKey;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = "Usando la Base de Conocimiento:\n" + knowledgeBase.toString() + "\n" +
                    "El usuario pregunta: '" + issueTitle + "'.\n" +
                    "Si hay solución en la base, dila brevemente. Si no, responde 'REQUIRES_HUMAN'.";

            String suggestion = callGemini(url, headers, prompt);

            if ("REQUIRES_HUMAN".equalsIgnoreCase(suggestion)) return null;
            return suggestion;
        } catch (Exception e) {
            return null;
        }
    }

    @Async
    public void analyzeAndSaveTicket(Long ticketId, String ticketDescription) {
        try {
            String url = apiUrl + "?key=" + apiKey;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = "Analiza este problema: '" + ticketDescription + "'. Responde SOLO JSON plano:\n" +
                    "{\"category\": \"...\", \"priority\": \"...\", \"tone\": \"...\", \"summary\": \"...\"}";

            String aiResponseRaw = callGemini(url, headers, prompt);
            JsonNode aiJson = objectMapper.readTree(aiResponseRaw);
            
            ticketRepository.findById(ticketId).ifPresent(ticket -> {
                ticket.setAiCategory(aiJson.path("category").asText("Otro"));
                ticket.setAiPriority(aiJson.path("priority").asText("Baja"));
                ticket.setAiTone(aiJson.path("tone").asText("Neutral"));
                ticket.setAiSummary(aiJson.path("summary").asText("Sin resumen"));
                Ticket savedTicket = ticketRepository.save(ticket);
                
                // NOTIFICAR AL FRONTEND VIA WEBSOCKETS USANDO DTO
                messagingTemplate.convertAndSend("/topic/tickets", ticketService.mapToResponse(savedTicket));
            });
        } catch (Exception e) {
            ticketRepository.findById(ticketId).ifPresent(ticket -> {
                ticket.setAiCategory("Error");
                Ticket savedTicket = ticketRepository.save(ticket);
                messagingTemplate.convertAndSend("/topic/tickets", ticketService.mapToResponse(savedTicket));
            });
        }
    }

    private String callGemini(String url, HttpHeaders headers, String prompt) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();  
        Map<String, Object> content = new HashMap<>();      
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        content.put("parts", List.of(part));
        requestBody.put("contents", List.of(content));      

        String response = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode rootNode = objectMapper.readTree(response);
        return rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText().trim();
    }
}
