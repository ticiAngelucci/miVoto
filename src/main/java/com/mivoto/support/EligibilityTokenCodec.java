package com.mivoto.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EligibilityTokenCodec {

  private final ObjectMapper objectMapper;

  public EligibilityTokenCodec(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String encode(String token, byte[] salt, Instant expiresAt) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("token", token);
    payload.put("salt", Base64.getUrlEncoder().withoutPadding().encodeToString(salt));
    payload.put("exp", expiresAt.toEpochMilli());
    try {
      byte[] json = objectMapper.writeValueAsBytes(payload);
      // TODO: Reemplazar por JWE con cifrado asimétrico.
      return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to encode eligibility token", e);
    }
  }

  public DecodedToken decode(String encoded) {
    try {
      byte[] json = Base64.getUrlDecoder().decode(encoded);
      @SuppressWarnings("unchecked")
      Map<String, Object> map = objectMapper.readValue(json, Map.class);
      String token = (String) map.get("token");
      String saltEncoded = (String) map.get("salt");
      Number exp = (Number) map.get("exp");
      byte[] salt = Base64.getUrlDecoder().decode(saltEncoded);
      Instant expiresAt = Instant.ofEpochMilli(exp.longValue());
      return new DecodedToken(token, salt, expiresAt);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid eligibility token", e);
    }
  }

  public record DecodedToken(String token, byte[] salt, Instant expiresAt) {}
}
