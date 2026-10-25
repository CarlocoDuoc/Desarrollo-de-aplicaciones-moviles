package com.example.esucaraplicacion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esucaraplicacion.data.Repositorio
import com.example.esucaraplicacion.data.Sesion
import com.example.esucaraplicacion.ui.components.EsucarTopBar
import com.example.esucaraplicacion.ui.components.EstadoVacio
import com.example.esucaraplicacion.ui.components.Etiqueta
import kotlinx.coroutines.launch

@Composable
fun MaterialScreen(
    sesion: Sesion,
    materialId: Int,
    onVolver: () -> Unit,
    onConsultar: (Int) -> Unit
) {
    val material = Repositorio.material(materialId)
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { EsucarTopBar("Material de estudio", onVolver = onVolver) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (material == null) {
            EstadoVacio("El material solicitado no existe.", Modifier.padding(padding))
        } else {
            val unidad = Repositorio.unidad(material.unidadId)
            val asignatura = unidad?.let { Repositorio.asignatura(it.asignaturaId) }
            val revisado = Repositorio.estaRevisado(sesion.nombre, material.id)
            val descargado = Repositorio.estaDescargado(sesion.nombre, material.id)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(material.titulo, style = MaterialTheme.typography.headlineSmall)
                Text(
                    listOfNotNull(asignatura?.codigo, unidad?.etiqueta).joinToString(" · "),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Etiqueta(material.tipo.etiqueta)
                    Etiqueta(if (revisado) "Estudiado" else "Pendiente", destacada = revisado)
                    if (descargado) Etiqueta("Disponible sin conexión", destacada = true)
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        material.descripcion,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Button(
                    onClick = {
                        Repositorio.alternarRevisado(sesion.nombre, material.id)
                        scope.launch {
                            snackbar.showSnackbar(
                                if (revisado) "Marca de estudiado quitada" else "Material marcado como estudiado"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (revisado) "Quitar marca de estudiado" else "Marcar como estudiado")
                }

                OutlinedButton(
                    onClick = {
                        Repositorio.alternarDescargado(sesion.nombre, material.id)
                        scope.launch {
                            snackbar.showSnackbar(
                                if (descargado) "Descarga eliminada" else "Guardado para ver sin conexión"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (descargado) "Quitar descarga" else "Descargar para ver sin conexión")
                }

                OutlinedButton(
                    onClick = { onConsultar(material.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Consultar al docente sobre este material")
                }
            }
        }
    }
}
