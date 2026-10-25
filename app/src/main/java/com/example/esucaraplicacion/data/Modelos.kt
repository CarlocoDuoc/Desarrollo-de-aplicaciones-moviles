package com.example.esucaraplicacion.data

import kotlin.math.roundToInt

enum class Rol(val etiqueta: String) {
    ESTUDIANTE("Estudiante"),
    DOCENTE("Docente")
}

enum class TipoMaterial(val etiqueta: String) {
    GUIA("Guía de ejercicios"),
    LECTURA("Lectura"),
    PRESENTACION("Presentación"),
    VIDEO("Video (enlace)")
}

data class Usuario(val id: Long, val nombre: String, val rol: Rol)

data class Sesion(val nombre: String, val rol: Rol)

data class Asignatura(val id: Int, val codigo: String, val nombre: String)

data class UnidadTematica(
    val id: Int,
    val asignaturaId: Int,
    val numero: Int,
    val nombre: String
) {
    val etiqueta: String get() = "U$numero · $nombre"
}

data class Material(
    val id: Int,
    val unidadId: Int,
    val titulo: String,
    val tipo: TipoMaterial,
    val descripcion: String
)

data class Consulta(
    val id: Int,
    val asignaturaId: Int,
    val unidadId: Int,
    val materialId: Int?,
    val emisor: String,
    val receptor: String,
    val asunto: String,
    val contenido: String,
    val fecha: String,
    val respuesta: String? = null,
    val fechaRespuesta: String? = null
) {
    val respondida: Boolean get() = respuesta != null
}

data class Progreso(val revisados: Int, val total: Int) {
    val fraccion: Float get() = if (total == 0) 0f else revisados.toFloat() / total
    val porcentaje: Int get() = (fraccion * 100).roundToInt()
}
