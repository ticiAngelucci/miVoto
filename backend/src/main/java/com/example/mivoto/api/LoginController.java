package com.example.mivoto.api;

import com.example.mivoto.model.User;
import com.example.mivoto.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

// Un DTO (Data Transfer Object) simple para recibir el JSON del login
// El frontend enviará: {"displayName": "ticiangelucci"}
record LoginRequest(String displayName) {}


@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login") // Endpoint: POST /api/auth/login
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 1. Llamamos al servicio para que busque al usuario
            Optional<User> userOptional = userService.findUserByDisplayName(loginRequest.displayName());

            // 2. Verificamos si el servicio lo encontró
            if (userOptional.isPresent()) {
                // ¡Éxito! Devolvemos los datos del usuario
                return ResponseEntity.ok(userOptional.get());
            } else {
                // No se encontró. Devolvemos un error 404 (Not Found)
                return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor"));
        }
    }
}
