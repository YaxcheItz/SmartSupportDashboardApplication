package com.yaxcherg.smartsupportdashboard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    private final RestClient restClient;

    public SupabaseStorageService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String uploadFile(MultipartFile file) throws Exception {
        String bucketName = "ticket-attachments";
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String fileName = UUID.randomUUID().toString() + extension;
        String endpoint = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;

        String response = restClient.post()
                .uri(endpoint)
                .header("Authorization", "Bearer " + supabaseKey)
                .header("apikey", supabaseKey)
                .contentType(MediaType.parseMediaType(file.getContentType() != null ? file.getContentType() : "application/octet-stream"))
                .body(file.getBytes())
                .retrieve()
                .body(String.class);

        // Si llega aquí sin lanzar excepción, es porque fue exitoso
        return fileName;
    }

    public String getSignedUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) return null;
        if (fileName.startsWith("http")) return fileName;
        
        String bucketName = "ticket-attachments";
        String endpoint = supabaseUrl + "/storage/v1/object/sign/" + bucketName + "/" + fileName;

        try {
            Map<String, Object> requestBody = Map.of("expiresIn", 3600);
            
            Map response = restClient.post()
                    .uri(endpoint)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("apikey", supabaseKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("signedURL")) {
                String partialUrl = (String) response.get("signedURL");
                if (!partialUrl.startsWith("/storage/v1")) {
                    return supabaseUrl + "/storage/v1" + partialUrl;
                }
                return supabaseUrl + partialUrl;
            }
        } catch (Exception e) {
            System.err.println("No se pudo obtener URL firmada para " + fileName + ": " + e.getMessage());
        }
        return null;
    }
}
