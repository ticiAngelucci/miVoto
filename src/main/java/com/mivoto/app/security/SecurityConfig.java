package com.mivoto.app.security;

import com.mivoto.app.config.JwtProperties;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/auth/miargentina/callback").permitAll()
            .requestMatchers(HttpMethod.GET, "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/votes/*/verify", "/ballots/*/tally").permitAll()
            .requestMatchers(HttpMethod.POST, "/eligibility/issue").authenticated()
            .requestMatchers(HttpMethod.POST, "/votes/cast").authenticated()
            .anyRequest().authenticated())
        .oauth2Login(Customizer.withDefaults())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'")))
        .logout(logout -> logout.addLogoutHandler(rateLimitingLogoutHandler()));
    return http.build();
  }

  @Bean
  JwtDecoder jwtDecoder(JwtProperties jwtProperties) {
    return NimbusJwtDecoder.withJwkSetUri(jwtProperties.jwkSetUri())
        .cache(Duration.ofMinutes(15))
        .build();
  }

  private LogoutHandler rateLimitingLogoutHandler() {
    return (request, response, authentication) -> {
      // TODO: Enforce logout rate limiting for abusive clients.
    };
  }
}
