package com.esucar.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "unidades")
public class Unidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "asignatura_id", nullable = false)
    private Integer asignaturaId;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false)
    private String nombre;

    protected Unidad() {}

    public Unidad(Integer asignaturaId, Integer numero, String nombre) {
        this.asignaturaId = asignaturaId;
        this.numero = numero;
        this.nombre = nombre;
    }

    public Integer getId() { return id; }
    public Integer getAsignaturaId() { return asignaturaId; }
    public Integer getNumero() { return numero; }
    public String getNombre() { return nombre; }
}
