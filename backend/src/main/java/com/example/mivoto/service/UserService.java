package com.example.mivoto.service;

import com.example.mivoto.model.User;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    private final Firestore db;
    // --- 1. INYECTAMOS EL SERVICIO DE BLOCKCHAIN ---
    private final BlockchainService blockchainService;

    public UserService(Firestore db, BlockchainService blockchainService) {
        this.db = db;
        this.blockchainService = blockchainService;
    }

    // --- 2. MODIFICAMOS LA LÓGICA DE ESTE MÉTODO ---
    public Optional<User> findUserByDisplayName(String displayName) throws Exception {
        
        var query = db.collection("users")
                      .whereEqualTo("displayName", displayName)
                      .limit(1)
                      .get()
                      .get();

        if (query.isEmpty()) {
            return Optional.empty(); // No se encontró
        }

        // 3. Obtenemos el usuario
        User user = query.getDocuments().get(0).toObject(User.class);

        // 4. LÓGICA DE WALLET: Verificamos si la wallet está vacía
        if (user.walletAddress() == null || user.walletAddress().isEmpty()) {
            System.out.println("Usuario " + user.displayName() + " no tiene wallet. Asignando una...");
            
            // 5. Llamamos al servicio de blockchain para crear una
            String newWalletAddress = blockchainService.createWalletForUser(user.id());

            // 6. Actualizamos el documento del usuario en Firestore
            db.collection("users").document(user.id())
              .update("walletAddress", newWalletAddress)
              .get(); // .get() espera a que la operación termine

            // 7. Devolvemos el usuario ACTUALIZADO con la nueva wallet
            return Optional.of(new User(user.id(), user.displayName(), user.email(), newWalletAddress));
        }

        // Si ya tenía wallet, solo devolvemos el usuario tal cual
        return Optional.of(user);
    }
}
