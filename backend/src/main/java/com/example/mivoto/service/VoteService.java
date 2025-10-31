package com.example.mivoto.service;

import com.example.mivoto.dto.VoteRequest;
import com.example.mivoto.model.Ballot;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class VoteService {

    private final Firestore db;
    private final BlockchainService blockchainService; // Inyectamos LA INTERFAZ

    public VoteService(Firestore db, BlockchainService blockchainService) {
        this.db = db;
        this.blockchainService = blockchainService; // Spring pondrá aquí el MockService
    }

    // El método principal de lógica de negocio
    public Map<String, String> processVote(VoteRequest request) throws Exception {

        // 1. VERIFICAR DOBLE VOTO
        // Buscamos en la colección 'ballots' si ya existe un documento
        // con el mismo userId Y el mismo electionId
        var ballotQuery = db.collection("ballots")
                            .whereEqualTo("userId", request.userId())
                            .whereEqualTo("electionId", request.electionId())
                            .get()
                            .get();

        // Si la consulta NO está vacía, significa que ya votó.
        if (!ballotQuery.isEmpty()) {
            // Lanzamos una excepción que el Controlador atrapará
            throw new IllegalStateException("Error: El usuario ya ha votado en esta elección.");
        }

        // 2. ENVIAR VOTO ANÓNIMO A BLOCKCHAIN
        // Si no ha votado, procedemos.
        // Llamamos al servicio de blockchain SÓLO con los datos anónimos.
        blockchainService.submitAnonymousVote(request.electionId(), request.candidateId());

        // 3. REGISTRAR PARTICIPACIÓN EN FIRESTORE
        // Si el paso 2 no falló, creamos el "recibo" en nuestra DB
        // para prevenir que vote de nuevo. (Nota: sin candidateId)
        Ballot newBallot = new Ballot(
            null,
            request.userId(),
            request.electionId(),
            "VOTED",
            Instant.now()
        );
        
        // Guardamos el nuevo documento
        db.collection("ballots").add(newBallot).get();

        // 4. GENERAR SBT (Comprobante)
        // (Esto podría fallar, en un proyecto real se manejaría en una cola)
        String sbtId = blockchainService.mintSBT(request.userId(), request.electionId());

        // 5. Devolver éxito
        return Map.of(
            "message", "Voto registrado con éxito",
            "sbtTransactionId", sbtId
        );
    }
}