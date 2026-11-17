package com.esucar.api.web;

import com.esucar.api.model.Progreso;
import com.esucar.api.repo.MaterialRepo;
import com.esucar.api.repo.ProgresoRepo;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/progreso")
public class ProgresoController {

    private final ProgresoRepo progreso;
    private final MaterialRepo materiales;

    public ProgresoController(ProgresoRepo progreso, MaterialRepo materiales) {
        this.progreso = progreso;
        this.materiales = materiales;
    }

    /** IDs de materiales marcados como estudiados por el usuario. */
    @GetMapping
    public List<Integer> revisados(@RequestParam String usuario) {
        return progreso.materialesRevisados(usuario.trim().toLowerCase());
    }

    @PutMapping("/{materialId}")
    @Transactional
    public void marcar(@PathVariable Integer materialId,
                       @RequestParam String usuario,
                       @RequestParam boolean revisado) {
        String u = usuario.trim().toLowerCase();
        if (!materiales.existsById(materialId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El material no existe");
        }
        boolean existe = progreso.existsByUsuarioAndMaterialId(u, materialId);
        if (revisado && !existe) {
            progreso.save(new Progreso(u, materialId));
        } else if (!revisado && existe) {
            progreso.deleteByUsuarioAndMaterialId(u, materialId);
        }
    }
}
