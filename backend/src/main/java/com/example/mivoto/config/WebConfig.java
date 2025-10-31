package com.example.mivoto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Esta configuración permite que tu frontend (ej. localhost:5173)
        // pueda hacer peticiones a tu backend (localhost:8080)
        
        registry.addMapping("/api/**") // Permite CORS para todas tus rutas /api
                .allowedOrigins("http://localhost:5173") // O el puerto que use el frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos permitidos
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
