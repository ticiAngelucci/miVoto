package com.mivoto.config;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class InfraConfig {

  @Bean
  @Profile("!dev")
  public FirebaseApp firebaseApp(FirebaseProperties properties) throws IOException {
    FirebaseOptions.Builder builder = FirebaseOptions.builder().setProjectId(properties.projectId());
    GoogleCredentials credentials;
    if (properties.useEmulator()) {
      AccessToken token = new AccessToken("emulator-token",
          new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)));
      credentials = GoogleCredentials.create(token);
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
  @Profile("!dev")
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
  public Credentials blockchainCredentials(Web3Properties properties,
      @Value("${WEB3_PRIVATE_KEY:}") String privateKey) {
    if (properties.isMockEnabled()) {
      return Credentials.create("0x0000000000000000000000000000000000000000000000000000000000000001");
    }
    if (privateKey == null || privateKey.isBlank()) {
      throw new IllegalStateException("WEB3_PRIVATE_KEY must be configured");
    }
    return Credentials.create(privateKey);
  }
}
