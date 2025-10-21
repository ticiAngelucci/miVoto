package com.mivoto.app.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class InfraConfig {

  @Bean
  public FirebaseApp firebaseApp(FirebaseProperties properties) throws IOException {
    FirebaseOptions.Builder builder = FirebaseOptions.builder().setProjectId(properties.projectId());
    GoogleCredentials credentials;
    if (properties.useEmulator()) {
      credentials = GoogleCredentials.fromStream(new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)));
      System.setProperty("FIRESTORE_EMULATOR_HOST", properties.emulatorAuthority());
    } else {
      Path credentialsPath = Path.of(Objects.requireNonNull(properties.credentialsFile(), "credentialsFile must be set"));
      try (InputStream stream = Files.newInputStream(credentialsPath)) {
        credentials = GoogleCredentials.fromStream(stream);
      }
    }
    builder.setCredentials(credentials);
    return FirebaseApp.initializeApp(builder.build());
  }

  @Bean
  public Firestore firestore(FirebaseApp firebaseApp, FirebaseProperties properties) {
    FirestoreOptions.Builder builder = FirestoreOptions.newBuilder().setProjectId(properties.projectId());
    if (properties.useEmulator()) {
      builder.setEmulatorHost(properties.emulatorAuthority());
    }
    return builder.build().getService();
  }

  @Bean
  public Web3j web3j(Web3Properties props) {
    HttpService service = new HttpService(props.rpcUrl());
    service.addHeader("User-Agent", "mivoto-backend");
    return Web3j.build(service);
  }

  @Bean
  public Credentials blockchainCredentials(@Value("${WEB3_PRIVATE_KEY:}") String privateKey) {
    if (privateKey == null || privateKey.isBlank()) {
      throw new IllegalStateException("WEB3_PRIVATE_KEY must be configured");
    }
    return Credentials.create(privateKey);
  }
}
