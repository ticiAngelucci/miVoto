package com.example.mivoto.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data; // Importa las anotaciones de Lombok
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Lombok generará automáticamente: Getters, Setters, toString, hashCode y equals
@Data
// Los constructores son necesarios para que Firestore y Jackson puedan crear el objeto
@NoArgsConstructor
@AllArgsConstructor
public class Institution {

    @DocumentId
    private String id;
    private String name;
    private String description;
    private boolean active;
    private int membersCount;
}
