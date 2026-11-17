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
import com.example.esucaraplicacion.data.Repositorio
import com.example.esucaraplicacion.data.TipoMaterial
import com.example.esucaraplicacion.ui.components.EsucarTopBar
import com.example.esucaraplicacion.ui.components.Selector

@Composable
fun PublicarMaterialScreen(
    onVolver: () -> Unit,
    onPublicado: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var enviando by remember { mutableStateOf(false) }

    var asignaturaId by rememberSaveable { mutableIntStateOf(-1) }
    var unidadId by rememberSaveable { mutableIntStateOf(-1) }
    var tipoNombre by rememberSaveable { mutableStateOf("") }
    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var intentoEnviar by rememberSaveable { mutableStateOf(false) }

    val asignatura = Repositorio.asignatura(asignaturaId)
    val unidad = Repositorio.unidad(unidadId)
    val tipo = TipoMaterial.entries.firstOrNull { it.name == tipoNombre }

    val errorAsignatura = if (intentoEnviar && asignatura == null) "Selecciona una asignatura" else null
    val errorUnidad = if (intentoEnviar && unidad == null) "Selecciona una unidad" else null
    val errorTipo = if (intentoEnviar && tipo == null) "Selecciona el tipo de material" else null
    val errorTitulo = if (intentoEnviar && titulo.trim().length < 5) "Mínimo 5 caracteres" else null

    fun publicar() {
        intentoEnviar = true
        if (asignatura == null || unidad == null || tipo == null || titulo.trim().length < 5) return

        scope.launch {
            enviando = true
            val r = Repositorio.publicarMaterial(unidad.id, titulo, tipo, descripcion)
            enviando = false
            if (r.isSuccess) {
                Toast.makeText(context, "Material publicado", Toast.LENGTH_SHORT).show()
                onPublicado()
            } else {
                Toast.makeText(context, Repositorio.mensajeError(r.exceptionOrNull()), Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(topBar = { EsucarTopBar("Publicar material", onVolver = onVolver) }) { padding ->
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
                    }
                },
                error = errorAsignatura
            )
            Selector(
                etiqueta = "Unidad temática",
                opciones = if (asignatura != null) Repositorio.unidadesDe(asignatura.id) else emptyList(),
                seleccionada = unidad,
                texto = { it.etiqueta },
                onSeleccion = { unidadId = it.id },
                placeholder = if (asignatura == null) "Primero elige una asignatura" else "Seleccionar…",
                error = errorUnidad
            )
            Selector(
                etiqueta = "Tipo de material",
                opciones = TipoMaterial.entries.toList(),
                seleccionada = tipo,
                texto = { it.etiqueta },
                onSeleccion = { tipoNombre = it.name },
                error = errorTipo
            )
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del recurso") },
                singleLine = true,
                isError = errorTitulo != null,
                supportingText = { errorTitulo?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción o enlace (opcional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { publicar() }, enabled = !enviando, modifier = Modifier.fillMaxWidth()) {
                Text(if (enviando) "Publicando…" else "Publicar material")
            }
        }
    }
}
