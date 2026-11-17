package com.example.esucaraplicacion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.esucaraplicacion.data.Repositorio
import com.example.esucaraplicacion.data.Sesion
import com.example.esucaraplicacion.ui.components.BarraProgreso
import com.example.esucaraplicacion.ui.components.EsucarTopBar
import com.example.esucaraplicacion.ui.components.EstadoVacio
import com.example.esucaraplicacion.ui.components.PedirPermisoNotificaciones
import com.example.esucaraplicacion.ui.components.TarjetaConsulta

private data class Pestana(val titulo: String, val icono: ImageVector)

private val PESTANAS_ESTUDIANTE = listOf(
    Pestana("Asignaturas", Icons.Filled.Home),
    Pestana("Progreso", Icons.Filled.CheckCircle),
    Pestana("Consultas", Icons.Filled.Email)
)

private val ESPACIO_PARA_BOTON = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp)

@Composable
fun HomeEstudianteScreen(
    sesion: Sesion,
    onAsignatura: (Int) -> Unit,
    onNuevaConsulta: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    PedirPermisoNotificaciones()
    LaunchedEffect(pestana) { Repositorio.actualizar() }

    Scaffold(
        topBar = {
            EsucarTopBar(
                titulo = "Hola, ${sesion.nombre}",
                acciones = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                PESTANAS_ESTUDIANTE.forEachIndexed { indice, item ->
                    NavigationBarItem(
                        selected = pestana == indice,
                        onClick = { pestana = indice },
                        icon = { Icon(item.icono, contentDescription = null) },
                        label = { Text(item.titulo) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (pestana == 2) {
                ExtendedFloatingActionButton(
                    onClick = onNuevaConsulta,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nueva consulta") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (Repositorio.sinConexion) {
                Text(
                    "Sin conexión: solo se muestran los materiales descargados.",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                when (pestana) {
                    0 -> TabAsignaturas(sesion, onAsignatura)
                    1 -> TabProgreso(sesion)
                    else -> TabConsultas(sesion)
                }
            }
        }
    }
}

@Composable
private fun TabAsignaturas(sesion: Sesion, onAsignatura: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Mis asignaturas", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Elige una asignatura para ver sus unidades temáticas y materiales.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(Repositorio.asignaturas, key = { it.id }) { asignatura ->
            val progreso = Repositorio.progresoDe(
                sesion.nombre,
                Repositorio.materialesDeAsignatura(asignatura.id)
            )
            Card(onClick = { onAsignatura(asignatura.id) }, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        asignatura.codigo,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(asignatura.nombre, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    BarraProgreso(progreso)
                }
            }
        }
    }
}

@Composable
private fun TabProgreso(sesion: Sesion) {
    val total = Repositorio.progresoDe(sesion.nombre, Repositorio.materiales.toList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Tu avance general", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${total.porcentaje}%",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    BarraProgreso(total)
                }
            }
        }
        items(Repositorio.asignaturas, key = { it.id }) { asignatura ->
            val progreso = Repositorio.progresoDe(
                sesion.nombre,
                Repositorio.materialesDeAsignatura(asignatura.id)
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(asignatura.nombre, style = MaterialTheme.typography.titleSmall)
                    BarraProgreso(progreso)
                    Repositorio.unidadesDe(asignatura.id).forEach { unidad ->
                        val p = Repositorio.progresoDe(sesion.nombre, Repositorio.materialesDe(unidad.id))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                unidad.etiqueta,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text("${p.revisados}/${p.total}", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabConsultas(sesion: Sesion) {
    val mias = Repositorio.consultas
        .filter { it.emisor.equals(sesion.nombre, ignoreCase = true) }
        .sortedByDescending { it.id }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ESPACIO_PARA_BOTON,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Mis consultas al docente", style = MaterialTheme.typography.headlineSmall)
        }
        if (mias.isEmpty()) {
            item {
                EstadoVacio("Aún no has enviado consultas. Usa «Nueva consulta» para escribirle a tu docente.")
            }
        }
        items(mias, key = { it.id }) { consulta ->
            TarjetaConsulta(consulta, mostrarEmisor = false)
        }
    }
}
