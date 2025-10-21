package com.mivoto.api.controller;

import com.mivoto.api.dto.EligibilityRequest;
import com.mivoto.api.dto.EligibilityResponse;
import com.mivoto.api.dto.MiArgentinaCallbackRequest;
import com.mivoto.infra.security.MiArgentinaOauthService;
import com.mivoto.service.eligibility.EligibilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final MiArgentinaOauthService oauthService;
  private final EligibilityService eligibilityService;

  public AuthController(MiArgentinaOauthService oauthService, EligibilityService eligibilityService) {
    this.oauthService = oauthService;
    this.eligibilityService = eligibilityService;
  }

  @PostMapping("/miargentina/callback")
  public ResponseEntity<EligibilityResponse> callback(@RequestBody @Valid MiArgentinaCallbackRequest request) {
    String idToken = oauthService.exchangeCodeForIdToken(request.code(), request.state());
    EligibilityResponse response = eligibilityService.issueEligibility(new EligibilityRequest(idToken));
    return ResponseEntity.ok(response);
  }
}
