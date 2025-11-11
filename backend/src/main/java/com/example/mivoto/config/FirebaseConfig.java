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

@Configuration
public class FirebaseConfig {

  // La inyección del projectId SÍ funciona porque está en application.yaml
  @Value("${app.firebase.projectId}")
  private String projectId;

  @Bean
  public Firestore firestore() throws Exception {
    
    // 1. Lee la variable de entorno que SÍ existe en Render y en compose.yaml
    var credsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
    
    // 2. Esta es la validación que viste en el error de Render (¡está bien!)
    if (credsPath == null || credsPath.isEmpty()) {
        throw new RuntimeException("La variable de entorno GOOGLE_APPLICATION_CREDENTIALS no está definida.");
    }

    // 3. Usa esa ruta para cargar el archivo
    var creds = GoogleCredentials.fromStream(new FileInputStream(credsPath));

    if (FirebaseApp.getApps().isEmpty()) {
      FirebaseOptions options = FirebaseOptions.builder()
          .setCredentials(creds)
          .setProjectId(projectId)
          .build();
      FirebaseApp.initializeApp(options);
    }

    return FirestoreOptions.newBuilder()
        .setProjectId(projectId)
        .setCredentials(creds)
        .build()
        .getService();
  }
}
