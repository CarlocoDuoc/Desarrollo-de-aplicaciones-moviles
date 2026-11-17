package com.example.esucaraplicacion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.esucaraplicacion.data.Rol
import com.example.esucaraplicacion.data.Repositorio
import com.example.esucaraplicacion.ui.components.EsucarTopBar
import kotlinx.coroutines.launch

private val FORMATO_NOMBRE = Regex("^[A-Za-z0-9._-]{3,20}$")

@Composable
fun RegistroScreen(
    onVolver: () -> Unit,
    onRegistrado: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var nombre by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var repetir by rememberSaveable { mutableStateOf("") }
    var verClave by rememberSaveable { mutableStateOf(false) }
    var rolNombre by rememberSaveable { mutableStateOf(Rol.ESTUDIANTE.name) }
    var intentoEnviar by rememberSaveable { mutableStateOf(false) }
    var nombreEnUso by rememberSaveable { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }

    val rol = Rol.valueOf(rolNombre)

    val errorNombre = when {
        nombreEnUso -> "Ese nombre de usuario ya existe"
        intentoEnviar && !FORMATO_NOMBRE.matches(nombre.trim()) ->
            "3 a 20 caracteres: letras, números, punto, guion o guion bajo"
        else -> null
    }
    val errorClave = if (intentoEnviar && clave.length < 4) "Mínimo 4 caracteres" else null
    val errorRepetir = if (intentoEnviar && repetir != clave) "Las contraseñas no coinciden" else null

    fun registrar() {
        intentoEnviar = true
        nombreEnUso = false
        errorGeneral = null
        val valido = FORMATO_NOMBRE.matches(nombre.trim()) && clave.length >= 4 && repetir == clave
        if (!valido) return

        scope.launch {
            cargando = true
            val resultado = Repositorio.registrar(nombre, clave, rol)
            cargando = false
            val usuario = resultado.getOrNull()
            val error = resultado.exceptionOrNull()
            when {
                usuario != null -> onRegistrado(usuario.nombre)
                Repositorio.esConflicto(error) -> nombreEnUso = true
                else -> errorGeneral = Repositorio.mensajeError(error)
            }
        }
    }

    Scaffold(topBar = { EsucarTopBar("Crear cuenta", onVolver = onVolver) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; nombreEnUso = false },
                label = { Text("Nombre de usuario") },
                singleLine = true,
                isError = errorNombre != null,
                supportingText = { errorNombre?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = clave,
                onValueChange = { clave = it },
                label = { Text("Contraseña") },
                singleLine = true,
                isError = errorClave != null,
                supportingText = { errorClave?.let { Text(it) } },
                visualTransformation = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                trailingIcon = {
                    TextButton(onClick = { verClave = !verClave }) {
                        Text(if (verClave) "Ocultar" else "Ver")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = repetir,
                onValueChange = { repetir = it },
                label = { Text("Repetir contraseña") },
                singleLine = true,
                isError = errorRepetir != null,
                supportingText = { errorRepetir?.let { Text(it) } },
                visualTransformation = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { registrar() }),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Tipo de usuario", style = MaterialTheme.typography.titleSmall)
            Rol.entries.forEach { opcion ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = rol == opcion,
                            onClick = { rolNombre = opcion.name },
                            role = Role.RadioButton
                        )
                ) {
                    RadioButton(selected = rol == opcion, onClick = null)
                    Text(opcion.etiqueta, modifier = Modifier.padding(start = 8.dp))
                }
            }

            errorGeneral?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { registrar() },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (cargando) "Registrando…" else "Registrarme")
            }
        }
    }
}
