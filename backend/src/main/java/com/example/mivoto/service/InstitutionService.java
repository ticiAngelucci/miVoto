package com.example.mivoto.service;

import com.example.mivoto.model.Institution;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service // Le dice a Spring que esta es una clase de Servicio
public class InstitutionService {

    private final Firestore db;

    // Spring inyectará automáticamente el bean de Firestore que ya creaste
    public InstitutionService(Firestore db) {
        this.db = db;
    }

    // Método para obtener TODAS las instituciones (para empezar)
    public List<Institution> getAllInstitutions() throws ExecutionException, InterruptedException {
        
        // 1. Apuntamos a la colección
        var collection = db.collection("institutions");
        
        // 2. Pedimos los documentos
        var snapshot = collection.get().get();

        // 3. Convertimos cada documento a nuestro objeto Institution
        return snapshot.getDocuments()
                .stream()
                .map(doc -> doc.toObject(Institution.class))
                .collect(Collectors.toList());
    }
}

