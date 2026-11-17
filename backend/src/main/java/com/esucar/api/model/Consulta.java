package com.esucar.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "consultas")
public class Consulta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "asignatura_id", nullable = false)
    private Integer asignaturaId;

    @Column(name = "unidad_id", nullable = false)
    private Integer unidadId;

    @Column(name = "material_id")
    private Integer materialId;

    @Column(nullable = false, length = 40)
    private String emisor;

    @Column(nullable = false, length = 40)
    private String receptor = "Docente";

    @Column(nullable = false)
    private String asunto;

    @Column(nullable = false, length = 4000)
    private String contenido;

    @Column(nullable = false)
    private Instant fecha = Instant.now();

    @Column(length = 4000)
    private String respuesta;

    @Column(name = "fecha_respuesta")
    private Instant fechaRespuesta;

    protected Consulta() {}

    public Consulta(Integer asignaturaId, Integer unidadId, Integer materialId,
                    String emisor, String asunto, String contenido) {
        this.asignaturaId = asignaturaId;
        this.unidadId = unidadId;
        this.materialId = materialId;
        this.emisor = emisor;
        this.asunto = asunto;
        this.contenido = contenido;
    }

    public void responder(String texto) {
        this.respuesta = texto;
        this.fechaRespuesta = Instant.now();
    }

    public Integer getId() { return id; }
    public Integer getAsignaturaId() { return asignaturaId; }
    public Integer getUnidadId() { return unidadId; }
    public Integer getMaterialId() { return materialId; }
    public String getEmisor() { return emisor; }
    public String getReceptor() { return receptor; }
    public String getAsunto() { return asunto; }
    public String getContenido() { return contenido; }
    public Instant getFecha() { return fecha; }
    public String getRespuesta() { return respuesta; }
    public Instant getFechaRespuesta() { return fechaRespuesta; }
}
