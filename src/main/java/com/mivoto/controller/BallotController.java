package com.mivoto.controller;

import com.mivoto.controller.dto.BallotResponse;
import com.mivoto.controller.dto.BallotResultResponse;
import com.mivoto.controller.dto.TallyResponse;
import com.mivoto.model.Ballot;
import com.mivoto.service.voting.BallotService;
import com.mivoto.service.voting.VotingService;
import com.mivoto.support.ResourceNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ballots")
public class BallotController {

  private final VotingService votingService;
  private final BallotService ballotService;
  private final Clock clock;

  public BallotController(VotingService votingService, BallotService ballotService, Clock clock) {
    this.votingService = votingService;
    this.ballotService = ballotService;
    this.clock = clock;
  }

  @GetMapping
  public ResponseEntity<List<BallotResponse>> list() {
    Instant now = Instant.now(clock);
    List<BallotResponse> body = ballotService.listBallots().stream()
        .map(ballot -> toResponse(ballot, now))
        .toList();
    return ResponseEntity.ok(body);
  }

  @GetMapping("/{id}")
  public ResponseEntity<BallotResponse> get(@PathVariable("id") String id) {
    Instant now = Instant.now(clock);
    return ballotService.findBallot(id)
        .map(ballot -> ResponseEntity.ok(toResponse(ballot, now)))
        .orElseThrow(() -> new ResourceNotFoundException("Ballot not found: " + id));
  }

  @GetMapping("/{id}/tally")
  public ResponseEntity<TallyResponse> tally(@PathVariable("id") String id) {
    return ResponseEntity.ok(votingService.tally(id));
  }

  @GetMapping("/{id}/result")
  public ResponseEntity<BallotResultResponse> result(@PathVariable("id") String id) {
    return ResponseEntity.ok(votingService.getFinalResult(id));
  }

  @PostMapping("/{id}/finalize")
  public ResponseEntity<BallotResultResponse> finalizeBallot(@PathVariable("id") String id) {
    return ResponseEntity.ok(votingService.finalizeBallot(id));
  }

  private BallotResponse toResponse(Ballot ballot, Instant now) {
    return new BallotResponse(
        ballot.id(),
        ballot.institutionId(),
        ballot.title(),
        ballot.candidateIds(),
        ballot.opensAt(),
        ballot.closesAt(),
        ballot.allowMultipleSelection(),
        ballot.isOpen(now)
    );
  }
}
