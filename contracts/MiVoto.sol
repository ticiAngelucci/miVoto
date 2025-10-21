// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

/// @title MiVoto smart contract minimal
/// @notice Emits tokens and receipts to prevent double voting.
contract MiVoto {
    mapping(bytes32 => bool) public consumedToken;

    event TokenIssued(bytes32 indexed tokenHash, address indexed issuer);
    event VoteCast(
        uint256 indexed ballotId,
        bytes32 indexed tokenHash,
        bytes32 voteHash,
        bytes32 receipt,
        address indexed relayer
    );

    /// @notice Registers a token hash without consuming it yet.
    function issueToken(bytes32 tokenHash) external {
        require(!consumedToken[tokenHash], "Already used or issued");
        emit TokenIssued(tokenHash, msg.sender);
    }

    /// @notice Consumes the token hash and emits vote receipt.
    function castVote(
        uint256 ballotId,
        bytes32 tokenHash,
        bytes32 voteHash,
        bytes32 receipt
    ) external {
        require(!consumedToken[tokenHash], "Token already used");
        consumedToken[tokenHash] = true;
        emit VoteCast(ballotId, tokenHash, voteHash, receipt, msg.sender);
    }
}
