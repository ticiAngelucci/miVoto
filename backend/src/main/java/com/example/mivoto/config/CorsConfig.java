package com.example.mivoto.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    private static final List<String> ALLOWED_ORIGIN_PATTERNS = List.of(
            "https://mi-voto-theta.vercel.app",
            "https://*.vercel.app",
            "http://localhost:5173",
            "http://127.0.0.1:5173");
    private static final List<String> ALLOWED_METHODS = List.of("GET", "POST", "OPTIONS");
    private static final long MAX_AGE_SECONDS = 3600L;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        ALLOWED_ORIGIN_PATTERNS.forEach(config::addAllowedOriginPattern);
        config.addAllowedHeader("*");
        ALLOWED_METHODS.forEach(config::addAllowedMethod);
        config.setAllowCredentials(true);
        config.setMaxAge(MAX_AGE_SECONDS);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
