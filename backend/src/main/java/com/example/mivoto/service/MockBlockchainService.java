package com.example.mivoto.service;

import org.springframework.stereotype.Service;

// Esta es la implementación SIMULADA.
// La anotación @Service hace que Spring la use cuando pidamos un "BlockchainService"
@Service 
public class MockBlockchainService implements BlockchainService {

    // --- IMPLEMENTACIÓN DEL MÉTODO DE VOTAR (DEL PASO 4) ---
    @Override
    public void submitAnonymousVote(String electionId, String candidateId) throws Exception {
        // Simulación: Imprimimos en la consola del backend
        System.out.println("====== MOCK BLOCKCHAIN ======");
        System.out.println("Recibido voto ANÓNIMO para elección: " + electionId);
        System.out.println("Voto por: " + candidateId);
        System.out.println("... Voto enviado al Smart Contract (simulado).");
        System.out.println("=============================");
        
        // No lanzamos error para simular que salió todo bien
    }

    // --- IMPLEMENTACIÓN DEL MÉTODO DE MINTEAR SBT (DEL PASO 4) ---
    @Override
    public String mintSBT(String userId, String electionId) throws Exception {
        String fakeSbtId = "SBT_TX_HASH_0x" + Math.abs(userId.hashCode());
        
        System.out.println("====== MOCK BLOCKCHAIN ======");
        System.out.println("Minteando SBT para Usuario: " + userId);
        System.out.println("En elección: " + electionId);
        System.out.println("... SBT generado (simulado): " + fakeSbtId);
        System.out.println("=============================");
        
        return fakeSbtId;
    }

    // --- IMPLEMENTACIÓN DEL NUEVO MÉTODO (DEL PASO 5) ---
    @Override
    public String createWalletForUser(String userId) throws Exception {
        // Creamos una dirección de wallet falsa pero única para el usuario
        String fakeWallet = "0xWallet_" + Math.abs(userId.hashCode());
        
        System.out.println("====== MOCK BLOCKCHAIN ======");
        System.out.println("Creando nueva Wallet para Usuario: " + userId);
        System.out.println("... Wallet Asignada (simulada): " + fakeWallet);
        System.out.println("=============================");
        
        return fakeWallet;
    }
}