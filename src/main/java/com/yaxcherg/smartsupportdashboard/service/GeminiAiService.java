package com.yaxcherg.smartsupportdashboard.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiAiService {

    // Leemos las variables que pusiste en application.properties
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    // RestTemplate es el "Postman" interno de Spring Boot
    private final RestTemplate restTemplate;
    // ObjectMapper nos ayuda a leer el JSON que nos devuelva la IA
    private final ObjectMapper objectMapper;

    public GeminiAiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // Este es el método principal que usaremos
    public String analyzeTicket(String ticketDescription) {
        try {
            String url = apiUrl + "?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // EL NUEVO PROMPT: Le pedimos a la IA que actúe como una API y nos devuelva un JSON
            String prompt = "Eres un asistente experto en soporte técnico. Lee esta descripción de un problema de un cliente: '" + ticketDescription + "'.\n" +
                    "Tu ÚNICO trabajo es responder con un objeto JSON válido, sin usar bloques de código Markdown (```json...), solo el texto JSON plano.\n" +
                    "El JSON DEBE tener exactamente esta estructura y claves:\n" +
                    "{\n" +
                    "  \"category\": \"(elige entre: Facturación, Soporte Técnico, Ventas, Queja, Spam, Otro)\",\n" +
                    "  \"priority\": \"(elige entre: Baja, Media, Alta, Urgente)\",\n" +
                    "  \"tone\": \"(elige entre: Enojado, Frustrado, Preocupado, Neutral, Feliz)\",\n" +
                    "  \"summary\": \"(escribe un resumen del problema en máximo 15 palabras)\"\n" +
                    "}\n" +
                    "Si el texto no tiene sentido (ej. 'asdasd'), pon la categoría 'Spam', prioridad 'Baja' y tono 'Neutral'.";

            // Aseguramos que las comillas dobles del prompt se escapen correctamente para el JSON final
            String escapedPrompt = prompt.replace("\"", "\\\"");

            String requestBody = "{"
                    + "\"contents\": [{"
                    + "\"parts\":[{\"text\": \"" + escapedPrompt + "\"}]"
                    + "}]"
                    + "}";

            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(url, requestEntity, String.class);

            JsonNode rootNode = objectMapper.readTree(response);

            // La IA nos devuelve un texto que (por nuestras instrucciones) es un JSON
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
            // Si hay error, devolvemos un JSON por defecto para que no explote el programa
            return "{\"category\":\"Error\",\"priority\":\"Baja\",\"tone\":\"Neutral\",\"summary\":\"Error al contactar IA\"}";
        }
    }
}