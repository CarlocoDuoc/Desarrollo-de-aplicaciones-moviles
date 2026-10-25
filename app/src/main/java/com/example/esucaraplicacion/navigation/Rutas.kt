package com.example.esucaraplicacion.navigation

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"

    const val HOME_ESTUDIANTE = "estudiante/home"
    const val ASIGNATURA = "estudiante/asignatura/{asignaturaId}"
    const val UNIDAD = "estudiante/unidad/{unidadId}"
    const val MATERIAL = "estudiante/material/{materialId}"
    const val NUEVA_CONSULTA = "estudiante/consulta/nueva?materialId={materialId}"

    const val HOME_DOCENTE = "docente/home"
    const val RESPONDER = "docente/consulta/{consultaId}"
    const val PUBLICAR = "docente/material/nuevo"

    fun asignatura(id: Int) = "estudiante/asignatura/$id"
    fun unidad(id: Int) = "estudiante/unidad/$id"
    fun material(id: Int) = "estudiante/material/$id"
    fun nuevaConsulta(materialId: Int = -1) = "estudiante/consulta/nueva?materialId=$materialId"
    fun responder(id: Int) = "docente/consulta/$id"
}
