package com.bricebopda.softcomp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration CORS globale de l'application.
 *
 * Autorise les appels cross-origin depuis n'importe quelle origine (développement).
 * En production, remplacer {@code "*"} par l'URL exacte du frontend
 * (ex: {@code "https://monapp.example.com"}).
 *
 * Méthodes autorisées : GET, POST, PUT, DELETE, OPTIONS
 * Headers autorisés : tous (*)
 */
@Configuration
public class CorsConfig {

    /**
     * Enregistre les règles CORS globales pour tous les endpoints {@code /api/**}.
     *
     * @return un WebMvcConfigurer qui applique la configuration CORS à Spring MVC
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        // Origines autorisées — remplacer par l'URL du frontend en production
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        // Durée de mise en cache du résultat du preflight (secondes)
                        .maxAge(3600);
            }
        };
    }
}
