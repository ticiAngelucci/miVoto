package com.mivoto.app.config;

import java.math.BigInteger;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "web3")
public record Web3Properties(
    String rpcUrl,
    Long chainId,
    String contractAddress,
    Long gasPrice,
    Long gasLimit
) {
  public BigInteger gasPrice() {
    return BigInteger.valueOf(gasPrice != null ? gasPrice : 20_000_000_000L);
  }

  public BigInteger gasLimit() {
    return BigInteger.valueOf(gasLimit != null ? gasLimit : 6_721_975L);
  }
}
