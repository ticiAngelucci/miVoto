package com.example.mivoto.service;

public interface BlockchainService {

    // 1. (Sin cambios) Sigue siendo necesario para el login simulado
    String createWalletForUser(String userId) throws Exception;

    // 2. (NUEVO - Reemplaza a submitAnonymousVote)
    // Coincide con MiVotoElection.issueToken()
    void issueToken(String userId, String electionId, String userWalletAddress) throws Exception;

    // 3. (NUEVO - Reemplaza a mintSBT)
    // Coincide con MiVotoElection.castVote()
    // Devuelve el ID del SBT minteado (como String para el mock)
    String castVote(String userId, String electionId, String candidateId) throws Exception;
}