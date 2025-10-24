package com.mivoto.controller;

import com.mivoto.controller.dto.CastVoteRequest;
import com.mivoto.controller.dto.CastVoteResponse;
import com.mivoto.controller.dto.VerifyReceiptResponse;
import com.mivoto.service.voting.VotingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/votes")
public class VotingController {

  private final VotingService votingService;

  public VotingController(VotingService votingService) {
    this.votingService = votingService;
  }

  @PostMapping("/cast")
  public ResponseEntity<CastVoteResponse> cast(@RequestBody @Valid CastVoteRequest request) {
    return ResponseEntity.accepted().body(votingService.castVote(request));
  }

  @GetMapping("/{receipt}/verify")
  public ResponseEntity<VerifyReceiptResponse> verify(@PathVariable("receipt") String receipt) {
    return ResponseEntity.ok(votingService.verifyReceipt(receipt));
  }
}
