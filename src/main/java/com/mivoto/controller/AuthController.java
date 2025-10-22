package com.mivoto.controller;

import com.mivoto.config.FrontendProperties;
import com.mivoto.controller.dto.LoginResponse;
import com.mivoto.controller.dto.MiArgentinaCallbackRequest;
import com.mivoto.infrastructure.security.MiArgentinaOauthService;
import com.mivoto.infrastructure.security.MiArgentinaTokenVerifier;
import com.mivoto.security.SessionKeys;
import com.mivoto.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final MiArgentinaOauthService oauthService;
  private final MiArgentinaTokenVerifier tokenVerifier;
  private final FrontendProperties frontendProperties;

  public AuthController(MiArgentinaOauthService oauthService,
      MiArgentinaTokenVerifier tokenVerifier,
      FrontendProperties frontendProperties) {
    this.oauthService = oauthService;
    this.tokenVerifier = tokenVerifier;
    this.frontendProperties = frontendProperties;
  }

  @PostMapping("/miargentina/callback")
  public ResponseEntity<LoginResponse> callbackPost(@RequestBody @Valid MiArgentinaCallbackRequest request,
      HttpSession session) {
    SessionUser user = handleLogin(request.code(), session);
    return ResponseEntity.ok(toResponse(user));
  }

  @GetMapping("/miargentina/callback")
  public ResponseEntity<String> callbackGet(@RequestParam("code") String code,
      @RequestParam(value = "state", required = false) String state,
      HttpSession session) {
    handleLogin(code, session);
    String redirect = frontendProperties.baseUrl();
    String body = """
        <!DOCTYPE html>
        <html lang=\"es\">
        <head>
          <meta charset=\"utf-8\"/>
          <title>Redirigiendo…</title>
          <meta http-equiv=\"refresh\" content=\"0;url=%s\"/>
          <script>
            window.location.replace('%s');
          </script>
        </head>
        <body>
          <p>Autenticación completada. Si no eres redireccionado automáticamente, haz clic <a href=\"%s\">aquí</a>.</p>
        </body>
        </html>
        """.formatted(redirect, redirect, redirect);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, "text/html;charset=UTF-8")
        .body(body);
  }

  @GetMapping("/session")
  public ResponseEntity<LoginResponse> session(HttpSession session) {
    SessionUser user = (SessionUser) session.getAttribute(SessionKeys.USER);
    if (user == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return ResponseEntity.ok(toResponse(user));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpSession session) {
    session.invalidate();
    return ResponseEntity.noContent().build();
  }

  private SessionUser handleLogin(String code, HttpSession session) {
    String idToken = oauthService.exchangeCodeForIdToken(code);
    SessionUser user = tokenVerifier.verify(idToken);
    session.setAttribute(SessionKeys.USER, user);
    session.setAttribute(SessionKeys.ID_TOKEN, idToken);
    return user;
  }

  private LoginResponse toResponse(SessionUser user) {
    return new LoginResponse(user.subject(), user.displayName(), user.email());
  }
}
