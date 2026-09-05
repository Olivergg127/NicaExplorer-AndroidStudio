package com.lospuntoycoma.nicaexplorer.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.lospuntoycoma.nicaexplorer.model.Comercio
import com.lospuntoycoma.nicaexplorer.model.UserProfile
import com.lospuntoycoma.nicaexplorer.model.UserRole
import kotlinx.coroutines.tasks.await
import java.util.Date

object FirebaseRepository {
    private const val TAG = "FirebaseRepository"

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * Obtiene el perfil completo del usuario desde Firestore incluyendo su rol.
     */
    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val document = db.collection("usuarios").document(uid).get().await()
            if (document.exists()) {
                val rolString = document.getString("rol") ?: "USUARIO"
                UserProfile(
                    uid = document.getString("uid") ?: "",
                    nombre = document.getString("nombre") ?: "",
                    correo = document.getString("correo") ?: "",
                    rol = try { UserRole.valueOf(rolString) } catch (e: Exception) { UserRole.USUARIO }
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun registerUser(email: String, password: String, fullName: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("No se pudo crear el usuario")

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build()
            user.updateProfile(profileUpdates).await()

            // Toda cuenta creada desde la aplicación comienza como USUARIO.
            // El administrador histórico se reconoce en Firestore Rules por correo
            // o por su documento existente, nunca desde este flujo de registro.
            val role = UserRole.USUARIO

            val userMap = hashMapOf(
                "uid" to user.uid,
                "nombre" to fullName,
                "correo" to email,
                "rol" to role.name,
                "fechaRegistro" to Date()
            )
            db.collection("usuarios").document(user.uid).set(userMap).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene los comercios activos desde Firestore (colección "comercios").
     * Solo lectura: no modifica ni escribe datos.
     */
    suspend fun getComerciosActivos(): Result<List<Comercio>> {
        return try {
            val snapshot = db.collection("comercios")
                .whereEqualTo("activo", true)
                .get()
                .await()
            val comercios = snapshot.documents.mapNotNull { doc ->
                try {
                    Comercio(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        categoria = doc.getString("categoria") ?: "",
                        descripcion = doc.getString("descripcion") ?: "",
                        ciudad = doc.getString("ciudad") ?: "",
                        direccion = doc.getString("direccion") ?: "",
                        horario = doc.getString("horario") ?: "",
                        imagenUrl = doc.getString("imagenUrl")
                            ?.takeIf { it.isNotBlank() }
                            ?: doc.getString("imagenurl").orEmpty(),
                        latitud = doc.getDouble("latitud") ?: 0.0,
                        longitud = doc.getDouble("longitud") ?: 0.0,
                        telefono = doc.getString("telefono") ?: "",
                        whatsapp = doc.getString("whatsapp") ?: "",
                        tieneWhatsapp = doc.getBoolean("tieneWhatsapp") ?: false,
                        activo = doc.getBoolean("activo") ?: false
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "getComerciosActivos: documento omitido (${doc.id})", e)
                    null
                }
            }
            Result.success(comercios)
        } catch (e: Exception) {
            Log.e(TAG, "getComerciosActivos: error al leer Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene un comercio por su ID desde Firestore (colección "comercios").
     * Solo lectura: no modifica ni escribe datos.
     */
    suspend fun getComercioById(id: String): Result<Comercio> {
        return try {
            val doc = db.collection("comercios").document(id).get().await()
            if (doc.exists()) {
                val comercio = Comercio(
                    id = doc.id,
                    nombre = doc.getString("nombre") ?: "",
                    categoria = doc.getString("categoria") ?: "",
                    descripcion = doc.getString("descripcion") ?: "",
                    ciudad = doc.getString("ciudad") ?: "",
                    direccion = doc.getString("direccion") ?: "",
                    horario = doc.getString("horario") ?: "",
                    imagenUrl = doc.getString("imagenUrl")
                        ?.takeIf { it.isNotBlank() }
                        ?: doc.getString("imagenurl").orEmpty(),
                    latitud = doc.getDouble("latitud") ?: 0.0,
                    longitud = doc.getDouble("longitud") ?: 0.0,
                    telefono = doc.getString("telefono") ?: "",
                    whatsapp = doc.getString("whatsapp") ?: "",
                    tieneWhatsapp = doc.getBoolean("tieneWhatsapp") ?: false,
                    activo = doc.getBoolean("activo") ?: false
                )
                Result.success(comercio)
            } else {
                Result.failure(Exception("Comercio no encontrado"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getComercioById: error al leer Firestore ($id)", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene la lista de todos los usuarios registrados (Solo para Admins).
     */
    suspend fun getAllUsers(): List<UserProfile> {
        return try {
            val snapshot = db.collection("usuarios").get().await()
            snapshot.documents.mapNotNull { doc ->
                val rolString = doc.getString("rol") ?: "USUARIO"
                UserProfile(
                    uid = doc.id,
                    nombre = doc.getString("nombre") ?: "",
                    correo = doc.getString("correo") ?: "",
                    rol = try { UserRole.valueOf(rolString) } catch (e: Exception) { UserRole.USUARIO }
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Actualiza el rol de un usuario específico.
     */
    suspend fun updateUserRole(uid: String, newRole: UserRole): Boolean {
        return try {
            db.collection("usuarios").document(uid)
                .update("rol", newRole.name)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Actualiza el nombre del usuario en Firestore y en Firebase Auth.
     * Usa set(merge = true) para que funcione también si el documento
     * usuarios/{uid} no existe (update() lanza NOT_FOUND en ese caso).
     */
    suspend fun updateUserName(uid: String, nombre: String): Boolean {
        var firestoreOk = false
        var authOk = false

        try {
            db.collection("usuarios").document(uid)
                .set(mapOf("nombre" to nombre), SetOptions.merge())
                .await()
            firestoreOk = true
        } catch (e: Exception) {
            Log.e(TAG, "updateUserName: error al guardar nombre en Firestore (usuarios/$uid)", e)
        }

        try {
            auth.currentUser?.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(nombre).build()
            )?.await()
            authOk = true
        } catch (e: Exception) {
            Log.e(TAG, "updateUserName: error al actualizar displayName en Firebase Auth", e)
        }

        return firestoreOk || authOk
    }

    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("No se pudo iniciar sesión")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Envía un correo de restablecimiento de contraseña con Firebase Authentication.
     * No revela si el correo está registrado o no.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
