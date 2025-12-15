package com.example.mivoto.service;

/**
 * Resultado devuelto por la interacción on-chain.
 */
public record VoteExecutionResult(
    String voteTransactionHash,
    String sbtTransactionHash,
    String sbtTokenId
) {}
