package com.example.mivoto.api;

import com.example.mivoto.dto.VoteRequest;
import com.example.mivoto.service.VoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping("/vote") // Endpoint: POST /api/vote
    public ResponseEntity<?> castVote(@RequestBody VoteRequest voteRequest) {
        try {
            // 1. Intentamos procesar el voto llamando al servicio
            Map<String, String> response = voteService.processVote(voteRequest);
            
            // 2. Si todo sale bien, devolvemos 200 OK con el mensaje de éxito
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            // 3. Si el VoteService lanzó la excepción de "doble voto"
            // Devolvemos un error 409 Conflict (o 403 Forbidden)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // 4. Cualquier otro error (ej. falló la blockchain)
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Error interno al procesar el voto."));
        }
    }
}
