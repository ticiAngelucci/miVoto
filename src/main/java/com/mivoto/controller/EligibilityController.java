package com.mivoto.controller;

import com.mivoto.controller.dto.EligibilityRequest;
import com.mivoto.controller.dto.EligibilityResponse;
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

  @PostMapping(
      value = "/issue",
      consumes = "application/json",
      produces = "application/json"
  )
  public ResponseEntity<EligibilityResponse> issue(@RequestBody @Valid EligibilityRequest request) {
    EligibilityResponse resp = eligibilityService.issueEligibility(request);
    // Si tu EligibilityResponse expone getId() o id(), arma Location
    try {
      String id = null;
      try {
        java.lang.reflect.Method m = resp.getClass().getMethod("getId");
        Object idObj = m.invoke(resp);
        if (idObj != null) {
          id = String.valueOf(idObj);
        }
      } catch (NoSuchMethodException e1) {
        try {
          java.lang.reflect.Method m = resp.getClass().getMethod("id");
          Object idObj = m.invoke(resp);
          if (idObj != null) {
            id = String.valueOf(idObj);
          }
        } catch (NoSuchMethodException e2) {
          // no id method available
        }
      }
      if (id != null && !id.isBlank()) {
        var location = java.net.URI.create("/eligibility/" + id);
        return ResponseEntity.created(location).body(resp);
      }
    } catch (Exception ignored) {
      // If reflection invocation fails or other exceptions, devolvemos 201 sin Location
    }
    return ResponseEntity.status(201).body(resp);
  }
}
