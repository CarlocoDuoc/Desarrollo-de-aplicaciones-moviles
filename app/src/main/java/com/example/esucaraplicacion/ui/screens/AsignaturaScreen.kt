package com.example.esucaraplicacion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esucaraplicacion.data.Repositorio
import com.example.esucaraplicacion.data.Sesion
import com.example.esucaraplicacion.ui.components.BarraProgreso
import com.example.esucaraplicacion.ui.components.EsucarTopBar
import com.example.esucaraplicacion.ui.components.EstadoVacio

@Composable
fun AsignaturaScreen(
    sesion: Sesion,
    asignaturaId: Int,
    onVolver: () -> Unit,
    onUnidad: (Int) -> Unit
) {
    val asignatura = Repositorio.asignatura(asignaturaId)

    Scaffold(
        topBar = { EsucarTopBar(asignatura?.codigo ?: "Asignatura", onVolver = onVolver) }
    ) { padding ->
        if (asignatura == null) {
            EstadoVacio("La asignatura solicitada no existe.", Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(asignatura.nombre, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Unidades temáticas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(Repositorio.unidadesDe(asignatura.id), key = { it.id }) { unidad ->
                    val materiales = Repositorio.materialesDe(unidad.id)
                    val progreso = Repositorio.progresoDe(sesion.nombre, materiales)
                    Card(onClick = { onUnidad(unidad.id) }, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(unidad.etiqueta, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            BarraProgreso(progreso)
                        }
                    }
                }
            }
        }
    }
}
