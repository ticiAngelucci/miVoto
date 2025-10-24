package com.mivoto.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "seed")
public record SeedProperties(boolean enabled) {

  public boolean enabled() {
    return enabled;
  }
}
