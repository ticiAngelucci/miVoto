package com.mivoto.service.eligibility;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mivoto.repository.VoterEligibilityRepository;
import com.mivoto.infrastructure.blockchain.VoteContractService;
import com.mivoto.infrastructure.security.MiArgentinaTokenVerifier;
import com.mivoto.service.audit.AuditService;
import com.mivoto.support.EligibilityException;
import com.mivoto.support.EligibilityTokenCodec;
import com.mivoto.support.HashingService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class EligibilityServiceTest {

  @Test
  void decodeTokenRejectsExpired() {
    MiArgentinaTokenVerifier verifier = mock(MiArgentinaTokenVerifier.class);
    VoterEligibilityRepository repository = mock(VoterEligibilityRepository.class);
    HashingService hashingService = mock(HashingService.class);
    EligibilityTokenCodec codec = new EligibilityTokenCodec(new ObjectMapper());
    VoteContractService voteContractService = mock(VoteContractService.class);
    AuditService auditService = mock(AuditService.class);
    Clock clock = Clock.fixed(Instant.parse("2024-05-01T10:00:00Z"), ZoneOffset.UTC);

    EligibilityService service = new EligibilityService(
        verifier,
        repository,
        hashingService,
        codec,
        voteContractService,
        auditService,
        clock
    );

    String token = codec.encode("raw", new byte[] {1}, Instant.parse("2024-05-01T09:00:00Z"));

    assertThatThrownBy(() -> service.decodeToken(token))
        .isInstanceOf(EligibilityException.class)
        .hasMessageContaining("expired");
  }
}
