package com.example.esucaraplicacion.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.esucaraplicacion.data.Repositorio
import com.example.esucaraplicacion.data.Rol
import com.example.esucaraplicacion.data.Sesion
import com.example.esucaraplicacion.data.Usuario
import com.example.esucaraplicacion.ui.screens.AsignaturaScreen
import com.example.esucaraplicacion.ui.screens.HomeDocenteScreen
import com.example.esucaraplicacion.ui.screens.HomeEstudianteScreen
import com.example.esucaraplicacion.ui.screens.LoginScreen
import com.example.esucaraplicacion.ui.screens.MaterialScreen
import com.example.esucaraplicacion.ui.screens.NuevaConsultaScreen
import com.example.esucaraplicacion.ui.screens.PublicarMaterialScreen
import com.example.esucaraplicacion.ui.screens.RegistroScreen
import com.example.esucaraplicacion.ui.screens.ResponderConsultaScreen
import com.example.esucaraplicacion.ui.screens.UnidadScreen

private const val CLAVE_REGISTRADO = "nombre_registrado"

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    var nombreSesion by rememberSaveable { mutableStateOf<String?>(null) }
    var rolSesion by rememberSaveable { mutableStateOf<String?>(null) }
    val sesion: Sesion? = run {
        val nombre = nombreSesion
        val rol = rolSesion
        if (nombre != null && rol != null) Sesion(nombre, Rol.valueOf(rol)) else null
    }

    fun iniciarSesion(usuario: Usuario) {
        nombreSesion = usuario.nombre
        rolSesion = usuario.rol.name

        val destino = when (usuario.rol) {
            Rol.ESTUDIANTE -> Rutas.HOME_ESTUDIANTE
            Rol.DOCENTE -> Rutas.HOME_DOCENTE
        }
        navController.navigate(destino) {
            popUpTo(Rutas.LOGIN) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun cerrarSesion() {
        navController.navigate(Rutas.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
        nombreSesion = null
        rolSesion = null
        Repositorio.limpiar()
    }

    fun redirigirSegunPerfil() {
        val destino = when (sesion?.rol) {
            Rol.ESTUDIANTE -> Rutas.HOME_ESTUDIANTE
            Rol.DOCENTE -> Rutas.HOME_DOCENTE
            null -> Rutas.LOGIN
        }

        if (navController.currentDestination?.route == destino) return
        navController.navigate(destino) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = Rutas.LOGIN) {

        composable(Rutas.LOGIN) { entry ->
            val registrado by entry.savedStateHandle
                .getStateFlow(CLAVE_REGISTRADO, "")
                .collectAsState()
            LoginScreen(
                nombreRegistrado = registrado,
                onLoginExitoso = { iniciarSesion(it) },
                onIrARegistro = { navController.navigate(Rutas.REGISTRO) }
            )
        }
        composable(Rutas.REGISTRO) {
            RegistroScreen(
                onVolver = { navController.popBackStack() },
                onRegistrado = { nombre ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(CLAVE_REGISTRADO, nombre)
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.HOME_ESTUDIANTE) {
            SoloPara(Rol.ESTUDIANTE, sesion, onSinAcceso = { redirigirSegunPerfil() }) { s ->
                HomeEstudianteScreen(
                    sesion = s,
                    onAsignatura = { navController.navigate(Rutas.asignatura(it)) },
                    onNuevaConsulta = { navController.navigate(Rutas.nuevaConsulta()) },
                    onCerrarSesion = { cerrarSesion() }
                )
            }
        }
        composable(
            route = Rutas.ASIGNATURA,
            arguments = listOf(navArgument("asignaturaId") { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt("asignaturaId") ?: -1
            SoloPara(Rol.ESTUDIANTE, sesion, onSinAcceso = { redirigirSegunPerfil() }) { s ->
                AsignaturaScreen(
                    sesion = s,
                    asignaturaId = id,
                    onVolver = { navController.popBackStack() },
                    onUnidad = { navController.navigate(Rutas.unidad(it)) }
                )
            }
        }
        composable(
            route = Rutas.UNIDAD,
            arguments = listOf(navArgument("unidadId") { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt("unidadId") ?: -1
            SoloPara(Rol.ESTUDIANTE, sesion, onSinAcceso = { redirigirSegunPerfil() }) { s ->
                UnidadScreen(
                    sesion = s,
                    unidadId = id,
                    onVolver = { navController.popBackStack() },
                    onMaterial = { navController.navigate(Rutas.material(it)) }
                )
            }
        }
        composable(
            route = Rutas.MATERIAL,
            arguments = listOf(navArgument("materialId") { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt("materialId") ?: -1
            SoloPara(Rol.ESTUDIANTE, sesion, onSinAcceso = { redirigirSegunPerfil() }) { s ->
                MaterialScreen(
                    sesion = s,
                    materialId = id,
                    onVolver = { navController.popBackStack() },
                    onConsultar = { navController.navigate(Rutas.nuevaConsulta(it)) }
                )
            }
        }
        composable(
            route = Rutas.NUEVA_CONSULTA,
            arguments = listOf(
                navArgument("materialId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { entry ->
            val materialId = entry.arguments?.getInt("materialId") ?: -1
            SoloPara(Rol.ESTUDIANTE, sesion, onSinAcceso = { redirigirSegunPerfil() }) { s ->
                NuevaConsultaScreen(
                    sesion = s,
                    materialIdInicial = materialId,
                    onVolver = { navController.popBackStack() },
                    onEnviada = { navController.popBackStack() }
                )
            }
        }

        composable(Rutas.HOME_DOCENTE) {
            SoloPara(Rol.DOCENTE, sesion, onSinAcceso = { redirigirSegunPerfil() }) { s ->
                HomeDocenteScreen(
                    sesion = s,
                    onResponder = { navController.navigate(Rutas.responder(it)) },
                    onPublicar = { navController.navigate(Rutas.PUBLICAR) },
                    onCerrarSesion = { cerrarSesion() }
                )
            }
        }
        composable(
            route = Rutas.RESPONDER,
            arguments = listOf(navArgument("consultaId") { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt("consultaId") ?: -1
            SoloPara(Rol.DOCENTE, sesion, onSinAcceso = { redirigirSegunPerfil() }) {
                ResponderConsultaScreen(
                    consultaId = id,
                    onVolver = { navController.popBackStack() },
                    onRespondida = { navController.popBackStack() }
                )
            }
        }
        composable(Rutas.PUBLICAR) {
            SoloPara(Rol.DOCENTE, sesion, onSinAcceso = { redirigirSegunPerfil() }) {
                PublicarMaterialScreen(
                    onVolver = { navController.popBackStack() },
                    onPublicado = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun SoloPara(
    rol: Rol,
    sesion: Sesion?,
    onSinAcceso: () -> Unit,
    contenido: @Composable (Sesion) -> Unit
) {
    if (sesion != null && sesion.rol == rol) {
        contenido(sesion)
    } else {
        LaunchedEffect(sesion) { onSinAcceso() }
    }
}
