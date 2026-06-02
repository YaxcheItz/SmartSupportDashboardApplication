package com.yaxcherg.smartsupportdashboard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    private final RestTemplate restTemplate;

    public SupabaseStorageService() {
        this.restTemplate = new RestTemplate();
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

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);
        headers.set("Content-Type", file.getContentType() != null ? file.getContentType() : "application/octet-stream");

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            // Enterprise Fix: Devolver SOLO el nombre de archivo, no la URL pública.
            // Así la BD guarda 'c94f...jpg' y luego pedimos una URL firmada dinámicamente.
            return fileName;
        } else {
            throw new RuntimeException("Error al subir el archivo a Supabase: " + response.getBody());
        }
    }

    public String getSignedUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) return null;
        if (fileName.startsWith("http")) return fileName; // Retrocompatibilidad para archivos antiguos
        
        String bucketName = "ticket-attachments";
        String endpoint = supabaseUrl + "/storage/v1/object/sign/" + bucketName + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);
        headers.set("Content-Type", "application/json");

        // Solicitamos que el enlace expire en 1 hora (3600 segundos)
        String requestBody = "{\"expiresIn\": 3600}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Supabase devuelve {"signedURL": "/object/sign/..."}
                String partialUrl = (String) response.getBody().get("signedURL");
                
                // IMPORTANTE: Si la URL parcial no trae el prefijo /storage/v1, hay que añadirlo
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
