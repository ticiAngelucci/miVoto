package com.mivoto.controller;

import com.mivoto.controller.dto.TallyResponse;
import com.mivoto.service.voting.VotingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ballots")
public class BallotController {

  private final VotingService votingService;

  public BallotController(VotingService votingService) {
    this.votingService = votingService;
  }

  @GetMapping("/{id}/tally")
  public ResponseEntity<TallyResponse> tally(@PathVariable String id) {
    return ResponseEntity.ok(votingService.tally(id));
  }
}
