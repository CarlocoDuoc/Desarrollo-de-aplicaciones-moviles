package com.example.esucaraplicacion.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.esucaraplicacion.data.Material
import com.example.esucaraplicacion.data.Repositorio
import com.example.esucaraplicacion.data.Sesion
import com.example.esucaraplicacion.ui.components.EsucarTopBar
import com.example.esucaraplicacion.ui.components.Selector

@Composable
fun NuevaConsultaScreen(
    sesion: Sesion,
    materialIdInicial: Int,
    onVolver: () -> Unit,
    onEnviada: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var enviando by remember { mutableStateOf(false) }

    val materialInicial = Repositorio.material(materialIdInicial)
    val unidadInicial = materialInicial?.let { Repositorio.unidad(it.unidadId) }

    var asignaturaId by rememberSaveable { mutableIntStateOf(unidadInicial?.asignaturaId ?: -1) }
    var unidadId by rememberSaveable { mutableIntStateOf(unidadInicial?.id ?: -1) }
    var materialId by rememberSaveable { mutableIntStateOf(materialInicial?.id ?: -1) }
    var asunto by rememberSaveable { mutableStateOf("") }
    var contenido by rememberSaveable { mutableStateOf("") }
    var intentoEnviar by rememberSaveable { mutableStateOf(false) }

    val asignatura = Repositorio.asignatura(asignaturaId)
    val unidad = Repositorio.unidad(unidadId)
    val material = Repositorio.material(materialId)

    val errorAsignatura = if (intentoEnviar && asignatura == null) "Selecciona una asignatura" else null
    val errorUnidad = if (intentoEnviar && unidad == null) "Selecciona una unidad" else null
    val errorAsunto = if (intentoEnviar && asunto.trim().length < 5) "Mínimo 5 caracteres" else null
    val errorContenido = if (intentoEnviar && contenido.trim().length < 10) "Mínimo 10 caracteres" else null

    fun enviar() {
        intentoEnviar = true
        if (asignatura == null || unidad == null ||
            asunto.trim().length < 5 || contenido.trim().length < 10
        ) return

        scope.launch {
            enviando = true
            val r = Repositorio.enviarConsulta(
                emisor = sesion.nombre,
                asignaturaId = asignatura.id,
                unidadId = unidad.id,
                materialId = material?.id,
                asunto = asunto,
                contenido = contenido
            )
            enviando = false
            if (r.isSuccess) {
                Toast.makeText(context, "Consulta enviada al docente", Toast.LENGTH_SHORT).show()
                onEnviada()
            } else {
                Toast.makeText(context, Repositorio.mensajeError(r.exceptionOrNull()), Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(topBar = { EsucarTopBar("Nueva consulta", onVolver = onVolver) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Selector(
                etiqueta = "Asignatura",
                opciones = Repositorio.asignaturas,
                seleccionada = asignatura,
                texto = { "${it.codigo} · ${it.nombre}" },
                onSeleccion = {
                    if (it.id != asignaturaId) {
                        asignaturaId = it.id
                        unidadId = -1
                        materialId = -1
                    }
                },
                error = errorAsignatura
            )
            Selector(
                etiqueta = "Unidad temática",
                opciones = if (asignatura != null) Repositorio.unidadesDe(asignatura.id) else emptyList(),
                seleccionada = unidad,
                texto = { it.etiqueta },
                onSeleccion = {
                    if (it.id != unidadId) {
                        unidadId = it.id
                        materialId = -1
                    }
                },
                placeholder = if (asignatura == null) "Primero elige una asignatura" else "Seleccionar…",
                error = errorUnidad
            )
            Selector<Material?>(
                etiqueta = "Material relacionado (opcional)",
                opciones = listOf<Material?>(null) + (if (unidad != null) Repositorio.materialesDe(unidad.id) else emptyList()),
                seleccionada = material,
                texto = { it?.titulo ?: "Sin material específico" },
                onSeleccion = { materialId = it?.id ?: -1 },
                placeholder = "Sin material específico"
            )
            OutlinedTextField(
                value = asunto,
                onValueChange = { asunto = it },
                label = { Text("Asunto") },
                singleLine = true,
                isError = errorAsunto != null,
                supportingText = { errorAsunto?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = contenido,
                onValueChange = { contenido = it },
                label = { Text("Escribe tu consulta") },
                minLines = 4,
                isError = errorContenido != null,
                supportingText = { errorContenido?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { enviar() }, enabled = !enviando, modifier = Modifier.fillMaxWidth()) {
                Text(if (enviando) "Enviando…" else "Enviar consulta")
            }
        }
    }
}
