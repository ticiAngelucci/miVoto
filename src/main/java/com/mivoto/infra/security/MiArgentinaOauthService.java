package com.mivoto.infra.security;

import org.springframework.stereotype.Component;

@Component
public class MiArgentinaOauthService {

  public String exchangeCodeForIdToken(String code, String state) {
    // TODO: Implementar intercambio real contra MiArgentina usando OAuth2.
    if (code == null || code.isBlank()) {
      throw new IllegalArgumentException("Authorization code is required");
    }
    return "stub-id-token";
  }
}
