package com.mivoto.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.mivoto.app.config.SecurityProperties;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class HashingServiceTest {

  @Test
  void hashesAreDeterministic() {
    SecurityProperties props = new SecurityProperties("pepper-sub", "pepper-token");
    HashingService hashingService = new HashingService(props);

    String subjectHash1 = hashingService.hashSubject("subject-123");
    String subjectHash2 = hashingService.hashSubject("subject-123");

    assertThat(subjectHash1).isEqualTo(subjectHash2);
    assertThat(subjectHash1).hasSize(64);
  }

  @Test
  void tokenHashUsesSalt() {
    SecurityProperties props = new SecurityProperties("pepper-sub", "pepper-token");
    HashingService hashingService = new HashingService(props);

    byte[] saltA = "salt-a".getBytes(StandardCharsets.UTF_8);
    byte[] saltB = "salt-b".getBytes(StandardCharsets.UTF_8);

    String hashA = hashingService.hashToken("token", saltA);
    String hashB = hashingService.hashToken("token", saltB);

    assertThat(hashA).isNotEqualTo(hashB);
  }
}
