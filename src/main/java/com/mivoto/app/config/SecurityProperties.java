package com.mivoto.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public record SecurityProperties(
    String subjectPepper,
    String tokenPepper
) {
}
