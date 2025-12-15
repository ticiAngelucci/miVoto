package com.example.mivoto.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.mivoto.config.BlockchainProperties;

import jakarta.annotation.PreDestroy;
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
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

@Service
@Primary
@Profile("!mock")
public class Web3BlockchainService implements BlockchainService {

    private static final Event VOTE_CAST_EVENT = new Event(
        "VoteCast",
        Arrays.asList(
            new TypeReference<Uint256>() {},
            new TypeReference<Bytes32>() {},
            new TypeReference<Bytes32>() {},
            new TypeReference<Bytes32>() {},
            new TypeReference<Address>() {},
            new TypeReference<Uint256>() {}
        )
    );

    private static final String VOTE_CAST_TOPIC = EventEncoder.encode(VOTE_CAST_EVENT);

    private final Web3j web3j;
    private final TransactionManager transactionManager;
    private final ContractGasProvider gasProvider;
    private final String electionContractAddress;

    public Web3BlockchainService(BlockchainProperties properties) {
        validateProperties(properties);
        this.web3j = Web3j.build(new HttpService(properties.getProviderUrl()));
        Credentials credentials = Credentials.create(properties.getPrivateKey());
        this.transactionManager = new RawTransactionManager(web3j, credentials, properties.getChainId());
        this.gasProvider = new StaticGasProvider(properties.getGasPrice(), properties.getGasLimit());
        this.electionContractAddress = properties.getElectionContractAddress();
    }

    private void validateProperties(BlockchainProperties properties) {
        if (!StringUtils.hasText(properties.getProviderUrl())) {
            throw new IllegalStateException("blockchain.providerUrl no está configurado");
        }
        if (!StringUtils.hasText(properties.getPrivateKey())) {
            throw new IllegalStateException("blockchain.privateKey no está configurado");
        }
        if (!StringUtils.hasText(properties.getElectionContractAddress())) {
            throw new IllegalStateException("blockchain.electionContractAddress no está configurado");
        }
    }

    @Override
    public String createWalletForUser(String userId) throws Exception {
        ECKeyPair keyPair = Keys.createEcKeyPair();
        return "0x" + Keys.getAddress(keyPair);
    }

    @Override
    public void issueToken(String userId, String electionId, String userWalletAddress) throws Exception {
        Bytes32 tokenHash = new Bytes32(buildTokenHash(userId, electionId));
        Address voter = new Address(normalizeAddress(userWalletAddress));
        Function function = new Function(
            "issueToken",
            Arrays.asList(tokenHash, voter),
            Collections.emptyList()
        );
        executeTransaction(function, "issueToken");
    }

    @Override
    public VoteExecutionResult castVote(String userId, String electionId, String candidateId, String ballotDocumentId) throws Exception {
        Bytes32 tokenHash = new Bytes32(buildTokenHash(userId, electionId));
        Bytes32 voteHash = new Bytes32(buildVoteHash(electionId, candidateId));
        Bytes32 receiptHash = new Bytes32(buildReceiptHash(userId, electionId, candidateId, ballotDocumentId));
        Uint256 ballotId = new Uint256(mapBallotId(ballotDocumentId));

        Function function = new Function(
            "castVote",
            Arrays.asList(ballotId, tokenHash, voteHash, receiptHash),
            Collections.emptyList()
        );
        TransactionReceipt receipt = executeTransaction(function, "castVote");
        BigInteger tokenId = extractTokenId(receipt);
        String txHash = receipt.getTransactionHash();
        return new VoteExecutionResult(txHash, txHash, tokenId.toString());
    }

    private TransactionReceipt executeTransaction(Function function, String operationName) throws Exception {
        String data = FunctionEncoder.encode(function);
        EthSendTransaction response = transactionManager.sendTransaction(
            gasProvider.getGasPrice(operationName),
            gasProvider.getGasLimit(operationName),
            electionContractAddress,
            data,
            BigInteger.ZERO
        );

        if (response.hasError()) {
            throw new IllegalStateException("Fallo " + operationName + ": " + response.getError().getMessage());
        }

        String txHash = response.getTransactionHash();
        return waitForReceipt(txHash, operationName);
    }

    private TransactionReceipt waitForReceipt(String txHash, String operation) throws Exception {
        int attempts = 40;
        long delayMs = 1500;
        for (int i = 0; i < attempts; i++) {
            EthGetTransactionReceipt receiptResponse = web3j.ethGetTransactionReceipt(txHash).send();
            if (receiptResponse.getTransactionReceipt().isPresent()) {
                return receiptResponse.getTransactionReceipt().get();
            }
            Thread.sleep(delayMs);
        }
        throw new IllegalStateException("No se confirmó la transacción " + operation + " (" + txHash + ")");
    }

    private BigInteger extractTokenId(TransactionReceipt receipt) {
        for (Log log : receipt.getLogs()) {
            List<String> topics = log.getTopics();
            if (topics == null || topics.isEmpty()) {
                continue;
            }
            if (!topics.get(0).equals(VOTE_CAST_TOPIC)) {
                continue;
            }
            List<Type> decoded = FunctionReturnDecoder.decode(
                log.getData(),
                VOTE_CAST_EVENT.getParameters()
            );
            if (decoded.size() == 6) {
                Type tokenType = decoded.get(5);
                if (tokenType instanceof Uint256 uint256) {
                    return uint256.getValue();
                }
            }
        }
        return BigInteger.ZERO;
    }

    private byte[] buildTokenHash(String userId, String electionId) {
        return hash("TOKEN:" + userId + ":" + electionId);
    }

    private byte[] buildVoteHash(String electionId, String candidateId) {
        return hash("VOTE:" + electionId + ":" + candidateId);
    }

    private byte[] buildReceiptHash(String userId, String electionId, String candidateId, String ballotId) {
        return hash("RECEIPT:" + userId + ":" + electionId + ":" + candidateId + ":" + ballotId);
    }

    private byte[] hash(String raw) {
        byte[] hashed = Hash.sha3(raw.getBytes(StandardCharsets.UTF_8));
        return Arrays.copyOf(hashed, 32);
    }

    private String normalizeAddress(String address) {
        if (!StringUtils.hasText(address)) {
            throw new IllegalArgumentException("Wallet del votante es requerido");
        }
        String normalized = address.startsWith("0x") ? address : "0x" + address;
        return Keys.toChecksumAddress(normalized);
    }

    private BigInteger mapBallotId(String ballotDocumentId) {
        byte[] hashed = Hash.sha3(ballotDocumentId.getBytes(StandardCharsets.UTF_8));
        return new BigInteger(1, hashed);
    }

    @PreDestroy
    public void shutdown() {
        web3j.shutdown();
    }
}
