package com.esucar.api.web;

import com.esucar.api.model.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class Dtos {
    private Dtos() {}

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").withZone(ZoneId.of("America/Santiago"));

    public static String fecha(Instant i) {
        return i == null ? null : FORMATO.format(i);
    }

    // ---------- Entrada ----------
    public record LoginReq(@NotBlank String nombre, @NotBlank String clave) {}

    public record RegistroReq(
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9._-]{3,20}$",
                    message = "3 a 20 caracteres: letras, números, punto, guion o guion bajo") String nombre,
            @NotBlank @Size(min = 4, max = 72) String clave,
            @NotNull Rol rol) {}

    public record MaterialReq(
            @NotNull Integer unidadId,
            @NotBlank @Size(min = 5, max = 200) String titulo,
            @NotNull TipoMaterial tipo,
            @Size(max = 2000) String descripcion) {}

    public record ConsultaReq(
            @NotNull Integer asignaturaId,
            @NotNull Integer unidadId,
            Integer materialId,
            @NotBlank String emisor,
            @NotBlank @Size(min = 5, max = 200) String asunto,
            @NotBlank @Size(min = 10, max = 4000) String contenido) {}

    public record RespuestaReq(@NotBlank @Size(min = 5, max = 4000) String respuesta) {}

    // ---------- Salida (los nombres coinciden con los modelos de la app Android) ----------
    public record UsuarioRes(Long id, String nombre, Rol rol) {
        public static UsuarioRes de(Usuario u) { return new UsuarioRes(u.getId(), u.getNombre(), u.getRol()); }
    }

    public record AsignaturaRes(Integer id, String codigo, String nombre) {
        public static AsignaturaRes de(Asignatura a) { return new AsignaturaRes(a.getId(), a.getCodigo(), a.getNombre()); }
    }

    public record UnidadRes(Integer id, Integer asignaturaId, Integer numero, String nombre) {
        public static UnidadRes de(Unidad u) { return new UnidadRes(u.getId(), u.getAsignaturaId(), u.getNumero(), u.getNombre()); }
    }

    public record MaterialRes(Integer id, Integer unidadId, String titulo, TipoMaterial tipo, String descripcion) {
        public static MaterialRes de(Material m) {
            return new MaterialRes(m.getId(), m.getUnidadId(), m.getTitulo(), m.getTipo(), m.getDescripcion());
        }
    }

    public record ConsultaRes(Integer id, Integer asignaturaId, Integer unidadId, Integer materialId,
                              String emisor, String receptor, String asunto, String contenido,
                              String fecha, String respuesta, String fechaRespuesta) {
        public static ConsultaRes de(Consulta c) {
            return new ConsultaRes(c.getId(), c.getAsignaturaId(), c.getUnidadId(), c.getMaterialId(),
                    c.getEmisor(), c.getReceptor(), c.getAsunto(), c.getContenido(),
                    Dtos.fecha(c.getFecha()), c.getRespuesta(), Dtos.fecha(c.getFechaRespuesta()));
        }
    }

    public record ErrorRes(String error) {}
}
