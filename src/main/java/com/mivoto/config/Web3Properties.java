package com.mivoto.config;

import java.math.BigInteger;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "web3")
public record Web3Properties(
    String rpcUrl,
    Long chainId,
    String contractAddress,
    Long gasPrice,
    Long gasLimit,
    Boolean mockEnabled
) {
  private static final long DEFAULT_GAS_PRICE = 20_000_000_000L; // 20 gwei
  private static final long DEFAULT_GAS_LIMIT = 6_721_975L;

  public BigInteger gasPriceWei() {
    long value = gasPrice != null ? gasPrice : DEFAULT_GAS_PRICE;
    return BigInteger.valueOf(value);
  }

  public BigInteger gasLimitUnits() {
    long value = gasLimit != null ? gasLimit : DEFAULT_GAS_LIMIT;
    return BigInteger.valueOf(value);
  }

  public boolean isMockEnabled() {
    return Boolean.TRUE.equals(mockEnabled);
  }
}
