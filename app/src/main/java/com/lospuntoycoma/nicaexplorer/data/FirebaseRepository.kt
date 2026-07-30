package com.lospuntoycoma.nicaexplorer.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.lospuntoycoma.nicaexplorer.model.UserProfile
import com.lospuntoycoma.nicaexplorer.model.UserRole
import kotlinx.coroutines.tasks.await
import java.util.Date

object FirebaseRepository {
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

            // Asignar ADMIN automáticamente si el correo coincide con el maestro
            val role = if (email.lowercase() == "admin@nicaexplorer.com") {
                UserRole.ADMIN
            } else {
                UserRole.USUARIO
            }

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

    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("No se pudo iniciar sesión")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
