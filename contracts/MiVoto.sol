// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/security/ReentrancyGuard.sol";

/**
 * @title MiVotoSoulboundToken
 * @dev Token ERC-721 no transferible usado como comprobante de voto.
 */
contract MiVotoSoulboundToken is ERC721, Ownable {
    uint256 private _nextTokenId = 1;
    address public minter;

    constructor(address initialMinter)
        ERC721("MiVoto Proof of Vote", "MI-VOTE-SBT")
        Ownable()
    {
        require(initialMinter != address(0), "Minter requerido");
        minter = initialMinter;
    }

    function _beforeTokenTransfer(
        address from,
        address to,
        uint256 tokenId,
        uint256 batchSize
    ) internal virtual override {
        require(
            from == address(0) || to == address(0),
            "SBT: intransferible"
        );
        super._beforeTokenTransfer(from, to, tokenId, batchSize);
    }

    function setMinter(address newMinter) external onlyOwner {
        require(newMinter != address(0), "Minter invalido");
        minter = newMinter;
    }

    function safeMint(address to) external returns (uint256) {
        require(msg.sender == minter, "Solo minter");
        require(to != address(0), "Destinatario invalido");
        uint256 tokenId = _nextTokenId++;
        _safeMint(to, tokenId);
        return tokenId;
    }
}

/**
 * @title MiVotoElection
 * @dev Gestiona emisión de tokens de elegibilidad y registro de votos on-chain.
 */
contract MiVotoElection is Ownable, ReentrancyGuard {
    struct Eligibility {
        address voter;
        bool consumed;
        uint256 tokenId;
    }

    MiVotoSoulboundToken public immutable sbt;
    mapping(bytes32 => Eligibility) private eligibilities;
    mapping(bytes32 => bool) private receipts;

    event TokenIssued(bytes32 indexed tokenHash, address indexed voter);
    event VoteCast(
        uint256 indexed ballotId,
        bytes32 indexed tokenHash,
        bytes32 voteHash,
        bytes32 receiptHash,
        address indexed voter,
        uint256 tokenId
    );

    constructor(address sbtAddress) Ownable() {
        require(sbtAddress != address(0), "SBT requerido");
        sbt = MiVotoSoulboundToken(sbtAddress);
    }

    function issueToken(bytes32 tokenHash, address voter) external onlyOwner {
        require(tokenHash != bytes32(0), "Token requerido");
        require(voter != address(0), "Votante requerido");
        Eligibility storage info = eligibilities[tokenHash];
        require(info.voter == address(0), "Token ya emitido");

        eligibilities[tokenHash] = Eligibility({
            voter: voter,
            consumed: false,
            tokenId: 0
        });

        emit TokenIssued(tokenHash, voter);
    }

    function castVote(
        uint256 ballotId,
        bytes32 tokenHash,
        bytes32 voteHash,
        bytes32 receiptHash
    ) external onlyOwner nonReentrant returns (uint256) {
        require(voteHash != bytes32(0), "Hash de voto requerido");
        require(receiptHash != bytes32(0), "Recibo requerido");

        Eligibility storage info = eligibilities[tokenHash];
        require(info.voter != address(0), "Token no emitido");
        require(!info.consumed, "Token ya utilizado");
        require(!receipts[receiptHash], "Recibo duplicado");

        info.consumed = true;
        receipts[receiptHash] = true;

        uint256 tokenId = info.tokenId;
        if (tokenId == 0) {
            tokenId = sbt.safeMint(info.voter);
            info.tokenId = tokenId;
        }

        emit VoteCast(ballotId, tokenHash, voteHash, receiptHash, info.voter, tokenId);
        return tokenId;
    }

    function voterForToken(bytes32 tokenHash) external view returns (address) {
        return eligibilities[tokenHash].voter;
    }

    function tokenConsumed(bytes32 tokenHash) external view returns (bool) {
        return eligibilities[tokenHash].consumed;
    }

    function isReceiptRegistered(bytes32 receiptHash) external view returns (bool) {
        return receipts[receiptHash];
    }
}
