package com.visionexdigital.weather_data_processing.configurations;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("weather");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(java.time.Duration.ofMinutes(30))
                .maximumSize(100) // Prevents excessive memory usage
        );
        return cacheManager;
    }
}
