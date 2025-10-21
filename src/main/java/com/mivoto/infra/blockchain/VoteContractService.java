package com.mivoto.infra.blockchain;

import com.mivoto.app.config.Web3Properties;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.ChainIdLong;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Numeric;

@Service
public class VoteContractService {

  private static final Logger log = LoggerFactory.getLogger(VoteContractService.class);
  private static final Event VOTE_CAST_EVENT = new Event(
      "VoteCast",
      Arrays.asList(
          new TypeReference<Uint256>(true) {},
          new TypeReference<Bytes32>(true) {},
          new TypeReference<Bytes32>() {},
          new TypeReference<Bytes32>() {},
          new TypeReference<org.web3j.abi.datatypes.Address>(true) {}
      )
  );

  private final Web3j web3j;
  private final Web3Properties props;
  private final StaticGasProvider gasProvider;
  private final TransactionManager transactionManager;
  private final TransactionReceiptProcessor receiptProcessor;

  public VoteContractService(Web3j web3j, Credentials credentials, Web3Properties props) {
    this.web3j = Objects.requireNonNull(web3j);
    this.props = Objects.requireNonNull(props);
    this.gasProvider = new StaticGasProvider(BigInteger.valueOf(props.gasPrice()), BigInteger.valueOf(props.gasLimit()));
    this.receiptProcessor = new PollingTransactionReceiptProcessor(
        web3j,
        Duration.ofSeconds(2).toMillis(),
        15
    );
    long chainId = props.chainId() != null ? props.chainId() : ChainIdLong.NONE;
    this.transactionManager = new RawTransactionManager(web3j, credentials, chainId, receiptProcessor);
  }

  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500L, multiplier = 2.0))
  public CompletableFuture<TransactionReceipt> issueToken(String tokenHashHex) {
    Function function = new Function(
        "issueToken",
        Collections.singletonList(toBytes32(tokenHashHex)),
        Collections.emptyList()
    );
    return execute(function);
  }

  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500L, multiplier = 2.0))
  public CompletableFuture<TransactionReceipt> castVote(long ballotId, String tokenHashHex, String voteHashHex, String receiptHex) {
    Function function = new Function(
        "castVote",
        Arrays.asList(
            new Uint256(BigInteger.valueOf(ballotId)),
            toBytes32(tokenHashHex),
            toBytes32(voteHashHex),
            toBytes32(receiptHex)
        ),
        Collections.emptyList()
    );
    return execute(function);
  }

  public boolean isReceiptRegistered(String receiptHex) {
    String normalized = normalizeHex(receiptHex);
    EthFilter filter = new EthFilter(
        DefaultBlockParameterName.EARLIEST,
        DefaultBlockParameterName.LATEST,
        props.contractAddress()
    ).addSingleTopic(EventEncoder.encode(VOTE_CAST_EVENT));
    try {
      EthLog logResponse = web3j.ethGetLogs(filter).send();
      for (EthLog.LogResult<?> logResult : logResponse.getLogs()) {
        Log logEntry = (Log) logResult.get();
        List<Type> decoded = FunctionReturnDecoder.decode(
            logEntry.getData(),
            VOTE_CAST_EVENT.getNonIndexedParameters()
        );
        if (decoded.size() < 2) {
          continue;
        }
        Bytes32 receiptValue = (Bytes32) decoded.get(1);
        String onChainReceipt = normalizeHex(Numeric.toHexString(receiptValue.getValue()));
        if (onChainReceipt.equals(normalized)) {
          return true;
        }
      }
    } catch (Exception e) {
      log.warn("Unable to verify receipt {} on-chain", normalized, e);
    }
    return false;
  }

  private CompletableFuture<TransactionReceipt> execute(Function function) {
    String encodedFunction = FunctionEncoder.encode(function);
    return CompletableFuture.supplyAsync(() -> {
      try {
        var response = transactionManager.sendTransaction(
            gasProvider.getGasPrice(),
            gasProvider.getGasLimit(),
            props.contractAddress(),
            encodedFunction,
            BigInteger.ZERO
        );
        if (response.hasError()) {
          throw new IllegalStateException("TX error: " + response.getError().getMessage());
        }
        String txHash = response.getTransactionHash();
        log.debug("Submitted tx {}", txHash);
        return receiptProcessor.waitForTransactionReceipt(txHash);
      } catch (Exception e) {
        throw new IllegalStateException("Blockchain transaction failed", e);
      }
    });
  }

  private Bytes32 toBytes32(String hexValue) {
    String normalized = normalizeHex(hexValue);
    byte[] value = Numeric.hexStringToByteArray(normalized);
    if (value.length > 32) {
      throw new IllegalArgumentException("Value exceeds 32 bytes: " + normalized);
    }
    byte[] padded = new byte[32];
    System.arraycopy(value, 0, padded, 32 - value.length, value.length);
    return new Bytes32(padded);
  }

  private String normalizeHex(String hexValue) {
    if (hexValue == null || hexValue.isBlank()) {
      throw new IllegalArgumentException("Hex value required");
    }
    return hexValue.startsWith("0x") ? hexValue : "0x" + hexValue;
  }
}
