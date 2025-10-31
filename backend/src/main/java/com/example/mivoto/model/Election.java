package com.example.mivoto.model;

import com.google.cloud.firestore.annotation.DocumentId;
import java.util.List; // Importamos List para el campo 'candidates'

// Modelo que representa tu colección "elections"
public record Election(
    @DocumentId String id,
    String name,
    String status,
    String institutionId,
    String voteRule,
    String startAt,       // Lo dejamos como String por ahora, tal como está en tu DB
    String endAt,         // Ídem
    List<String> candidates // Firestore puede mapear arrays de JSON a Listas de Java
) {}