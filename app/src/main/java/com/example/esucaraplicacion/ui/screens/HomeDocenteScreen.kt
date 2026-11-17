package com.example.esucaraplicacion.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.esucaraplicacion.data.Consulta
import com.example.esucaraplicacion.data.Repositorio
import com.example.esucaraplicacion.data.Sesion
import com.example.esucaraplicacion.ui.components.EsucarTopBar
import com.example.esucaraplicacion.ui.components.EstadoVacio
import com.example.esucaraplicacion.ui.components.Etiqueta
import com.example.esucaraplicacion.ui.components.PedirPermisoNotificaciones
import com.example.esucaraplicacion.ui.components.TarjetaConsulta

private data class PestanaDocente(val titulo: String, val icono: ImageVector)

private val PESTANAS_DOCENTE = listOf(
    PestanaDocente("Consultas", Icons.Filled.Email),
    PestanaDocente("Materiales", Icons.Filled.Info)
)

private val ESPACIO_PARA_BOTON_DOCENTE =
    PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp)

@Composable
fun HomeDocenteScreen(
    sesion: Sesion,
    onResponder: (Int) -> Unit,
    onPublicar: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    PedirPermisoNotificaciones()
    LaunchedEffect(pestana) { Repositorio.actualizar() }

    Scaffold(
        topBar = {
            EsucarTopBar(
                titulo = "Docente: ${sesion.nombre}",
                acciones = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                PESTANAS_DOCENTE.forEachIndexed { indice, item ->
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
            if (pestana == 1) {
                ExtendedFloatingActionButton(
                    onClick = onPublicar,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Publicar material") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (pestana) {
                0 -> TabConsultasDocente(onResponder)
                else -> TabMaterialesDocente()
            }
        }
    }
}

@Composable
private fun TabConsultasDocente(onResponder: (Int) -> Unit) {
    var filtroAsignatura by rememberSaveable { mutableIntStateOf(0) }
    var soloPendientes by rememberSaveable { mutableStateOf(false) }

    val consultas = Repositorio.consultas.filter {
        (filtroAsignatura == 0 || it.asignaturaId == filtroAsignatura) &&
            (!soloPendientes || !it.respondida)
    }
    val porAsignatura = consultas.groupBy { it.asignaturaId }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Consultas de los estudiantes", style = MaterialTheme.typography.headlineSmall)
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtroAsignatura == 0,
                    onClick = { filtroAsignatura = 0 },
                    label = { Text("Todas") }
                )
                Repositorio.asignaturas.forEach { asignatura ->
                    FilterChip(
                        selected = filtroAsignatura == asignatura.id,
                        onClick = { filtroAsignatura = asignatura.id },
                        label = { Text(asignatura.codigo) }
                    )
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = soloPendientes, onCheckedChange = { soloPendientes = it })
                Text("Solo pendientes", modifier = Modifier.padding(start = 8.dp))
            }
        }
        if (consultas.isEmpty()) {
            item { EstadoVacio("No hay consultas con este filtro.") }
        }
        porAsignatura.forEach { (asignaturaId, lista) ->
            val asignatura = Repositorio.asignatura(asignaturaId)
            val pendientes = lista.count { !it.respondida }
            item(key = "cabecera-$asignaturaId") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${asignatura?.codigo ?: ""} · ${asignatura?.nombre ?: "Asignatura"}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Etiqueta("$pendientes pendientes", destacada = pendientes > 0)
                }
            }
            items(
                lista.sortedWith(compareBy<Consulta>({ it.respondida }, { -it.id })),
                key = { "consulta-${it.id}" }
            ) { consulta ->
                TarjetaConsulta(
                    consulta = consulta,
                    mostrarEmisor = true,
                    onClick = { onResponder(consulta.id) }
                )
            }
        }
    }
}

@Composable
private fun TabMaterialesDocente() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ESPACIO_PARA_BOTON_DOCENTE,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Material publicado", style = MaterialTheme.typography.headlineSmall)
        }
        Repositorio.asignaturas.forEach { asignatura ->
            item(key = "asig-${asignatura.id}") {
                Text(
                    "${asignatura.codigo} · ${asignatura.nombre}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Repositorio.unidadesDe(asignatura.id).forEach { unidad ->
                val materiales = Repositorio.materialesDe(unidad.id)
                item(key = "uni-${unidad.id}") {
                    Text(unidad.etiqueta, style = MaterialTheme.typography.titleSmall)
                }
                if (materiales.isEmpty()) {
                    item(key = "vacio-${unidad.id}") {
                        Text(
                            "Sin materiales",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(materiales, key = { "mat-${it.id}" }) { material ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                material.titulo,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Etiqueta(material.tipo.etiqueta)
                        }
                    }
                }
            }
        }
    }
}
