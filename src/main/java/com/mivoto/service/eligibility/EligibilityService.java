package com.mivoto.service.eligibility;

import com.mivoto.controller.dto.EligibilityRequest;
import com.mivoto.controller.dto.EligibilityResponse;
import com.mivoto.model.EligibilityStatus;
import com.mivoto.model.VoterEligibility;
import com.mivoto.repository.VoterEligibilityRepository;
import com.mivoto.infrastructure.blockchain.VoteContractService;
import com.mivoto.infrastructure.security.MiArgentinaTokenVerifier;
import com.mivoto.security.SessionUser;
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
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;

@Service
public class EligibilityService {

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
    SessionUser user;
    try {
      user = tokenVerifier.verify(request.idToken());
    } catch (IllegalArgumentException ex) {
      throw new EligibilityException("MiArgentina token invalid", ex);
    }
    String wallet = normalizeWallet(request.walletAddress());
    String subjectHash = hashingService.hashSubject(user.subject());
    Instant now = Instant.now(clock);
    repository.findActiveBySubjectHash(subjectHash).ifPresent(existing -> {
      if (existing.expiresAt().isAfter(now) && existing.status() == EligibilityStatus.ACTIVE) {
        repository.markConsumed(existing.tokenHash());
      }
    });

    String rawToken = UUID.randomUUID().toString();
    byte[] salt = new byte[16];
    SECURE_RANDOM.nextBytes(salt);
    Instant expiresAt = now.plus(2, ChronoUnit.HOURS);
    String tokenHash = hashingService.hashToken(rawToken, salt);

    String encodedToken = codec.encode(rawToken, salt, expiresAt);

    Instant issuedAt = now;
    VoterEligibility eligibility = new VoterEligibility(
        UUID.randomUUID().toString(),
        subjectHash,
        issuedAt,
        expiresAt,
        tokenHash,
        wallet,
        EligibilityStatus.ACTIVE,
        "mi-argentina"
    );
    try {
      voteContractService.issueToken(tokenHash, wallet).join();
    } catch (Exception e) {
      Throwable cause = e.getCause() != null ? e.getCause() : e;
      throw new EligibilityException("Failed to register eligibility on-chain", cause);
    }
    repository.save(eligibility);
    auditService.record("identity-service", "ELIGIBILITY_ISSUED", Map.of(
        "eligibilityId", eligibility.id(),
        "subjectHash", subjectHash,
        "walletAddress", wallet
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

  private String normalizeWallet(String wallet) {
    if (wallet == null || wallet.isBlank()) {
      throw new EligibilityException("Wallet address required");
    }
    String trimmed = wallet.trim();
    if (!WalletUtils.isValidAddress(trimmed)) {
      throw new EligibilityException("Wallet address invalid");
    }
    String prefixed = trimmed.startsWith("0x") || trimmed.startsWith("0X")
        ? trimmed
        : "0x" + trimmed;
    try {
      return Keys.toChecksumAddress(prefixed);
    } catch (Exception ex) {
      throw new EligibilityException("Wallet address invalid checksum", ex);
    }
  }
}
