package com.example.mivoto.api;

import com.example.mivoto.model.Institution;
import com.example.mivoto.service.InstitutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/institutions") // La URL base para todo lo de instituciones
public class InstitutionController {

    private final InstitutionService institutionService;

    // Spring inyecta el Servicio que acabamos de crear
    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    // Definimos un endpoint GET en /api/institutions
    @GetMapping
    public ResponseEntity<List<Institution>> getAllInstitutions() {
        try {
            // 1. Llamamos a nuestra lógica de negocio
            List<Institution> institutions = institutionService.getAllInstitutions();
            
            // 2. Devolvemos la lista como JSON con un código 200 OK
            return ResponseEntity.ok(institutions);
            
        } catch (Exception e) {
            // Si algo falla (ej. no hay conexión con Firestore)
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
