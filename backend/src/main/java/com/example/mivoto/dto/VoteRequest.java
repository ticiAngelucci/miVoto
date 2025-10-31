package com.example.mivoto.dto;

// Usamos un 'record' para definir el JSON que esperamos recibir
// El frontend debe enviarnos estos TRES datos
public record VoteRequest(
    String userId,       // El ID del usuario que se logueó
    String electionId,   // El ID de la elección que vota
    String candidateId   // Por quién vota (Este dato NO se guardará en Firestore)
) {}
