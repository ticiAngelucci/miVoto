package com.mivoto.infrastructure.security;

import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class MiArgentinaOauthService {

  private static final Logger log = LoggerFactory.getLogger(MiArgentinaOauthService.class);
  private final RestTemplate restTemplate;
  private final ClientRegistration registration;

  public MiArgentinaOauthService(RestTemplateBuilder builder,
      ClientRegistrationRepository registrationRepository) {
    this.restTemplate = builder.build();
    this.registration = Objects.requireNonNull(
        registrationRepository.findByRegistrationId("miargentina"),
        "MiArgentina client registration not configured");
  }

  public String exchangeCodeForIdToken(String code) {
    if (code == null || code.isBlank()) {
      throw new IllegalArgumentException("Authorization code is required");
    }
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", "authorization_code");
    form.add("code", code);
    form.add("redirect_uri", registration.getRedirectUri());
    form.add("client_id", registration.getClientId());
    if (registration.getClientSecret() != null) {
      form.add("client_secret", registration.getClientSecret());
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    try {
      ResponseEntity<Map> response = restTemplate.postForEntity(
          registration.getProviderDetails().getTokenUri(),
          new HttpEntity<>(form, headers),
          Map.class);
      Map<?, ?> body = response.getBody();
      if (body == null || !body.containsKey("id_token")) {
        throw new IllegalStateException("MiArgentina token response missing id_token");
      }
      Object idToken = body.get("id_token");
      if (!(idToken instanceof String tokenString)) {
        throw new IllegalStateException("MiArgentina id_token has unexpected format");
      }
      return tokenString;
    } catch (RestClientException ex) {
      log.error("MiArgentina token exchange failed", ex);
      throw new IllegalStateException("MiArgentina token exchange failed", ex);
    }
  }
}
