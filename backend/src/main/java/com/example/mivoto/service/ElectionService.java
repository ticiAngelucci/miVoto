package com.example.mivoto.service;

import com.example.mivoto.model.Election;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ElectionService {

    private final Firestore db;

    public ElectionService(Firestore db) {
        this.db = db;
    }

    // Lógica: Buscar elecciones por el ID de la institución
    public List<Election> getElectionsByInstitutionId(String institutionId) throws ExecutionException, InterruptedException {
        
        // 1. Creamos la consulta: "en la colección 'elections', 
        //    dame todos los docs donde 'institutionId' sea igual al que me pasaron"
        var query = db.collection("elections")
                      .whereEqualTo("institutionId", institutionId)
                      .get()
                      .get();

        // 2. Convertimos los resultados a nuestra lista de objetos Election
        return query.getDocuments()
                .stream()
                .map(doc -> doc.toObject(Election.class))
                .collect(Collectors.toList());
    }
}