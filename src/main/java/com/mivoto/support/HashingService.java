package com.mivoto.support;

import com.mivoto.config.SecurityProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class HashingService {

  private final SecurityProperties securityProperties;
  private final HexFormat hexFormat = HexFormat.of();

  public HashingService(SecurityProperties securityProperties) {
    this.securityProperties = Objects.requireNonNull(securityProperties);
  }

  public String hashSubject(String subject) {
    String peppered = subject + securityProperties.subjectPepper();
    return sha256Hex(peppered.getBytes(StandardCharsets.UTF_8));
  }

  public String hashToken(String token, byte[] salt) {
    byte[] combined = (token + securityProperties.tokenPepper()).getBytes(StandardCharsets.UTF_8);
    byte[] salted = new byte[combined.length + salt.length];
    System.arraycopy(combined, 0, salted, 0, combined.length);
    System.arraycopy(salt, 0, salted, combined.length, salt.length);
    return sha256Hex(salted);
  }

  public String hashVotePayload(String ballotId, Map<String, Object> payload) {
    String canonical = ballotId + ":" + payload.toString(); // TODO: usar canonical JSON estable
    return sha256Hex(canonical.getBytes(StandardCharsets.UTF_8));
  }

  public String deriveReceipt(String ballotId, String voteHash, Instant timestamp) {
    String material = ballotId + ":" + voteHash + ":" + timestamp.toEpochMilli();
    return sha256Hex(material.getBytes(StandardCharsets.UTF_8));
  }

  public String sha256Hex(byte[] data) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(data);
      return hexFormat.formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm missing", e);
    }
  }
}
