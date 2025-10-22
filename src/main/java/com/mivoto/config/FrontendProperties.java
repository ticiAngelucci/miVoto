package com.mivoto.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "frontend")
public record FrontendProperties(String baseUrl) {

  public String baseUrl() {
    return baseUrl != null ? baseUrl : "http://localhost:3000";
  }
}
