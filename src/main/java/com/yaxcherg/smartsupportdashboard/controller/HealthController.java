package com.yaxcherg.smartsupportdashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Endpoint para mantener activo el servidor en Render y la base de datos en Supabase.
     * Ejecuta una consulta ultraligera (SELECT 1) para registrar actividad en PostgreSQL.
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        boolean dbStatus = false;
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            dbStatus = (result != null && result == 1);
        } catch (Exception e) {
            dbStatus = false;
        }

        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "database", dbStatus ? "CONNECTED" : "UNAVAILABLE",
            "message", "Smart Support Dashboard y Supabase activos."
        ));
    }
}

