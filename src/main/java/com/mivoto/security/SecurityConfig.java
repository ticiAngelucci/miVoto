package com.mivoto.security;

import com.mivoto.config.JwtProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Profile("!dev")
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(Customizer.withDefaults())
        .csrf(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/auth/miargentina/callback").permitAll()
            .requestMatchers(HttpMethod.GET, "/auth/miargentina/callback").permitAll()
            .requestMatchers(HttpMethod.GET, "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/internal/vote-status/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/votes/*/verify", "/ballots/*/tally").permitAll()
            .requestMatchers(HttpMethod.POST, "/eligibility/issue").authenticated()
            .requestMatchers(HttpMethod.POST, "/eligibility/issue/session").authenticated()
            .requestMatchers(HttpMethod.POST, "/votes/cast").authenticated()
            .requestMatchers(HttpMethod.POST, "/seed/**").authenticated()
            .anyRequest().authenticated())
        .oauth2Login(Customizer.withDefaults())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'")))
        .logout(logout -> logout.addLogoutHandler(rateLimitingLogoutHandler()))
        .addFilterBefore(new SessionUserAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  JwtDecoder jwtDecoder(JwtProperties jwtProperties) {
    return NimbusJwtDecoder.withJwkSetUri(jwtProperties.jwkSetUri())
        .build();
  }

  private LogoutHandler rateLimitingLogoutHandler() {
    return (request, response, authentication) -> {
      // TODO: Enforce logout rate limiting for abusive clients.
    };
  }
}
