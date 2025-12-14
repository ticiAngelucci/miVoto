package com.example.mivoto.api;

import com.example.mivoto.model.Institution;
import com.example.mivoto.service.InstitutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map; // <-- ¡ASEGÚRATE DE AGREGAR ESTE IMPORT!

// (Ya no necesitamos los imports de Logger)
@CrossOrigin(origins = "${FRONT_URL:https://mi-voto-theta.vercel.app}")
@RestController
@RequestMapping("/api/institutions") 
public class InstitutionController {

    private final InstitutionService institutionService;

    // (Ya no necesitamos el Logger)

    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @GetMapping
    // Cambiamos el tipo de retorno a un genérico ResponseEntity<?>
    public ResponseEntity<?> getAllInstitutions() {
        try {
            List<Institution> institutions = institutionService.getAllInstitutions();
            return ResponseEntity.ok(institutions);

        } catch (Exception e) {
            // --- ¡NUEVO BLOQUE CATCH! ---
            // Vamos a devolver el mensaje de error en el body de la respuesta
            String errorMessage = (e.getMessage() != null) ? e.getMessage() : "Causa desconocida (e.getMessage() es null)";

            // Imprimimos solo el mensaje de error (más seguro que el objeto 'e' completo)
            System.out.println("### ERROR CAPTURADO: " + errorMessage);

            // Devolvemos un 500 pero esta vez CON el mensaje de error
            return ResponseEntity.status(500).body(Map.of("error", errorMessage));
        }
    }
}
