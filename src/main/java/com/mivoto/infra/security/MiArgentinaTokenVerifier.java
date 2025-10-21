package com.mivoto.infra.security;

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

  public MiArgentinaUser verify(String idToken) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(idToken);
      JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
      Instant expiration = claims.getExpirationTime().toInstant();
      if (expiration.isBefore(clock.instant())) {
        throw new IllegalArgumentException("MiArgentina token expired");
      }
      // TODO: validar firma utilizando JWKS de MiArgentina y comparar audience/issuer.
      String subject = claims.getSubject();
      if (subject == null || subject.isBlank()) {
        throw new IllegalArgumentException("MiArgentina token missing subject");
      }
      return new MiArgentinaUser(subject);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Unable to parse MiArgentina id_token", e);
    }
  }

  public record MiArgentinaUser(String subject) {}
}
