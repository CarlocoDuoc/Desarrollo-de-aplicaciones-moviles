package com.esucar.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "materiales")
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "unidad_id", nullable = false)
    private Integer unidadId;

    @Column(nullable = false)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMaterial tipo;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    protected Material() {}

    public Material(Integer unidadId, String titulo, TipoMaterial tipo, String descripcion) {
        this.unidadId = unidadId;
        this.titulo = titulo;
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    public Integer getId() { return id; }
    public Integer getUnidadId() { return unidadId; }
    public String getTitulo() { return titulo; }
    public TipoMaterial getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public Instant getCreadoEn() { return creadoEn; }
}
