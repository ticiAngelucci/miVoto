package com.example.mivoto.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data; 
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Reemplaza el 'record' por un POJO con Lombok
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @DocumentId
    private String id; // El ID de Firestore
    private String displayName;
    private String email;
    private String walletAddress;
    // ¡No necesitas más campos!
}

