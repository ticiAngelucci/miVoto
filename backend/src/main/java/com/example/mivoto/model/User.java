package com.example.mivoto.model;

import com.google.cloud.firestore.annotation.DocumentId;

// Modelo que representa tu colección "users"
public record User(
    @DocumentId String id, // El ID de Firestore
    String displayName,
    String email,
    String walletAddress
) {}
