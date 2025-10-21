package com.mivoto.api.controller;

import com.mivoto.api.dto.EligibilityRequest;
import com.mivoto.api.dto.EligibilityResponse;
import com.mivoto.service.eligibility.EligibilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/eligibility")
public class EligibilityController {

  private final EligibilityService eligibilityService;

  public EligibilityController(EligibilityService eligibilityService) {
    this.eligibilityService = eligibilityService;
  }

  @PostMapping("/issue")
  public ResponseEntity<EligibilityResponse> issue(@RequestBody @Valid EligibilityRequest request) {
    return ResponseEntity.status(201).body(eligibilityService.issueEligibility(request));
  }
}
