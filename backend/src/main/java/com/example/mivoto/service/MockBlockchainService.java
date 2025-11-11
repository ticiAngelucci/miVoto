package com.example.mivoto.service;

import org.springframework.stereotype.Service;

@Service 
public class MockBlockchainService implements BlockchainService {

    // 1. (Sin cambios)
    @Override
    public String createWalletForUser(String userId) throws Exception {
        String fakeWallet = "0xWallet_" + Math.abs(userId.hashCode());
        System.out.println("====== MOCK BLOCKCHAIN ======");
        System.out.println("Creando nueva Wallet para Usuario: " + userId);
        System.out.println("... Wallet Asignada (simulada): " + fakeWallet);
        System.out.println("=============================");
        return fakeWallet;
    }

    // 2. (NUEVO) Simula la emisión del token de elegibilidad
    @Override
    public void issueToken(String userId, String electionId, String userWalletAddress) throws Exception {
        System.out.println("====== MOCK BLOCKCHAIN ======");
        System.out.println("REGISTRANDO ELEGIBILIDAD (issueToken)");
        System.out.println("... Usuario: " + userId);
        System.out.println("... Elección: " + electionId);
        System.out.println("... Wallet: " + userWalletAddress);
        System.out.println("=============================");
        // No hace nada, solo simula el éxito
    }

    // 3. (NUEVO) Simula el voto Y el minteo del SBT
    @Override
    public String castVote(String userId, String electionId, String candidateId) throws Exception {
        System.out.println("====== MOCK BLOCKCHAIN ======");
        System.out.println("EMITIENDO VOTO (castVote)");
        System.out.println("... Voto anónimo por: " + candidateId);
        
        // Simulación de que castVote() mintea el SBT internamente
        String fakeSbtId = "SBT_TX_HASH_0x" + Math.abs((userId + electionId).hashCode());
        System.out.println("... SBT minteado internamente. Token ID: " + fakeSbtId);
        System.out.println("=============================");
        
        return fakeSbtId;
    }
}