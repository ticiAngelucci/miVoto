package com.example.mivoto.config;

import java.math.BigInteger;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blockchain")
public class BlockchainProperties {

    private String providerUrl;
    private String privateKey;
    private String electionContractAddress;
    private long chainId = 11155111L;
    private BigInteger gasPrice = BigInteger.valueOf(20_000_000_000L);
    private BigInteger gasLimit = BigInteger.valueOf(600_000L);

    public String getProviderUrl() {
        return providerUrl;
    }

    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getElectionContractAddress() {
        return electionContractAddress;
    }

    public void setElectionContractAddress(String electionContractAddress) {
        this.electionContractAddress = electionContractAddress;
    }

    public long getChainId() {
        return chainId;
    }

    public void setChainId(long chainId) {
        this.chainId = chainId;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }
}
