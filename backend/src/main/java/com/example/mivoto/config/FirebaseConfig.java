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

  @Bean
  public Firestore firestore(@Value("${app.firebase.projectId}") String projectId) throws Exception {
    var credsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
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