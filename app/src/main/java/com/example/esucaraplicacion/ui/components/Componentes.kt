package com.example.esucaraplicacion.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.esucaraplicacion.R
import com.example.esucaraplicacion.data.Consulta
import com.example.esucaraplicacion.data.Progreso
import com.example.esucaraplicacion.data.Repositorio
import com.example.esucaraplicacion.notifications.Notificaciones

@Composable
fun LogoEsucar(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val idLogo = remember {
        context.resources.getIdentifier("logo_esucar", "drawable", context.packageName)
    }
    Image(
        painter = painterResource(if (idLogo != 0) idLogo else R.drawable.logo_esucar_placeholder),
        contentDescription = "Logo ESUCAR",
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EsucarTopBar(
    titulo: String,
    onVolver: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color.White, shape = RoundedCornerShape(6.dp)) {
                    LogoEsucar(Modifier.size(36.dp).padding(2.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(titulo, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        navigationIcon = {
            if (onVolver != null) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        actions = acciones,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun <T> Selector(
    etiqueta: String,
    opciones: List<T>,
    seleccionada: T?,
    texto: (T) -> String,
    onSeleccion: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Seleccionar…",
    error: String? = null
) {
    var abierto by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium)
        Box {
            OutlinedButton(
                onClick = { abierto = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (seleccionada != null) texto(seleccionada) else placeholder,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("▾")
            }
            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(texto(opcion)) },
                        onClick = {
                            onSeleccion(opcion)
                            abierto = false
                        }
                    )
                }
            }
        }
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun Etiqueta(texto: String, destacada: Boolean = false, modifier: Modifier = Modifier) {
    val fondo = if (destacada) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val letra = if (destacada) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(shape = RoundedCornerShape(50), color = fondo, contentColor = letra, modifier = modifier) {
        Text(
            texto,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun BarraProgreso(progreso: Progreso, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${progreso.revisados} de ${progreso.total} materiales estudiados",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Text("${progreso.porcentaje}%", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.size(6.dp))
        LinearProgressIndicator(progress = { progreso.fraccion }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun EstadoVacio(texto: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TarjetaConsulta(
    consulta: Consulta,
    mostrarEmisor: Boolean,
    onClick: (() -> Unit)? = null
) {
    val asignatura = Repositorio.asignatura(consulta.asignaturaId)
    val unidad = Repositorio.unidad(consulta.unidadId)
    val material = consulta.materialId?.let { Repositorio.material(it) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    consulta.asunto,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Etiqueta(
                    texto = if (consulta.respondida) "Respondida" else "Pendiente",
                    destacada = consulta.respondida
                )
            }
            Text(
                listOfNotNull(asignatura?.codigo, unidad?.etiqueta, material?.titulo).joinToString(" · "),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (mostrarEmisor) {
                Text("De: ${consulta.emisor}", style = MaterialTheme.typography.labelMedium)
            }
            Text(consulta.contenido, style = MaterialTheme.typography.bodyMedium)
            Text(
                consulta.fecha,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (consulta.respuesta != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Respuesta del docente", style = MaterialTheme.typography.labelMedium)
                        Text(consulta.respuesta, style = MaterialTheme.typography.bodyMedium)
                        consulta.fechaRespuesta?.let {
                            Text(it, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PedirPermisoNotificaciones() {
    val context = LocalContext.current
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        Notificaciones.crearCanal(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            lanzador.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
