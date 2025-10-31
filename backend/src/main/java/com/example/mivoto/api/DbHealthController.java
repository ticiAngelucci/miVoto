package com.example.mivoto.api;

import com.google.cloud.firestore.Firestore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;
import java.util.Map;

@RestController
public class DbHealthController {

  private final Firestore db;
  public DbHealthController(Firestore db) { this.db = db; }

  @GetMapping("/health/db")
  public Map<String, Object> ping() throws Exception {
    var ref = db.collection("connectivity-check").document("ping");
    var ts = Instant.now().toString();
    ref.set(Map.of("last", ts)).get();   // write
    var snap = ref.get().get();          // read
    return Map.of(
        "firestoreProject", db.getOptions().getProjectId(),
        "lastWrite", snap.getString("last")
    );
  }
}