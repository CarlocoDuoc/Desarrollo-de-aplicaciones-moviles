package com.example.esucaraplicacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.esucaraplicacion.navigation.AppNavHost
import com.example.esucaraplicacion.ui.theme.EsucarAplicacionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EsucarAplicacionTheme {
                AppNavHost()
            }
        }
    }
}
