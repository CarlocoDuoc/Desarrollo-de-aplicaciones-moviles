package com.esucar.api.web;

import com.esucar.api.model.Material;
import com.esucar.api.repo.*;
import com.esucar.api.web.Dtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final AsignaturaRepo asignaturas;
    private final UnidadRepo unidades;
    private final MaterialRepo materiales;

    public CatalogoController(AsignaturaRepo asignaturas, UnidadRepo unidades, MaterialRepo materiales) {
        this.asignaturas = asignaturas;
        this.unidades = unidades;
        this.materiales = materiales;
    }

    @GetMapping("/asignaturas")
    public List<AsignaturaRes> asignaturas() {
        return asignaturas.findAll().stream()
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .map(AsignaturaRes::de).toList();
    }

    @GetMapping("/unidades")
    public List<UnidadRes> unidades() {
        return unidades.findAllByOrderByAsignaturaIdAscNumeroAsc().stream().map(UnidadRes::de).toList();
    }

    @GetMapping("/materiales")
    public List<MaterialRes> materiales() {
        return materiales.findAllByOrderByIdAsc().stream().map(MaterialRes::de).toList();
    }

    @PostMapping("/materiales")
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialRes publicar(@Valid @RequestBody MaterialReq req) {
        if (!unidades.existsById(req.unidadId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La unidad no existe");
        }
        String desc = req.descripcion() == null || req.descripcion().isBlank()
                ? "Sin descripción." : req.descripcion().trim();
        Material m = materiales.save(new Material(req.unidadId(), req.titulo().trim(), req.tipo(), desc));
        return MaterialRes.de(m);
    }
}
