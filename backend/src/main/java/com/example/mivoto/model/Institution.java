package com.example.mivoto.model;

// Importante: Necesitas esta anotación para que Firestore mapee el ID del documento
import com.google.cloud.firestore.annotation.DocumentId;

// Usamos un 'record' de Java. Es una clase de datos simple.
public record Institution(
    @DocumentId String id, // Esto capturará el ID automático (ej. 5TtpcI8...)
    String name,
    String description,
    boolean active,
    int membersCount
) {
    // ¡No necesitas nada más! Ni constructores, ni getters/setters.
}
