package com.example.mivoto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String FRONTEND_ORIGIN = "https://mi-voto-theta.vercel.app";
    private static final String[] ALLOWED_METHODS = {"GET", "POST", "OPTIONS"};

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(FRONTEND_ORIGIN)
                .allowedMethods(ALLOWED_METHODS)
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
