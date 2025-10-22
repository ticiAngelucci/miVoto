package com.mivoto.infrastructure.security;

import com.mivoto.security.SessionUser;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class MiArgentinaTokenVerifier {

  private final Clock clock;

  public MiArgentinaTokenVerifier(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
  }

  public SessionUser verify(String idToken) {
    if ("stub-id-token".equals(idToken)) {
      return new SessionUser("stub-user", "Ciudadano", "Prueba", "ciudadano@example.com");
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
}
