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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.esucaraplicacion.data.Repositorio
import com.example.esucaraplicacion.notifications.Notificaciones
import com.example.esucaraplicacion.ui.components.EsucarTopBar
import com.example.esucaraplicacion.ui.components.EstadoVacio
import com.example.esucaraplicacion.ui.components.TarjetaConsulta

@Composable
fun ResponderConsultaScreen(
    consultaId: Int,
    onVolver: () -> Unit,
    onRespondida: () -> Unit
) {
    val context = LocalContext.current
    val consulta = Repositorio.consulta(consultaId)

    var respuesta by rememberSaveable { mutableStateOf("") }
    var intentoEnviar by rememberSaveable { mutableStateOf(false) }
    val errorRespuesta = if (intentoEnviar && respuesta.trim().length < 5) "Mínimo 5 caracteres" else null

    Scaffold(topBar = { EsucarTopBar("Responder consulta", onVolver = onVolver) }) { padding ->
        if (consulta == null) {
            EstadoVacio("La consulta solicitada no existe.", Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TarjetaConsulta(consulta, mostrarEmisor = true)

                if (!consulta.respondida) {
                    OutlinedTextField(
                        value = respuesta,
                        onValueChange = { respuesta = it },
                        label = { Text("Tu respuesta") },
                        minLines = 4,
                        isError = errorRespuesta != null,
                        supportingText = { errorRespuesta?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            intentoEnviar = true
                            if (respuesta.trim().length >= 5) {
                                Repositorio.responder(consulta.id, respuesta)
                                Notificaciones.mostrar(
                                    context = context,
                                    id = consulta.id,
                                    titulo = "El docente respondió tu consulta",
                                    texto = consulta.asunto
                                )
                                Toast.makeText(context, "Respuesta enviada", Toast.LENGTH_SHORT).show()
                                onRespondida()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enviar respuesta")
                    }
                }
            }
        }
    }
}
