package com.mivoto.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "firebase")
public record FirebaseProperties(
    String projectId,
    String credentialsFile,
    boolean useEmulator,
    String emulatorHost,
    int emulatorPort
) {
  public String emulatorAuthority() {
    return emulatorHost + ":" + emulatorPort;
  }
}
