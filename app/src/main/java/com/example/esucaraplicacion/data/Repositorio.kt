package com.example.esucaraplicacion.data

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.esucaraplicacion.notifications.Notificaciones
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.security.MessageDigest

/** Copia completa de lo que la app muestra; también es lo que se guarda para uso sin conexión. */
private data class Datos(
    val asignaturas: List<Asignatura> = emptyList(),
    val unidades: List<UnidadTematica> = emptyList(),
    val materiales: List<Material> = emptyList(),
    val consultas: List<Consulta> = emptyList(),
    val revisados: List<Int> = emptyList()
)

/** Lo que se guarda en un archivo del teléfono al descargar un material. */
private data class DescargaLocal(
    val material: Material,
    val unidad: UnidadTematica?,
    val asignatura: Asignatura?
)

/** Credencial local para poder entrar sin internet (solo el hash, nunca la clave). */
private data class CredencialLocal(val id: Long, val nombre: String, val rol: Rol, val hash: String)

/**
 * Fuente única de datos de la app.
 *
 * Los datos viven en el backend (Spring Boot + Neon). Aquí se mantiene una copia observable por
 * Compose, que se sincroniza con el servidor y se guarda localmente para poder abrir la app sin
 * conexión. Las pantallas siguen leyendo de este objeto igual que antes.
 */
object Repositorio {

    val asignaturas = mutableStateListOf<Asignatura>()
    val unidades = mutableStateListOf<UnidadTematica>()
    val materiales = mutableStateListOf<Material>()
    val consultas = mutableStateListOf<Consulta>()

    private val revisados = mutableStateListOf<String>()
    private val descargados = mutableStateListOf<String>()

    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var contexto: Context? = null
    /** true cuando la app está trabajando solo con lo guardado en el teléfono. */
    var sinConexion by mutableStateOf(false)
        private set

    private var usuarioActual: String? = null
    private var rolActual: Rol? = null

    // ---------- Ciclo de vida ----------

    fun iniciar(context: Context) {
        if (contexto != null) return
        contexto = context.applicationContext
    }

    /** Llamar justo después de un login correcto: trae todo del servidor (o del caché si no hay red). */
    suspend fun cargar(usuario: String, rol: Rol): Boolean {
        usuarioActual = usuario
        rolActual = rol
        refrescarDescargados(usuario)
        val remoto = llamar { traer(usuario, rol) }
        return if (remoto.isSuccess) {
            sinConexion = false
            aplicar(remoto.getOrThrow(), usuario)
            guardarCache()
            true
        } else {
            sinConexion = true
            cargarSinConexion(usuario, rol)
            false
        }
    }

    /** Refresca desde el servidor; avisa si hay respuestas o materiales nuevos (solo estudiantes). */
    suspend fun actualizar() {
        val usuario = usuarioActual ?: return
        val rol = rolActual ?: return
        val resultado = llamar { traer(usuario, rol) }
        resultado.onSuccess { datos ->
            if (rol == Rol.ESTUDIANTE) avisarNovedades(datos)
            sinConexion = false
            aplicar(datos, usuario)
            guardarCache()
        }
        if (resultado.isFailure && !sinConexion) {
            sinConexion = true
            cargarSinConexion(usuario, rol)
        }
    }

    fun limpiar() {
        usuarioActual = null
        rolActual = null
        sinConexion = false
        descargados.clear()
        asignaturas.clear()
        unidades.clear()
        materiales.clear()
        consultas.clear()
        revisados.clear()
    }

    // ---------- Autenticación ----------

    /**
     * Inicia sesión en el servidor. Si no hay conexión y ese usuario ya había entrado antes en este
     * teléfono, valida contra la credencial guardada localmente (modo sin conexión).
     */
    suspend fun login(nombre: String, clave: String): Result<Usuario> {
        val r = llamar { Api.servicio.login(LoginReq(nombre.trim(), clave)) }
        r.onSuccess { guardarCredencial(it, clave) }
        if (r.exceptionOrNull() is IOException) {
            loginLocal(nombre, clave)?.let { return Result.success(it) }
        }
        return r
    }

    suspend fun registrar(nombre: String, clave: String, rol: Rol): Result<Usuario> =
        llamar { Api.servicio.registro(RegistroReq(nombre.trim(), clave, rol)) }

    // ---------- Consultas ----------

    fun asignatura(id: Int): Asignatura? = asignaturas.firstOrNull { it.id == id }

    fun unidad(id: Int): UnidadTematica? = unidades.firstOrNull { it.id == id }

    fun material(id: Int): Material? = materiales.firstOrNull { it.id == id }

    fun consulta(id: Int): Consulta? = consultas.firstOrNull { it.id == id }

    fun unidadesDe(asignaturaId: Int): List<UnidadTematica> =
        unidades.filter { it.asignaturaId == asignaturaId }

    fun materialesDe(unidadId: Int): List<Material> =
        materiales.filter { it.unidadId == unidadId }

    fun materialesDeAsignatura(asignaturaId: Int): List<Material> {
        val idsUnidades = unidadesDe(asignaturaId).map { it.id }
        return materiales.filter { it.unidadId in idsUnidades }
    }

    // ---------- Progreso de estudio (se guarda en el servidor) ----------

    private fun clave(usuario: String, materialId: Int) = "${usuario.lowercase()}#$materialId"

    fun estaRevisado(usuario: String, materialId: Int): Boolean =
        clave(usuario, materialId) in revisados

    fun alternarRevisado(usuario: String, materialId: Int) {
        val k = clave(usuario, materialId)
        val nuevoEstado = k !in revisados
        if (nuevoEstado) revisados.add(k) else revisados.remove(k)

        scope.launch {
            val resultado = llamar { Api.servicio.marcarProgreso(materialId, usuario, nuevoEstado) }
            if (resultado.isFailure) {
                // Deshacer el cambio local si el servidor no lo aceptó
                if (nuevoEstado) revisados.remove(k) else revisados.add(k)
                avisar("No se pudo guardar tu avance: ${mensajeError(resultado.exceptionOrNull())}")
            } else {
                guardarCache()
            }
        }
    }

    // ---------- Descargas para uso sin conexión (solo en el dispositivo) ----------

    fun estaDescargado(usuario: String, materialId: Int): Boolean =
        clave(usuario, materialId) in descargados

    /** Descarga: guarda el material en un archivo del teléfono. Quitar descarga: borra el archivo. */
    fun alternarDescargado(usuario: String, materialId: Int) {
        val k = clave(usuario, materialId)
        val archivo = archivoDescarga(usuario, materialId) ?: return
        if (k in descargados) {
            archivo.delete()
            descargados.remove(k)
            if (sinConexion) materiales.removeAll { it.id == materialId }
        } else {
            val m = material(materialId) ?: return
            val u = unidad(m.unidadId)
            val a = u?.let { asignatura(it.asignaturaId) }
            val ok = runCatching {
                archivo.parentFile?.mkdirs()
                archivo.writeText(gson.toJson(DescargaLocal(m, u, a)))
            }.isSuccess
            if (ok) descargados.add(k) else avisar("No se pudo guardar el material en el teléfono")
        }
    }

    fun progresoDe(usuario: String, lista: List<Material>): Progreso =
        Progreso(lista.count { estaRevisado(usuario, it.id) }, lista.size)

    // ---------- Escritura (estudiante / docente) ----------

    suspend fun enviarConsulta(
        emisor: String,
        asignaturaId: Int,
        unidadId: Int,
        materialId: Int?,
        asunto: String,
        contenido: String
    ): Result<Consulta> {
        val r = llamar {
            Api.servicio.enviarConsulta(
                ConsultaReq(asignaturaId, unidadId, materialId, emisor, asunto.trim(), contenido.trim())
            )
        }
        r.onSuccess { consultas.add(it); guardarCache() }
        return r
    }

    suspend fun responder(consultaId: Int, respuesta: String): Result<Consulta> {
        val r = llamar { Api.servicio.responder(consultaId, RespuestaReq(respuesta.trim())) }
        r.onSuccess { actualizada ->
            val i = consultas.indexOfFirst { it.id == consultaId }
            if (i >= 0) consultas[i] = actualizada else consultas.add(actualizada)
            guardarCache()
        }
        return r
    }

    suspend fun publicarMaterial(
        unidadId: Int,
        titulo: String,
        tipo: TipoMaterial,
        descripcion: String
    ): Result<Material> {
        val r = llamar {
            Api.servicio.publicarMaterial(
                MaterialReq(unidadId, titulo.trim(), tipo, descripcion.trim())
            )
        }
        r.onSuccess { materiales.add(it); guardarCache() }
        return r
    }

    // ---------- Errores ----------

    fun mensajeError(e: Throwable?): String = when (e) {
        is HttpException -> {
            val cuerpo = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
            val delServidor = cuerpo?.let {
                runCatching { JsonParser.parseString(it).asJsonObject.get("error").asString }.getOrNull()
            }
            delServidor ?: "El servidor respondió con error ${e.code()}"
        }
        is IOException -> "No se pudo conectar con el servidor. Revisa tu conexión."
        else -> "Ocurrió un error inesperado"
    }

    fun esConflicto(e: Throwable?): Boolean = e is HttpException && e.code() == 409

    // ---------- Internos ----------

    /** Ejecuta una llamada de red fuera del hilo principal y devuelve Result (sin lanzar). */
    private suspend fun <T> llamar(bloque: suspend () -> T): Result<T> = withContext(Dispatchers.IO) {
        try {
            Result.success(bloque())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun traer(usuario: String, rol: Rol): Datos {
        val api = Api.servicio
        return Datos(
            asignaturas = api.asignaturas(),
            unidades = api.unidades(),
            materiales = api.materiales(),
            consultas = if (rol == Rol.DOCENTE) api.consultas() else api.consultas(usuario),
            revisados = api.progreso(usuario)
        )
    }

    private fun aplicar(datos: Datos, usuario: String) {
        asignaturas.reemplazar(datos.asignaturas)
        unidades.reemplazar(datos.unidades)
        materiales.reemplazar(datos.materiales)
        consultas.reemplazar(datos.consultas)
        revisados.reemplazar(datos.revisados.map { clave(usuario, it) })
    }

    private fun <T> SnapshotStateList<T>.reemplazar(nuevos: List<T>) {
        if (toList() != nuevos) {
            clear()
            addAll(nuevos)
        }
    }

    private fun avisarNovedades(nuevo: Datos) {
        val ctx = contexto ?: return
        if (materiales.isEmpty() && consultas.isEmpty()) return // primera carga: nada que comparar

        val respondidasAntes = consultas.filter { it.respondida }.map { it.id }.toSet()
        nuevo.consultas.filter { it.respondida && it.id !in respondidasAntes }.forEach {
            Notificaciones.mostrar(ctx, it.id, "El docente respondió tu consulta", it.asunto)
        }

        val materialesAntes = materiales.map { it.id }.toSet()
        nuevo.materiales.filter { it.id !in materialesAntes }.forEach { m ->
            val unidad = nuevo.unidades.firstOrNull { it.id == m.unidadId }
            val codigo = nuevo.asignaturas.firstOrNull { it.id == unidad?.asignaturaId }?.codigo ?: ""
            Notificaciones.mostrar(ctx, 1000 + m.id, "Nuevo material de estudio", "$codigo: ${m.titulo}")
        }
    }

    private fun avisar(texto: String) {
        contexto?.let { Toast.makeText(it, texto, Toast.LENGTH_LONG).show() }
    }

    // ---------- Descargas y modo sin conexión ----------

    private fun carpetaDescargas(usuario: String): File? =
        contexto?.let { File(it.filesDir, "descargas/${usuario.lowercase()}") }

    private fun archivoDescarga(usuario: String, materialId: Int): File? =
        carpetaDescargas(usuario)?.let { File(it, "$materialId.json") }

    private fun leerDescargas(usuario: String): List<DescargaLocal> =
        carpetaDescargas(usuario)?.listFiles { f -> f.extension == "json" }
            ?.mapNotNull { f -> runCatching { gson.fromJson(f.readText(), DescargaLocal::class.java) }.getOrNull() }
            .orEmpty()

    private fun refrescarDescargados(usuario: String) {
        descargados.clear()
        leerDescargas(usuario).forEach { descargados.add(clave(usuario, it.material.id)) }
    }

    /** Sin internet: el estudiante ve solo lo descargado; el docente ve su última copia guardada. */
    private fun cargarSinConexion(usuario: String, rol: Rol) {
        val cache = leerCache(usuario)
        if (rol == Rol.ESTUDIANTE) {
            val descargas = leerDescargas(usuario)
            asignaturas.reemplazar(descargas.mapNotNull { it.asignatura }.distinctBy { it.id }.sortedBy { it.id })
            unidades.reemplazar(descargas.mapNotNull { it.unidad }.distinctBy { it.id }
                .sortedWith(compareBy({ it.asignaturaId }, { it.numero })))
            materiales.reemplazar(descargas.map { it.material }.sortedBy { it.id })
            consultas.reemplazar(cache?.consultas.orEmpty())
            revisados.reemplazar((cache?.revisados.orEmpty()).map { clave(usuario, it) })
        } else if (cache != null) {
            aplicar(cache, usuario)
        }
    }

    private fun hashClave(nombre: String, clave: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest("esucar:${nombre.lowercase()}:$clave".toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun guardarCredencial(u: Usuario, clave: String) {
        val c = CredencialLocal(u.id, u.nombre, u.rol, hashClave(u.nombre, clave))
        prefs()?.edit()?.putString("auth_${u.nombre.lowercase()}", gson.toJson(c))?.apply()
    }

    private fun loginLocal(nombre: String, clave: String): Usuario? {
        val json = prefs()?.getString("auth_${nombre.trim().lowercase()}", null) ?: return null
        val c = runCatching { gson.fromJson(json, CredencialLocal::class.java) }.getOrNull() ?: return null
        return if (c.hash == hashClave(c.nombre, clave)) Usuario(c.id, c.nombre, c.rol) else null
    }

    // ---------- Caché local ----------

    private fun prefs() = contexto?.getSharedPreferences("esucar_local", Context.MODE_PRIVATE)

    private fun claveCache(usuario: String) = "cache_${usuario.lowercase()}"

    private fun guardarCache() {
        val usuario = usuarioActual ?: return
        val datos = Datos(
            asignaturas.toList(),
            unidades.toList(),
            materiales.toList(),
            consultas.toList(),
            revisados.filter { it.startsWith("${usuario.lowercase()}#") }
                .map { it.substringAfter('#').toInt() }
        )
        prefs()?.edit()?.putString(claveCache(usuario), gson.toJson(datos))?.apply()
    }

    private fun leerCache(usuario: String): Datos? =
        prefs()?.getString(claveCache(usuario), null)
            ?.let { runCatching { gson.fromJson(it, Datos::class.java) }.getOrNull() }
}
