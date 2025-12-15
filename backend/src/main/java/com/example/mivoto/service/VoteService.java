package com.example.mivoto.service;

import com.example.mivoto.dto.VoteRequest;
import com.example.mivoto.model.Ballot;
import com.example.mivoto.model.User; // <-- ¡IMPORTANTE! Asegúrate de importar User
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class VoteService {

    private final Firestore db;
    private final BlockchainService blockchainService; 
    
    // Inyecta el UserService para poder buscar la wallet del usuario
    private final UserService userService;

    public VoteService(Firestore db, BlockchainService blockchainService, UserService userService) {
        this.db = db;
        this.blockchainService = blockchainService;
        this.userService = userService;
    }

    public Map<String, String> processVote(VoteRequest request) throws Exception {

        // 1. VERIFICAR DOBLE VOTO (Sin cambios)
        var ballotQuery = db.collection("ballots")
                            .whereEqualTo("userId", request.userId())
                            .whereEqualTo("electionId", request.electionId())
                            .get()
                            .get();

        if (!ballotQuery.isEmpty()) {
            throw new IllegalStateException("Error: El usuario ya ha votado en esta elección.");
        }

        // --- INICIO DE CAMBIOS ---

        // 2. OBTENER WALLET DEL USUARIO
        // Buscamos al usuario en Firestore para obtener su walletAddress
        User user = db.collection("users").document(request.userId()).get().get().toObject(User.class);
        if (user == null || user.getWalletAddress() == null || user.getWalletAddress().isEmpty()) {
            throw new IllegalStateException("Error: El usuario no tiene una wallet asignada.");
        }
        String userWalletAddress = user.getWalletAddress();


        // 3. REGISTRAR ELEGIBILIDAD (NUEVO PASO)
        // Llamamos a issueToken ANTES de votar
        blockchainService.issueToken(request.userId(), request.electionId(), userWalletAddress);

        
        // 4. REGISTRAR PARTICIPACIÓN EN FIRESTORE (Sin cambios)
        // Guardamos el recibo ANTES de votar para asegurar el bloqueo
        Ballot newBallot = new Ballot(
            null,
            request.userId(),
            request.electionId(),
            "VOTED",
            Instant.now()
        );
        // Obtenemos la referencia del nuevo documento
        var ballotRef = db.collection("ballots").add(newBallot).get();
        String ballotDocumentId = ballotRef.getId();

        // 5. EMITIR VOTO Y MINTEAR SBT (MÉTODO ACTUALIZADO)
        // Llamamos a castVote. Ya no llamamos a mintSBT por separado.
        VoteExecutionResult executionResult = blockchainService.castVote(
            request.userId(),
            request.electionId(),
            request.candidateId(),
            ballotDocumentId
        );
        
        // --- FIN DE CAMBIOS ---

        // 6. Devolver éxito
        return Map.of(
            "message", "Voto registrado con éxito",
            "voteTransactionHash", executionResult.voteTransactionHash(),
            "sbtTransactionHash", executionResult.sbtTransactionHash(),
            "sbtTokenId", executionResult.sbtTokenId()
        );
    }
}
