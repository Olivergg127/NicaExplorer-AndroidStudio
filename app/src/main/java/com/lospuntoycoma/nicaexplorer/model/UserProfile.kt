package com.lospuntoycoma.nicaexplorer.model

/**
 * Representa el perfil completo de un usuario con su rol asignado.
 */
data class UserProfile(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val rol: UserRole = UserRole.USUARIO
)
