package com.lospuntoycoma.nicaexplorer.model

/**
 * Define los roles posibles dentro de la aplicación NicaExplorer.
 */
enum class UserRole {
    ADMIN,    // Control total y gestión de contenido y usuarios
    EDITOR,   // Gestiona contenido operativo (lugares, comercios, rutas)
    USUARIO,  // Turista estándar / visitante registrado
    COMERCIO, // Cuenta de negocio: administra sus propios comercios
    AUDITOR   // Visualización de reportes y logs
}
