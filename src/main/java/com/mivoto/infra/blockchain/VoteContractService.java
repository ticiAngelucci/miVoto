package com.mivoto.infra.blockchain;

import com.mivoto.app.config.Web3Properties;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.StaticGasProvider;

@Service
public class VoteContractService {

  private static final Logger log = LoggerFactory.getLogger(VoteContractService.class);

  private final Web3j web3j;
  private final Credentials credentials;
  private final Web3Properties props;
  private final StaticGasProvider gasProvider;

  public VoteContractService(Web3j web3j, Credentials credentials, Web3Properties props) {
    this.web3j = Objects.requireNonNull(web3j);
    this.credentials = Objects.requireNonNull(credentials);
    this.props = Objects.requireNonNull(props);
    this.gasProvider = new StaticGasProvider(props.gasPrice(), props.gasLimit());
  }

  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500L, multiplier = 2.0))
  public CompletableFuture<TransactionReceipt> issueToken(String tokenHashHex) {
    // TODO: Reemplazar por wrapper generado de web3j (MiVoto).
    String payload = buildIssueTokenPayload(tokenHashHex);
    return sendTransaction(payload);
  }

  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500L, multiplier = 2.0))
  public CompletableFuture<TransactionReceipt> castVote(long ballotId, String tokenHashHex, String voteHashHex, String receiptHex) {
    String payload = buildCastVotePayload(ballotId, tokenHashHex, voteHashHex, receiptHex);
    return sendTransaction(payload);
  }

  public boolean isReceiptRegistered(String receiptHex) {
    try {
      return web3j.ethGetLogs(new org.web3j.protocol.core.methods.request.EthFilter(
              DefaultBlockParameterName.EARLIEST,
              DefaultBlockParameterName.LATEST,
              props.contractAddress())
              .addOptionalTopics(null, null, null, receiptHex))
          .send()
          .getLogs()
          .stream()
          .map(logResult -> (Log) logResult)
          .anyMatch(l -> l.getData() != null && l.getData().contains(receiptHex.substring(2)));
    } catch (Exception e) {
      log.warn("Unable to verify receipt {} on-chain", receiptHex, e);
      return false;
    }
  }

  private CompletableFuture<TransactionReceipt> sendTransaction(String data) {
    BigInteger nonce;
    try {
      nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.PENDING)
          .send().getTransactionCount();
    } catch (Exception e) {
      CompletableFuture<TransactionReceipt> future = new CompletableFuture<>();
      future.completeExceptionally(e);
      return future;
    }
    Transaction tx = Transaction.createFunctionCallTransaction(
        credentials.getAddress(),
        nonce,
        gasProvider.getGasPrice(),
        gasProvider.getGasLimit(),
        props.contractAddress(),
        data);
    return web3j.ethSendTransaction(tx)
        .sendAsync()
        .thenCompose(response -> {
          if (response.hasError()) {
            CompletableFuture<TransactionReceipt> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("TX error: " + response.getError().getMessage()));
            return future;
          }
          String txHash = response.getTransactionHash();
          log.debug("Submitted tx {}", txHash);
          return pollReceipt(txHash);
        });
  }

  private CompletableFuture<TransactionReceipt> pollReceipt(String txHash) {
    return CompletableFuture.supplyAsync(() -> {
      int attempts = 0;
      while (attempts < 15) {
        attempts++;
        try {
          Optional<TransactionReceipt> receipt = Optional.ofNullable(
              web3j.ethGetTransactionReceipt(txHash).send().getTransactionReceipt().orElse(null));
          if (receipt.isPresent()) {
            return receipt.get();
          }
          Thread.sleep(Duration.ofSeconds(2).toMillis());
        } catch (Exception e) {
          log.debug("Waiting for tx {}", txHash, e);
        }
      }
      throw new IllegalStateException("Timed out waiting for receipt " + txHash);
    });
  }

  private String buildIssueTokenPayload(String tokenHashHex) {
    // TODO: serializar llamada ABI issueToken(bytes32)
    return "0x" + tokenHashHex.replace("0x", "");
  }

  private String buildCastVotePayload(long ballotId, String tokenHashHex, String voteHashHex, String receiptHex) {
    // TODO: serializar llamada ABI castVote(uint256,bytes32,bytes32,bytes32)
    return "0x" + tokenHashHex.replace("0x", "") + voteHashHex.replace("0x", "") + receiptHex.replace("0x", "");
  }
}
