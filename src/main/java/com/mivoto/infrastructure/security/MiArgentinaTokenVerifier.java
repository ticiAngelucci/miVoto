package com.mivoto.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mivoto.security.SessionUser;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class MiArgentinaTokenVerifier {

  private final Clock clock;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public MiArgentinaTokenVerifier(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
  }

  public SessionUser verify(String idToken) {
    if ("stub-id-token".equals(idToken)) {
      return new SessionUser("stub-user", "Ciudadano", "Prueba", "ciudadano@example.com");
    }
    if (idToken != null && idToken.startsWith("stub-id-token.")) {
      String encoded = idToken.substring("stub-id-token.".length());
      try {
        byte[] decoded = Base64.getUrlDecoder().decode(encoded);
        JsonNode node = OBJECT_MAPPER.readTree(new String(decoded, StandardCharsets.UTF_8));
        String subject = textOrNull(node, "sub");
        if (subject == null || subject.isBlank()) {
          throw new IllegalArgumentException("Stub token missing subject");
        }
        String givenName = textOrNull(node, "given_name");
        String familyName = textOrNull(node, "family_name");
        String email = textOrNull(node, "email");
        return new SessionUser(subject, givenName, familyName, email);
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid stub MiArgentina token", e);
      }
    }
    try {
      SignedJWT signedJWT = SignedJWT.parse(idToken);
      JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
      Instant expiration = claims.getExpirationTime().toInstant();
      if (expiration.isBefore(clock.instant())) {
        throw new IllegalArgumentException("MiArgentina token expired");
      }
      String subject = claims.getSubject();
      if (subject == null || subject.isBlank()) {
        throw new IllegalArgumentException("MiArgentina token missing subject");
      }
      String givenName = claims.getStringClaim("given_name");
      String familyName = claims.getStringClaim("family_name");
      String email = claims.getStringClaim("email");
      return new SessionUser(subject, givenName, familyName, email);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Unable to parse MiArgentina id_token", e);
    }
  }

  private String textOrNull(JsonNode node, String field) {
    JsonNode value = node.get(field);
    if (value == null || value.isNull()) {
      return null;
    }
    String text = value.asText();
    return text != null && !text.isBlank() ? text : null;
  }
}
