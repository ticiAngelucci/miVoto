package com.example.mivoto.service;

public interface BlockchainService {

    // (Este ya lo teníamos)
    void submitAnonymousVote(String electionId, String candidateId) throws Exception;

    // (Este ya lo teníamos)
    String mintSBT(String userId, String electionId) throws Exception;
    
    // --- NUEVO MÉTODO ---
    // Asigna una nueva wallet a un usuario
    String createWalletForUser(String userId) throws Exception;
}