package com.example.esucaraplicacion.data

import androidx.compose.runtime.mutableStateListOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Repositorio {

    val asignaturas: List<Asignatura> = listOf(
        Asignatura(1, "DER101", "Derechos Humanos y Función Policial"),
        Asignatura(2, "GES102", "Gestión en Seguridad Pública"),
        Asignatura(3, "COM103", "Comunicación Efectiva")
    )

    val unidades: List<UnidadTematica> = listOf(
        UnidadTematica(1, 1, 1, "Fundamentos de los Derechos Humanos"),
        UnidadTematica(2, 1, 2, "Uso de la fuerza"),
        UnidadTematica(3, 2, 1, "Introducción a la gestión pública"),
        UnidadTematica(4, 2, 2, "Planificación operativa"),
        UnidadTematica(5, 3, 1, "Comunicación oral"),
        UnidadTematica(6, 3, 2, "Redacción de informes")
    )

    val materiales = mutableStateListOf(
        Material(1, 1, "Lectura: Historia de los Derechos Humanos", TipoMaterial.LECTURA, "Lectura introductoria sobre el origen y la evolución de los derechos humanos."),
        Material(2, 1, "Presentación Unidad 1", TipoMaterial.PRESENTACION, "Diapositivas de la clase de fundamentos."),
        Material(3, 1, "Guía de ejercicios 1", TipoMaterial.GUIA, "Ejercicios de aplicación de conceptos básicos."),
        Material(4, 2, "Lectura: Principios del uso de la fuerza", TipoMaterial.LECTURA, "Legalidad, necesidad y proporcionalidad."),
        Material(5, 2, "Video: Casos prácticos", TipoMaterial.VIDEO, "Enlace a un video con casos de análisis."),
        Material(6, 3, "Lectura: Estado y administración pública", TipoMaterial.LECTURA, "Conceptos de Estado, gobierno y administración."),
        Material(7, 4, "Pauta de planificación operativa", TipoMaterial.GUIA, "Pauta paso a paso para planificar un servicio."),
        Material(8, 5, "Presentación: Técnicas de exposición", TipoMaterial.PRESENTACION, "Estructura y recursos para una exposición oral."),
        Material(9, 6, "Guía de redacción de informes", TipoMaterial.GUIA, "Formato y ejemplos de informes institucionales.")
    )

    private val revisados = mutableStateListOf<String>()
    private val descargados = mutableStateListOf<String>()

    val consultas = mutableStateListOf<Consulta>()

    private var proximoIdConsulta = 1
    private var proximoIdMaterial = 100

    fun asignatura(id: Int): Asignatura? = asignaturas.firstOrNull { it.id == id }

    fun unidad(id: Int): UnidadTematica? = unidades.firstOrNull { it.id == id }

    fun material(id: Int): Material? = materiales.firstOrNull { it.id == id }

    fun consulta(id: Int): Consulta? = consultas.firstOrNull { it.id == id }

    fun unidadesDe(asignaturaId: Int): List<UnidadTematica> =
        unidades.filter { it.asignaturaId == asignaturaId }

    fun materialesDe(unidadId: Int): List<Material> =
        materiales.filter { it.unidadId == unidadId }

    fun materialesDeAsignatura(asignaturaId: Int): List<Material> {
        val idsUnidades = unidadesDe(asignaturaId).map { it.id }
        return materiales.filter { it.unidadId in idsUnidades }
    }

    private fun clave(usuario: String, materialId: Int) = "${usuario.lowercase()}#$materialId"

    fun estaRevisado(usuario: String, materialId: Int): Boolean =
        clave(usuario, materialId) in revisados

    fun alternarRevisado(usuario: String, materialId: Int) {
        val k = clave(usuario, materialId)
        if (!revisados.remove(k)) revisados.add(k)
    }

    fun estaDescargado(usuario: String, materialId: Int): Boolean =
        clave(usuario, materialId) in descargados

    fun alternarDescargado(usuario: String, materialId: Int) {
        val k = clave(usuario, materialId)
        if (!descargados.remove(k)) descargados.add(k)
    }

    fun progresoDe(usuario: String, lista: List<Material>): Progreso =
        Progreso(lista.count { estaRevisado(usuario, it.id) }, lista.size)

    fun enviarConsulta(
        emisor: String,
        asignaturaId: Int,
        unidadId: Int,
        materialId: Int?,
        asunto: String,
        contenido: String
    ) {
        consultas.add(
            Consulta(
                id = proximoIdConsulta++,
                asignaturaId = asignaturaId,
                unidadId = unidadId,
                materialId = materialId,
                emisor = emisor,
                receptor = "Docente",
                asunto = asunto.trim(),
                contenido = contenido.trim(),
                fecha = ahora()
            )
        )
    }

    fun responder(consultaId: Int, respuesta: String) {
        val i = consultas.indexOfFirst { it.id == consultaId }
        if (i >= 0) {
            consultas[i] = consultas[i].copy(respuesta = respuesta.trim(), fechaRespuesta = ahora())
        }
    }

    fun publicarMaterial(
        unidadId: Int,
        titulo: String,
        tipo: TipoMaterial,
        descripcion: String
    ): Material {
        val nuevo = Material(
            id = proximoIdMaterial++,
            unidadId = unidadId,
            titulo = titulo.trim(),
            tipo = tipo,
            descripcion = descripcion.trim().ifEmpty { "Sin descripción." }
        )
        materiales.add(nuevo)
        return nuevo
    }

    private fun ahora(): String =
        SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.forLanguageTag("es-CL")).format(Date())
}
