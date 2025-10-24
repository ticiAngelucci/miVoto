package com.mivoto.controller;

import com.mivoto.service.admin.SeedService;
import com.mivoto.service.admin.SeedService.SeedSummary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/seed")
public class SeedController {

  private final SeedService seedService;

  public SeedController(SeedService seedService) {
    this.seedService = seedService;
  }

  @PostMapping("/default")
  public ResponseEntity<SeedSummary> seedDefault() {
    if (!seedService.isEnabled()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Seed deshabilitado en este entorno");
    }
    SeedSummary summary = seedService.seedDefaultData();
    return ResponseEntity.ok(summary);
  }

  @PostMapping("/close/{id}")
  public ResponseEntity<Void> closeBallot(@PathVariable("id") String id) {
    if (!seedService.isEnabled()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Seed deshabilitado en este entorno");
    }
    seedService.closeBallotNow(id);
    return ResponseEntity.noContent().build();
  }
}
