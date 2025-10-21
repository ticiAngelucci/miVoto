package com.mivoto.service.eligibility;

import com.mivoto.api.dto.EligibilityRequest;
import com.mivoto.api.dto.EligibilityResponse;
import com.mivoto.domain.model.EligibilityStatus;
import com.mivoto.domain.model.VoterEligibility;
import com.mivoto.domain.repository.VoterEligibilityRepository;
import com.mivoto.infra.blockchain.VoteContractService;
import com.mivoto.infra.security.MiArgentinaTokenVerifier;
import com.mivoto.infra.security.MiArgentinaTokenVerifier.MiArgentinaUser;
import com.mivoto.service.audit.AuditService;
import com.mivoto.support.EligibilityException;
import com.mivoto.support.EligibilityTokenCodec;
import com.mivoto.support.EligibilityTokenCodec.DecodedToken;
import com.mivoto.support.HashingService;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EligibilityService {

  private static final Logger log = LoggerFactory.getLogger(EligibilityService.class);
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final MiArgentinaTokenVerifier tokenVerifier;
  private final VoterEligibilityRepository repository;
  private final HashingService hashingService;
  private final EligibilityTokenCodec codec;
  private final VoteContractService voteContractService;
  private final AuditService auditService;
  private final Clock clock;

  public EligibilityService(MiArgentinaTokenVerifier tokenVerifier,
      VoterEligibilityRepository repository,
      HashingService hashingService,
      EligibilityTokenCodec codec,
      VoteContractService voteContractService,
      AuditService auditService,
      Clock clock) {
    this.tokenVerifier = Objects.requireNonNull(tokenVerifier);
    this.repository = Objects.requireNonNull(repository);
    this.hashingService = Objects.requireNonNull(hashingService);
    this.codec = Objects.requireNonNull(codec);
    this.voteContractService = Objects.requireNonNull(voteContractService);
    this.auditService = Objects.requireNonNull(auditService);
    this.clock = Objects.requireNonNull(clock);
  }

  public EligibilityResponse issueEligibility(EligibilityRequest request) {
    MiArgentinaUser user;
    try {
      user = tokenVerifier.verify(request.idToken());
    } catch (IllegalArgumentException ex) {
      throw new EligibilityException("MiArgentina token invalid", ex);
    }
    String subjectHash = hashingService.hashSubject(user.subject());
    repository.findActiveBySubjectHash(subjectHash).ifPresent(existing ->
        log.info("Reusing active eligibility {} for subjectHash", existing.id()));

    String rawToken = UUID.randomUUID().toString();
    byte[] salt = new byte[16];
    SECURE_RANDOM.nextBytes(salt);
    Instant expiresAt = Instant.now(clock).plus(2, ChronoUnit.HOURS);
    String tokenHash = hashingService.hashToken(rawToken, salt);

    String encodedToken = codec.encode(rawToken, salt, expiresAt);

    VoterEligibility eligibility = new VoterEligibility(
        UUID.randomUUID().toString(),
        subjectHash,
        Instant.now(clock),
        expiresAt,
        tokenHash,
        EligibilityStatus.ACTIVE,
        "mi-argentina"
    );
    repository.save(eligibility);
    voteContractService.issueToken(prefixHex(tokenHash)).whenComplete((receipt, throwable) -> {
      if (throwable != null) {
        log.error("Failed to register tokenHash on-chain", throwable);
      } else if (receipt != null) {
        log.debug("Token registered on-chain tx {}", receipt.getTransactionHash());
      }
    });
    auditService.record("identity-service", "ELIGIBILITY_ISSUED", Map.of(
        "eligibilityId", eligibility.id(),
        "subjectHash", subjectHash
    ));
    return new EligibilityResponse(encodedToken, expiresAt);
  }

  public DecodedToken decodeToken(String encoded) {
    DecodedToken token = codec.decode(encoded);
    if (token.expiresAt().isBefore(Instant.now(clock))) {
      throw new EligibilityException("Eligibility token expired");
    }
    return token;
  }

  private String prefixHex(String hexWithoutPrefix) {
    if (hexWithoutPrefix.startsWith("0x")) {
      return hexWithoutPrefix;
    }
    return "0x" + hexWithoutPrefix;
  }
}
