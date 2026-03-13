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
            // 1. Construimos la URL completa con nuestra llave secreta
            String url = apiUrl + "?key=" + apiKey;

            // 2. Preparamos los Headers (Cabeceras)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 3. Creamos el Prompt (Las instrucciones para la IA)
            // Le pedimos que actúe como un experto y que SOLO nos devuelva la Categoría y Prioridad separadas por una coma.
            String prompt = "Eres un asistente experto en soporte técnico. Lee esta descripción de un problema de un cliente y clasifícalo.\n" +
                    "Descripción: '" + ticketDescription + "'\n" +
                    "Devuelve SOLO el resultado en este formato exacto: CATEGORIA,PRIORIDAD\n" +
                    "Ejemplo: Facturación,Alta\n" +
                    "Categorías posibles: Facturación, Soporte Técnico, Ventas, Queja, Otro.\n" +
                    "Prioridades posibles: Baja, Media, Alta, Urgente.";

            // 4. Construimos el JSON (el "Body") exactamente como Google lo exige
            String requestBody = "{"
                    + "\"contents\": [{"
                    + "\"parts\":[{\"text\": \"" + prompt + "\"}]"
                    + "}]"
                    + "}";

            // 5. Juntamos cabeceras y cuerpo
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            // 6. Hacemos la llamada (¡Aquí nos conectamos con Google!)
            String response = restTemplate.postForObject(url, requestEntity, String.class);

            // 7. Extraemos la respuesta del texto gigante que nos envía Google
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
            // Si algo falla (no hay internet, llave mala, etc.), devolvemos un valor por defecto
            System.err.println("Error al contactar con la IA: " + e.getMessage());
            return "Sin Categoría,No Asignada";
        }
    }
}