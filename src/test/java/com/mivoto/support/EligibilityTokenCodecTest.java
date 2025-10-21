package com.mivoto.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class EligibilityTokenCodecTest {

  @Test
  void encodeDecodeRoundTrip() {
    EligibilityTokenCodec codec = new EligibilityTokenCodec(new ObjectMapper());
    Instant expiresAt = Instant.now().plusSeconds(60);

    String encoded = codec.encode("token", new byte[] {1, 2, 3}, expiresAt);
    EligibilityTokenCodec.DecodedToken decoded = codec.decode(encoded);

    assertThat(decoded.token()).isEqualTo("token");
    assertThat(decoded.salt()).containsExactly( (byte) 1, (byte) 2, (byte) 3);
    assertThat(decoded.expiresAt()).isEqualTo(expiresAt);
  }
}
