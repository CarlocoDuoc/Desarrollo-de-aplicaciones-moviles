package com.example.esucaraplicacion.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class UsuarioDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, NOMBRE_BD, null, VERSION_BD) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLA (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE COLLATE NOCASE,
                salt TEXT NOT NULL,
                clave_hash TEXT NOT NULL,
                rol TEXT NOT NULL
            )
            """.trimIndent()
        )

        insertar(db, "alumno", "1234", Rol.ESTUDIANTE)
        insertar(db, "docente", "1234", Rol.DOCENTE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA")
        onCreate(db)
    }

    fun registrar(nombre: String, clave: String, rol: Rol): Usuario? {
        val limpio = nombre.trim()
        return try {
            val id = insertar(writableDatabase, limpio, clave, rol)
            Usuario(id, limpio, rol)
        } catch (e: SQLiteConstraintException) {
            null
        }
    }

    fun autenticar(nombre: String, clave: String): Usuario? {
        return readableDatabase.query(
            TABLA,
            arrayOf("id", "nombre", "salt", "clave_hash", "rol"),
            "nombre = ?",
            arrayOf(nombre.trim()),
            null, null, null,
            "1"
        ).use { cursor ->
            if (!cursor.moveToFirst()) {
                null
            } else {
                val salt = cursor.getString(2)
                val hashGuardado = cursor.getString(3)
                val hashIngresado = hash(clave, salt)
                if (MessageDigest.isEqual(hashIngresado.toByteArray(), hashGuardado.toByteArray())) {
                    Usuario(
                        id = cursor.getLong(0),
                        nombre = cursor.getString(1),
                        rol = Rol.valueOf(cursor.getString(4))
                    )
                } else {
                    null
                }
            }
        }
    }

    private fun insertar(db: SQLiteDatabase, nombre: String, clave: String, rol: Rol): Long {
        val salt = nuevaSalt()
        val valores = ContentValues().apply {
            put("nombre", nombre)
            put("salt", salt)
            put("clave_hash", hash(clave, salt))
            put("rol", rol.name)
        }
        return db.insertOrThrow(TABLA, null, valores)
    }

    companion object {
        private const val NOMBRE_BD = "esucar.db"
        private const val VERSION_BD = 2
        private const val TABLA = "usuarios"
        private const val ITERACIONES = 10_000

        @Volatile
        private var instancia: UsuarioDbHelper? = null

        fun obtener(context: Context): UsuarioDbHelper =
            instancia ?: synchronized(this) {
                instancia ?: UsuarioDbHelper(context).also { instancia = it }
            }

        private fun nuevaSalt(): String {
            val bytes = ByteArray(16)
            SecureRandom().nextBytes(bytes)
            return bytes.aHex()
        }

        private fun hash(clave: String, saltHex: String): String {
            val spec = PBEKeySpec(clave.toCharArray(), saltHex.deHexABytes(), ITERACIONES, 160)
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
            return bytes.aHex()
        }

        private fun ByteArray.aHex(): String = joinToString("") { "%02x".format(it) }

        private fun String.deHexABytes(): ByteArray =
            ByteArray(length / 2) { i -> substring(i * 2, i * 2 + 2).toInt(16).toByte() }
    }
}
