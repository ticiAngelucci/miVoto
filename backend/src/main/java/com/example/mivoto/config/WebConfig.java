package com.example.mivoto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        
        // Aplicamos una regla global para TODAS las rutas (/**)
        // Esto incluye /api, /swagger-ui.html, y /v3/api-docs
        registry.addMapping("/**") 
                .allowedOrigins(
                    "http://localhost:5173", 
                    "https://mi-voto-theta.vercel.app"
                ) 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}