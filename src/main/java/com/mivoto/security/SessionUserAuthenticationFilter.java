package com.mivoto.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class SessionUserAuthenticationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      var session = request.getSession(false);
      if (session != null) {
        Object attribute = session.getAttribute(SessionKeys.USER);
        if (attribute instanceof SessionUser user) {
          Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
              user,
              session.getAttribute(SessionKeys.ID_TOKEN),
              java.util.List.of(new SimpleGrantedAuthority("ROLE_USER"))
          );
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }
    }
    filterChain.doFilter(request, response);
  }
}
