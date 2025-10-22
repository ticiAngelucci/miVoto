package com.mivoto.controller;

import com.mivoto.controller.dto.InstitutionRequest;
import com.mivoto.controller.dto.InstitutionResponse;
import com.mivoto.model.Institution;
import com.mivoto.service.institution.InstitutionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/institutions")
public class InstitutionController {

  private final InstitutionService institutionService;

  public InstitutionController(InstitutionService institutionService) {
    this.institutionService = institutionService;
  }

  @GetMapping
  public ResponseEntity<List<InstitutionResponse>> list() {
    List<InstitutionResponse> responses = institutionService.listAll().stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<InstitutionResponse> get(@PathVariable String id) {
    return ResponseEntity.ok(toResponse(institutionService.get(id)));
  }

  @PostMapping
  public ResponseEntity<InstitutionResponse> create(@RequestBody @Valid InstitutionRequest request) {
    Institution created = institutionService.create(request);
    return ResponseEntity
        .created(URI.create("/institutions/" + created.id()))
        .body(toResponse(created));
  }

  @PutMapping("/{id}")
  public ResponseEntity<InstitutionResponse> update(
      @PathVariable String id,
      @RequestBody @Valid InstitutionRequest request) {
    Institution updated = institutionService.update(id, request);
    return ResponseEntity.ok(toResponse(updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    institutionService.delete(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  private InstitutionResponse toResponse(Institution institution) {
    return new InstitutionResponse(
        institution.id(),
        institution.name(),
        institution.description(),
        institution.active(),
        institution.createdAt(),
        institution.updatedAt()
    );
  }
}
