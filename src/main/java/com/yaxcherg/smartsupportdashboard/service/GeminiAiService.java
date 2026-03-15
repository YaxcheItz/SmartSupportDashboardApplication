package com.yaxcherg.smartsupportdashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiAiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String analyzeTicket(String ticketDescription) {
        try {
            // Verificación de seguridad en logs (solo los primeros 5 caracteres)
            if (apiKey != null && apiKey.length() > 5) {
                System.out.println("Usando API Key que empieza por: " + apiKey.substring(0, 5));
            }

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

            // CONSTRUCCIÓN SEGURA DEL JSON (Sin errores de comillas o enters)
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
            System.err.println("Error al contactar con la IA: " + e.getMessage());
            return "{\"category\":\"Error\",\"priority\":\"Baja\",\"tone\":\"Neutral\",\"summary\":\"Error de conexión con IA\"}";
        }
    }
}
