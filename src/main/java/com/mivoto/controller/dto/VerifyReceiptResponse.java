package com.mivoto.controller.dto;

public record VerifyReceiptResponse(
    String receipt,
    String ballotId,
    boolean onChain,
    boolean offChain,
    String txHash
) {
}
