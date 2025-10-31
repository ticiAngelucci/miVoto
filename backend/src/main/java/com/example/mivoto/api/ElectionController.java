package com.example.mivoto.api;

import com.example.mivoto.model.Election;
import com.example.mivoto.service.ElectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/institutions") // Nota: La base sigue siendo /api/institutions
public class ElectionController {

    private final ElectionService electionService;

    public ElectionController(ElectionService electionService) {
        this.electionService = electionService;
    }

    // Creamos un endpoint GET en /api/institutions/{institutionId}/elections
    // @PathVariable le dice a Spring que ponga el valor de la URL en la variable
    @GetMapping("/{institutionId}/elections")
    public ResponseEntity<?> getElectionsForInstitution(@PathVariable String institutionId) {
        try {
            // 1. Llamamos a nuestro servicio con el ID
            List<Election> elections = electionService.getElectionsByInstitutionId(institutionId);

            // 2. Si la lista está vacía, no es un error, solo devolvemos un array vacío
            return ResponseEntity.ok(elections);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
