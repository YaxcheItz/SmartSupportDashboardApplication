package com.yaxcherg.smartsupportdashboard.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
public class CacheConfig {

    // Intenta usar Redis como caché principal, si falla usa memoria
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        try {
            // Verifica si hay conexión a Redis
            redisConnectionFactory.getConnection().close();
            
            RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(10)) // Tiempo de vida de la caché: 10 minutos
                    .disableCachingNullValues();

            return RedisCacheManager.builder(redisConnectionFactory)
                    .cacheDefaults(cacheConfiguration)
                    .build();
        } catch (Exception e) {
            // Si Redis falla (ej. en Render si no está configurado), usa caché en memoria
            System.out.println("⚠️ No se pudo conectar a Redis. Usando caché en memoria (ConcurrentMapCacheManager).");
            return new ConcurrentMapCacheManager();
        }
    }
}
