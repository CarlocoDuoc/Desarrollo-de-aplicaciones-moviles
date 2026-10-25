package com.example.esucaraplicacion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.esucaraplicacion.data.Usuario
import com.example.esucaraplicacion.data.UsuarioDbHelper
import com.example.esucaraplicacion.ui.components.LogoEsucar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    nombreRegistrado: String,
    onLoginExitoso: (Usuario) -> Unit,
    onIrARegistro: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { UsuarioDbHelper.obtener(context) }
    val scope = rememberCoroutineScope()

    var nombre by rememberSaveable(nombreRegistrado) { mutableStateOf(nombreRegistrado) }
    var clave by rememberSaveable { mutableStateOf("") }
    var verClave by rememberSaveable { mutableStateOf(false) }
    var errorNombre by rememberSaveable { mutableStateOf<String?>(null) }
    var errorClave by rememberSaveable { mutableStateOf<String?>(null) }
    var errorGeneral by rememberSaveable { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    fun ingresar() {
        errorNombre = if (nombre.isBlank()) "Ingresa tu nombre de usuario" else null
        errorClave = if (clave.isEmpty()) "Ingresa tu contraseña" else null
        errorGeneral = null
        if (errorNombre != null || errorClave != null) return

        scope.launch {
            cargando = true
            val usuario = withContext(Dispatchers.IO) { db.autenticar(nombre, clave) }
            cargando = false
            if (usuario == null) {
                errorGeneral = "Nombre de usuario o contraseña incorrectos"
            } else {
                onLoginExitoso(usuario)
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LogoEsucar(Modifier.size(112.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                "Plataforma ESUCAR",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Escuela de Suboficiales de Carabineros",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            if (nombreRegistrado.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Cuenta creada. Ingresa con tu contraseña para continuar.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; errorNombre = null; errorGeneral = null },
                label = { Text("Nombre de usuario") },
                singleLine = true,
                isError = errorNombre != null,
                supportingText = { errorNombre?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = clave,
                onValueChange = { clave = it; errorClave = null; errorGeneral = null },
                label = { Text("Contraseña") },
                singleLine = true,
                isError = errorClave != null,
                supportingText = { errorClave?.let { Text(it) } },
                visualTransformation = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { ingresar() }),
                trailingIcon = {
                    TextButton(onClick = { verClave = !verClave }) {
                        Text(if (verClave) "Ocultar" else "Ver")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            errorGeneral?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { ingresar() },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (cargando) "Verificando…" else "Ingresar")
            }
            TextButton(onClick = onIrARegistro) {
                Text("¿No tienes cuenta? Regístrate")
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Cuentas de prueba:\nalumno / 1234   ·   docente / 1234",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
