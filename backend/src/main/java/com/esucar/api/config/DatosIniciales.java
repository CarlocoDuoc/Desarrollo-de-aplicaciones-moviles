package com.esucar.api.config;

import com.esucar.api.model.*;
import com.esucar.api.repo.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Carga datos de ejemplo la primera vez que la base de Neon está vacía. */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepo usuarios;
    private final AsignaturaRepo asignaturas;
    private final UnidadRepo unidades;
    private final MaterialRepo materiales;
    private final PasswordEncoder encoder;

    public DatosIniciales(UsuarioRepo usuarios, AsignaturaRepo asignaturas, UnidadRepo unidades,
                          MaterialRepo materiales, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.asignaturas = asignaturas;
        this.unidades = unidades;
        this.materiales = materiales;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarios.count() == 0) {
            usuarios.save(new Usuario("alumno", encoder.encode("1234"), Rol.ESTUDIANTE));
            usuarios.save(new Usuario("docente", encoder.encode("1234"), Rol.DOCENTE));
        }
        if (asignaturas.count() > 0) return;

        Asignatura der = asignaturas.save(new Asignatura("DER101", "Derechos Humanos y Función Policial"));
        Asignatura ges = asignaturas.save(new Asignatura("GES102", "Gestión en Seguridad Pública"));
        Asignatura com = asignaturas.save(new Asignatura("COM103", "Comunicación Efectiva"));

        Unidad u1 = unidades.save(new Unidad(der.getId(), 1, "Fundamentos de los Derechos Humanos"));
        Unidad u2 = unidades.save(new Unidad(der.getId(), 2, "Uso de la fuerza"));
        Unidad u3 = unidades.save(new Unidad(ges.getId(), 1, "Introducción a la gestión pública"));
        Unidad u4 = unidades.save(new Unidad(ges.getId(), 2, "Planificación operativa"));
        Unidad u5 = unidades.save(new Unidad(com.getId(), 1, "Comunicación oral"));
        Unidad u6 = unidades.save(new Unidad(com.getId(), 2, "Redacción de informes"));

        materiales.save(new Material(u1.getId(), "Lectura: Historia de los Derechos Humanos", TipoMaterial.LECTURA,
                "Lectura introductoria sobre el origen y la evolución de los derechos humanos."));
        materiales.save(new Material(u1.getId(), "Presentación Unidad 1", TipoMaterial.PRESENTACION,
                "Diapositivas de la clase de fundamentos."));
        materiales.save(new Material(u1.getId(), "Guía de ejercicios 1", TipoMaterial.GUIA,
                "Ejercicios de aplicación de conceptos básicos."));
        materiales.save(new Material(u2.getId(), "Lectura: Principios del uso de la fuerza", TipoMaterial.LECTURA,
                "Legalidad, necesidad y proporcionalidad."));
        materiales.save(new Material(u2.getId(), "Video: Casos prácticos", TipoMaterial.VIDEO,
                "Enlace a un video con casos de análisis."));
        materiales.save(new Material(u3.getId(), "Lectura: Estado y administración pública", TipoMaterial.LECTURA,
                "Conceptos de Estado, gobierno y administración."));
        materiales.save(new Material(u4.getId(), "Pauta de planificación operativa", TipoMaterial.GUIA,
                "Pauta paso a paso para planificar un servicio."));
        materiales.save(new Material(u5.getId(), "Presentación: Técnicas de exposición", TipoMaterial.PRESENTACION,
                "Estructura y recursos para una exposición oral."));
        materiales.save(new Material(u6.getId(), "Guía de redacción de informes", TipoMaterial.GUIA,
                "Formato y ejemplos de informes institucionales."));
    }
}
