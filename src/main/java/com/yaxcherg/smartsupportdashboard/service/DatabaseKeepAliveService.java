package com.yaxcherg.smartsupportdashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DatabaseKeepAliveService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseKeepAliveService.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseKeepAliveService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Se ejecuta cada 24 horas (86400000 milisegundos)
     * para evitar que Supabase (versión gratuita) pause la base de datos por inactividad.
     */
    @Scheduled(fixedRate = 86400000)
    public void pingDatabase() {
        try {
            logger.info("📡 Ejecutando ping de mantenimiento para mantener Supabase activa...");
            jdbcTemplate.execute("SELECT 1");
            logger.info("✅ Ping a la base de datos completado con éxito.");
        } catch (Exception e) {
            logger.error("❌ Error al hacer ping a la base de datos: {}", e.getMessage());
        }
    }
}
