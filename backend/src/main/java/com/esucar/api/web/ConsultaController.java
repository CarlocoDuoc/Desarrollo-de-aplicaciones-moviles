package com.esucar.api.web;

import com.esucar.api.model.Consulta;
import com.esucar.api.repo.*;
import com.esucar.api.web.Dtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    private final ConsultaRepo consultas;
    private final UnidadRepo unidades;
    private final MaterialRepo materiales;
    private final UsuarioRepo usuarios;

    public ConsultaController(ConsultaRepo consultas, UnidadRepo unidades,
                              MaterialRepo materiales, UsuarioRepo usuarios) {
        this.consultas = consultas;
        this.unidades = unidades;
        this.materiales = materiales;
        this.usuarios = usuarios;
    }

    /** Sin parámetro: todas (vista docente). Con ?emisor=: solo las de ese estudiante. */
    @GetMapping
    public List<ConsultaRes> listar(@RequestParam(required = false) String emisor) {
        List<Consulta> lista = (emisor == null || emisor.isBlank())
                ? consultas.findAllByOrderByIdAsc()
                : consultas.findAllByEmisorIgnoreCaseOrderByIdAsc(emisor.trim());
        return lista.stream().map(ConsultaRes::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultaRes enviar(@Valid @RequestBody ConsultaReq req) {
        var unidad = unidades.findById(req.unidadId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La unidad no existe"));
        if (!unidad.getAsignaturaId().equals(req.asignaturaId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La unidad no pertenece a la asignatura");
        }
        if (req.materialId() != null && !materiales.existsById(req.materialId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El material no existe");
        }
        var emisor = usuarios.findByNombreNormalizado(req.emisor().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El emisor no existe"));

        Consulta c = consultas.save(new Consulta(req.asignaturaId(), req.unidadId(), req.materialId(),
                emisor.getNombre(), req.asunto().trim(), req.contenido().trim()));
        return ConsultaRes.de(c);
    }

    @PutMapping("/{id}/respuesta")
    public ConsultaRes responder(@PathVariable Integer id, @Valid @RequestBody RespuestaReq req) {
        Consulta c = consultas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La consulta no existe"));
        c.responder(req.respuesta().trim());
        return ConsultaRes.de(consultas.save(c));
    }
}
