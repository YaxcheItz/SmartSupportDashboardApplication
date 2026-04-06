package com.yaxcherg.smartsupportdashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * Endpoint ligero para mantener activo el servidor en Render.
     * Un servicio externo (como UptimeRobot o cron-job.org) puede hacer
     * peticiones a este endpoint cada 14 minutos.
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "message", "El servidor de Smart Support Dashboard está activo."
        ));
    }
}
