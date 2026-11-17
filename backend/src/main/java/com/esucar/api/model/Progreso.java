package com.esucar.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "progreso",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario", "material_id"}))
public class Progreso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** nombre de usuario en minúsculas */
    @Column(nullable = false, length = 40)
    private String usuario;

    @Column(name = "material_id", nullable = false)
    private Integer materialId;

    protected Progreso() {}

    public Progreso(String usuario, Integer materialId) {
        this.usuario = usuario;
        this.materialId = materialId;
    }

    public Long getId() { return id; }
    public String getUsuario() { return usuario; }
    public Integer getMaterialId() { return materialId; }
}
