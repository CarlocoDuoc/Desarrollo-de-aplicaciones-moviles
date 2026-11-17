package com.example.esucaraplicacion.data

import com.example.esucaraplicacion.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class LoginReq(val nombre: String, val clave: String)
data class RegistroReq(val nombre: String, val clave: String, val rol: Rol)
data class MaterialReq(val unidadId: Int, val titulo: String, val tipo: TipoMaterial, val descripcion: String)
data class ConsultaReq(
    val asignaturaId: Int,
    val unidadId: Int,
    val materialId: Int?,
    val emisor: String,
    val asunto: String,
    val contenido: String
)
data class RespuestaReq(val respuesta: String)

interface EsucarApi {
    @POST("api/auth/login")
    suspend fun login(@Body req: LoginReq): Usuario

    @POST("api/auth/registro")
    suspend fun registro(@Body req: RegistroReq): Usuario

    @GET("api/asignaturas")
    suspend fun asignaturas(): List<Asignatura>

    @GET("api/unidades")
    suspend fun unidades(): List<UnidadTematica>

    @GET("api/materiales")
    suspend fun materiales(): List<Material>

    @POST("api/materiales")
    suspend fun publicarMaterial(@Body req: MaterialReq): Material

    @GET("api/consultas")
    suspend fun consultas(@Query("emisor") emisor: String? = null): List<Consulta>

    @POST("api/consultas")
    suspend fun enviarConsulta(@Body req: ConsultaReq): Consulta

    @PUT("api/consultas/{id}/respuesta")
    suspend fun responder(@Path("id") id: Int, @Body req: RespuestaReq): Consulta

    @GET("api/progreso")
    suspend fun progreso(@Query("usuario") usuario: String): List<Int>

    @PUT("api/progreso/{materialId}")
    suspend fun marcarProgreso(
        @Path("materialId") materialId: Int,
        @Query("usuario") usuario: String,
        @Query("revisado") revisado: Boolean
    )
}

object Api {
    val servicio: EsucarApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EsucarApi::class.java)
    }
}
