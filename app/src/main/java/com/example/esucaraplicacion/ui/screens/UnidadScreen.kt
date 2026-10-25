package com.example.esucaraplicacion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esucaraplicacion.data.Repositorio
import com.example.esucaraplicacion.data.Sesion
import com.example.esucaraplicacion.ui.components.BarraProgreso
import com.example.esucaraplicacion.ui.components.EsucarTopBar
import com.example.esucaraplicacion.ui.components.EstadoVacio
import com.example.esucaraplicacion.ui.components.Etiqueta

@Composable
fun UnidadScreen(
    sesion: Sesion,
    unidadId: Int,
    onVolver: () -> Unit,
    onMaterial: (Int) -> Unit
) {
    val unidad = Repositorio.unidad(unidadId)

    Scaffold(
        topBar = { EsucarTopBar(unidad?.let { "Unidad ${it.numero}" } ?: "Unidad", onVolver = onVolver) }
    ) { padding ->
        if (unidad == null) {
            EstadoVacio("La unidad solicitada no existe.", Modifier.padding(padding))
        } else {
            val materiales = Repositorio.materialesDe(unidad.id)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(unidad.nombre, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        Repositorio.asignatura(unidad.asignaturaId)?.nombre ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item { BarraProgreso(Repositorio.progresoDe(sesion.nombre, materiales)) }
                if (materiales.isEmpty()) {
                    item { EstadoVacio("Esta unidad aún no tiene materiales publicados.") }
                }
                items(materiales, key = { it.id }) { material ->
                    val revisado = Repositorio.estaRevisado(sesion.nombre, material.id)
                    val descargado = Repositorio.estaDescargado(sesion.nombre, material.id)
                    Card(onClick = { onMaterial(material.id) }, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(start = 8.dp, top = 12.dp, end = 16.dp, bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = revisado,
                                onCheckedChange = { Repositorio.alternarRevisado(sesion.nombre, material.id) }
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(material.titulo, style = MaterialTheme.typography.titleSmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Etiqueta(material.tipo.etiqueta)
                                    Etiqueta(if (revisado) "Estudiado" else "Pendiente", destacada = revisado)
                                    if (descargado) Etiqueta("Sin conexión", destacada = true)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
