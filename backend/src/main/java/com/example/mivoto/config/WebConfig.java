package com.example.mivoto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        
        // 1. Regla para tu API
        registry.addMapping("/api/**") 
                .allowedOrigins(
                    "http://localhost:5173", // Para pruebas locales
                    "https://mi-voto-theta.vercel.app" // <-- AÑADIDO PARA VERCEL
                ) 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);

        // 2. Regla para la documentación de Swagger (API)
        registry.addMapping("/v3/api-docs/**")
                .allowedOrigins(
                    "http://localhost:5173", 
                    "http://127.0.0.1:5173", 
                    "http://localhost:8080",
                    "https://mi-voto-theta.vercel.app" // <-- AÑADIDO PARA VERCEL
                )
                .allowedMethods("GET")
                .allowedHeaders("*");
                
        // 3. Regla para la Interfaz de Swagger
        registry.addMapping("/swagger-ui/**")
                .allowedOrigins(
                    "http://localhost:5173", 
                    "http://127.0.0.1:5173", 
                    "http://localhost:8080",
                    "https://mi-voto-theta.vercel.app" // <-- AÑADIDO PARA VERCEL
                )
                .allowedMethods("GET")
                .allowedHeaders("*");
    }
}
