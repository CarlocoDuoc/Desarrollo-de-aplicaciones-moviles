package com.esucar.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String nombre;

    /** nombre en minúsculas: garantiza unicidad sin distinguir mayúsculas */
    @Column(name = "nombre_normalizado", nullable = false, unique = true, length = 40)
    private String nombreNormalizado;

    @Column(name = "clave_hash", nullable = false)
    private String claveHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    protected Usuario() {}

    public Usuario(String nombre, String claveHash, Rol rol) {
        this.nombre = nombre;
        this.nombreNormalizado = nombre.toLowerCase();
        this.claveHash = claveHash;
        this.rol = rol;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getNombreNormalizado() { return nombreNormalizado; }
    public String getClaveHash() { return claveHash; }
    public Rol getRol() { return rol; }
}
