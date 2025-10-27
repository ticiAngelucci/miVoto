package com.mivoto.infrastructure.blockchain;

import com.mivoto.config.Web3Properties;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
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
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;
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
          new TypeReference<Address>(true) {},
          new TypeReference<Uint256>() {}
      )
  );

  private static final String VOTE_CAST_EVENT_SIGNATURE = EventEncoder.encode(VOTE_CAST_EVENT);

  private static final class TokenState {
    private final String walletAddress;
    private boolean consumed;
    private long sbtTokenId;

    private TokenState(String walletAddress) {
      this.walletAddress = walletAddress;
      this.consumed = false;
      this.sbtTokenId = 0L;
    }
  }

  private final Web3j web3j;
  private final Web3Properties props;
  private final StaticGasProvider gasProvider;
  private final TransactionManager transactionManager;
  private final TransactionReceiptProcessor receiptProcessor;
  private final boolean mockEnabled;
  private final ConcurrentMap<String, TokenState> tokenStates = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Long> receiptTokenIds = new ConcurrentHashMap<>();
  private final AtomicLong mockTokenSequence = new AtomicLong(1L);

  public VoteContractService(Web3j web3j, Credentials credentials, Web3Properties props) {
    this.web3j = Objects.requireNonNull(web3j);
    this.props = Objects.requireNonNull(props);
    this.mockEnabled = props.isMockEnabled();
    if (mockEnabled) {
      this.gasProvider = null;
      this.receiptProcessor = null;
      this.transactionManager = null;
    } else {
      this.gasProvider = new StaticGasProvider(props.gasPriceWei(), props.gasLimitUnits());
      this.receiptProcessor = new PollingTransactionReceiptProcessor(
          web3j,
          Duration.ofSeconds(2).toMillis(),
          15
      );
      long chainId = props.chainId() != null ? props.chainId() : ChainIdLong.NONE;
      this.transactionManager = new RawTransactionManager(web3j, credentials, chainId, receiptProcessor);
    }
  }

  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500L, multiplier = 2.0))
  public CompletableFuture<TransactionReceipt> issueToken(String tokenHashHex, String walletAddress) {
    String normalizedToken = normalizeHex(tokenHashHex);
    String normalizedWallet = normalizeAddress(walletAddress);
    if (mockEnabled) {
      TokenState previous = tokenStates.putIfAbsent(normalizedToken, new TokenState(normalizedWallet));
      if (previous != null) {
        return CompletableFuture.failedFuture(new IllegalStateException("Token hash already issued"));
      }
      TransactionReceipt receipt = new TransactionReceipt();
      receipt.setTransactionHash("0xmock-token-" + normalizedToken.substring(2, Math.min(normalizedToken.length(), 10)));
      return CompletableFuture.completedFuture(receipt);
    }
    Function function = new Function(
        "issueToken",
        Arrays.asList(
            toBytes32(tokenHashHex),
            new Address(normalizedWallet)
        ),
        Collections.emptyList()
    );
    return execute(function);
  }

  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500L, multiplier = 2.0))
  public CompletableFuture<TransactionReceipt> castVote(long ballotId, String tokenHashHex, String voteHashHex, String receiptHex) {
    String normalizedToken = normalizeHex(tokenHashHex);
    String normalizedReceipt = normalizeHex(receiptHex);
    if (mockEnabled) {
      TokenState state = tokenStates.get(normalizedToken);
      if (state == null) {
        return CompletableFuture.failedFuture(new IllegalStateException("Token hash not issued"));
      }
      synchronized (state) {
        if (state.consumed) {
          return CompletableFuture.failedFuture(new IllegalStateException("Token already used"));
        }
        state.consumed = true;
        if (state.sbtTokenId == 0L) {
          state.sbtTokenId = mockTokenSequence.getAndIncrement();
        }
        receiptTokenIds.put(normalizedReceipt, state.sbtTokenId);
      }
      TransactionReceipt receipt = new TransactionReceipt();
      receipt.setTransactionHash("0xmock-vote-" + normalizedReceipt.substring(2, Math.min(normalizedReceipt.length(), 10)));
      return CompletableFuture.completedFuture(receipt);
    }
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
    if (mockEnabled) {
      return receiptTokenIds.containsKey(normalized);
    }
    EthFilter filter = new EthFilter(
        DefaultBlockParameterName.EARLIEST,
        DefaultBlockParameterName.LATEST,
        props.contractAddress()
    ).addSingleTopic(VOTE_CAST_EVENT_SIGNATURE);
    try {
      EthLog logResponse = web3j.ethGetLogs(filter).send();
      for (EthLog.LogResult<?> logResult : logResponse.getLogs()) {
        Log logEntry = (Log) logResult.get();
        if (logEntry.getTopics().isEmpty() || !VOTE_CAST_EVENT_SIGNATURE.equals(logEntry.getTopics().get(0))) {
          continue;
        }
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

  public Optional<String> extractSbtTokenId(TransactionReceipt receipt, String receiptHashHex) {
    if (receipt == null) {
      return Optional.empty();
    }
    String normalizedReceipt = normalizeHex(receiptHashHex);
    if (mockEnabled) {
      Long tokenId = receiptTokenIds.get(normalizedReceipt);
      return tokenId != null ? Optional.of(Long.toString(tokenId)) : Optional.empty();
    }
    try {
      for (Log logEntry : receipt.getLogs()) {
        List<String> topics = logEntry.getTopics();
        if (topics.isEmpty() || !VOTE_CAST_EVENT_SIGNATURE.equals(topics.get(0))) {
          continue;
        }
        List<Type> decoded = FunctionReturnDecoder.decode(
            logEntry.getData(),
            VOTE_CAST_EVENT.getNonIndexedParameters()
        );
        if (decoded.size() < 3) {
          continue;
        }
        Bytes32 receiptValue = (Bytes32) decoded.get(1);
        String onChainReceipt = normalizeHex(Numeric.toHexString(receiptValue.getValue()));
        if (!onChainReceipt.equals(normalizedReceipt)) {
          continue;
        }
        Uint256 tokenId = (Uint256) decoded.get(2);
        return Optional.of(tokenId.getValue().toString());
      }
    } catch (Exception e) {
      log.debug("Failed to decode SBT token id from receipt {}", receipt.getTransactionHash(), e);
    }
    return Optional.empty();
  }

  private CompletableFuture<TransactionReceipt> execute(Function function) {
    if (mockEnabled) {
      TransactionReceipt receipt = new TransactionReceipt();
      receipt.setTransactionHash("0xmock-" + function.getName());
      return CompletableFuture.completedFuture(receipt);
    }
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
    String cleaned = Numeric.cleanHexPrefix(hexValue);
    return Numeric.prependHexPrefix(cleaned.toLowerCase());
  }

  private String normalizeAddress(String address) {
    if (address == null || address.isBlank()) {
      throw new IllegalArgumentException("Address required");
    }
    String cleaned = Numeric.cleanHexPrefix(address);
    if (cleaned.length() != 40) {
      throw new IllegalArgumentException("Address must have 20 bytes");
    }
    return Numeric.prependHexPrefix(cleaned.toLowerCase());
  }
}
