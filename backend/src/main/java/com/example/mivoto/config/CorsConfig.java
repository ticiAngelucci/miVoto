package com.example.mivoto.config;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  private final CorsProperties corsProperties;

  public CorsConfig(CorsProperties corsProperties) {
    this.corsProperties = corsProperties;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    var registration = registry.addMapping("/**");

    var normalizedOrigins = normalizeOrigins(corsProperties.getAllowedOrigins());
    if (!normalizedOrigins.isEmpty()) {
      registration.allowedOrigins(normalizedOrigins.toArray(new String[0]));
    }

    var allowedOriginPatterns = corsProperties.getAllowedOriginPatterns();
    if (!allowedOriginPatterns.isEmpty()) {
      registration.allowedOriginPatterns(allowedOriginPatterns.toArray(new String[0]));
    }

    var allowedMethods = corsProperties.getAllowedMethods();
    if (!allowedMethods.isEmpty()) {
      registration.allowedMethods(allowedMethods.toArray(new String[0]));
    }

    registration.maxAge(corsProperties.getMaxAgeSeconds());
  }

  private List<String> normalizeOrigins(List<String> origins) {
    return origins.stream()
        .filter(StringUtils::hasText)
        .map(String::trim)
        .map(this::removeTrailingSlash)
        .collect(Collectors.toList());
  }

  private String removeTrailingSlash(String origin) {
    return origin.endsWith("/") ? origin.substring(0, origin.length() - 1) : origin;
  }
}
