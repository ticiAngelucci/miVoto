package com.example.mivoto.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException; // Importación necesaria

@Configuration
public class FirebaseConfig {

    // 1. Inyecta la ruta de montaje del Secret File que definiste en Render
    //    (Ejemplo de valor en Render: /etc/secrets/firebase-key.json)
    @Value("${FIREBASE_CREDENTIALS_PATH}")
    private String firebaseCredentialsPath;

    @Bean
    public Firestore firestore(@Value("${app.firebase.projectId}") String projectId) throws Exception {

        // 2. ¡Comprobación de seguridad! Esto evitará el NullPointerException si la variable no existe.
        if (firebaseCredentialsPath == null || firebaseCredentialsPath.isEmpty()) {
            throw new IllegalArgumentException(
                "La ruta de credenciales de Firebase (FIREBASE_CREDENTIALS_PATH) no está configurada en Render."
            );
        }
        
        // 3. Usa la ruta inyectada directamente para abrir el archivo.
        FileInputStream serviceAccount;
        try {
            serviceAccount = new FileInputStream(firebaseCredentialsPath);
        } catch (FileNotFoundException e) {
            // Este error significa que Render NO montó el Secret File en la ruta especificada.
            System.err.println("ERROR: No se pudo encontrar el archivo de credenciales de Firebase en la ruta: " + firebaseCredentialsPath);
            throw new RuntimeException("Fallo al cargar las credenciales de Firebase. Revise la ruta del Secret File en Render.", e);
        }

        var creds = GoogleCredentials.fromStream(serviceAccount);

        // 4. Inicialización de Firebase
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(creds)
                .setProjectId(projectId)
                .build();
            FirebaseApp.initializeApp(options);
        }

        // 5. Devolución de la instancia de Firestore
        return FirestoreOptions.newBuilder()
            .setProjectId(projectId)
            .setCredentials(creds)
            .build()
            .getService();
    }
}
