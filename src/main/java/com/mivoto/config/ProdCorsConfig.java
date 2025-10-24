package com.mivoto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("!dev")
public class ProdCorsConfig implements WebMvcConfigurer {

  private final FrontendProperties frontendProperties;

  public ProdCorsConfig(FrontendProperties frontendProperties) {
    this.frontendProperties = frontendProperties;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOriginPatterns(frontendProperties.baseUrl())
        .allowedMethods("*")
        .allowedHeaders("*")
        .allowCredentials(true);
  }
}
