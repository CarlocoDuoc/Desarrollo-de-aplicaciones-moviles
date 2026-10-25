package com.example.esucaraplicacion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VerdeModoOscuro,
    onPrimary = VerdeEsucarOscuro,
    primaryContainer = VerdeEsucar,
    onPrimaryContainer = VerdeEsucarClaro,
    secondary = DoradoModoOscuro,
    onSecondary = Color(0xFF3B2F00),
    secondaryContainer = Color(0xFF574500),
    onSecondaryContainer = DoradoEsucarClaro
)

private val LightColorScheme = lightColorScheme(
    primary = VerdeEsucar,
    onPrimary = Color.White,
    primaryContainer = VerdeEsucarClaro,
    onPrimaryContainer = VerdeEsucarOscuro,
    secondary = DoradoEsucar,
    onSecondary = Color.White,
    secondaryContainer = DoradoEsucarClaro,
    onSecondaryContainer = Color(0xFF261A00)
)

@Composable
fun EsucarAplicacionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
