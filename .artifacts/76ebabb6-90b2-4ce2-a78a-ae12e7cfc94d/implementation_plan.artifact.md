# Sistema de Administración y Gestión de Roles Interno

El objetivo es implementar un "Súper Administrador" inicial y un Panel de Administración dentro de la app para que este usuario pueda gestionar y promover a otros usuarios sin depender de la consola de Firebase.

## Estrategia de Gestión de Roles

1.  **Súper Admin Inicial:** Definiremos un correo electrónico maestro (ej: `admin@nicaexplorer.com`). Si alguien se registra con este correo, obtendrá el rol `ADMIN` automáticamente.
2.  **Panel de Administración:** Una nueva pantalla exclusiva para Admins donde podrán ver la lista de todos los usuarios registrados y cambiar su rol (Usuario, Auditor o Admin).
3.  **Seguridad:** Las funciones de edición de roles solo funcionarán si el usuario autenticado tiene el rol `ADMIN`.

## Cambios Propuestos

### Repositorio de Datos

#### [MODIFY] [FirebaseRepository.kt](file:///C:/Users/josep/StudioProjects/NicaExplorer-AndroidStudio/app/src/main/java/com/lospuntoycoma/nicaexplorer/data/FirebaseRepository.kt)
- **Súper Admin:** Modificar `registerUser` para asignar `ADMIN` automáticamente si el correo es `admin@nicaexplorer.com`.
- **Gestión:** Añadir `getAllUsers()` para obtener la lista de todos los perfiles.
- **Edición:** Añadir `updateUserRole(uid, newRole)` para guardar el cambio en Firestore.

### Interfaz de Usuario

#### [NEW] [AdminPanelScreen.kt](file:///C:/Users/josep/StudioProjects/NicaExplorer-AndroidStudio/app/src/main/java/com/lospuntoycoma/nicaexplorer/ui/screens/AdminPanelScreen.kt)
- Lista de usuarios con scroll.
- Botones de acción para cada usuario: "Hacer Admin", "Hacer Auditor", "Quitar permisos".

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/josep/StudioProjects/NicaExplorer-AndroidStudio/app/src/main/java/com/lospuntoycoma/nicaexplorer/ui/screens/HomeScreen.kt)
- Añadir un acceso al **Panel de Administración** en el menú o perfil, visible solo para el `ADMIN`.

### Navegación

#### [MODIFY] [AppNavigation.kt](file:///C:/Users/josep/StudioProjects/NicaExplorer-AndroidStudio/app/src/main/java/com/lospuntoycoma/nicaexplorer/navigation/AppNavigation.kt)
- Registrar la nueva ruta para el Panel de Administración.

## Verificación Plan

### Manual Verification
1.  **Súper Admin:** Registrarse con `admin@nicaexplorer.com` y verificar que el rol es `ADMIN`.
2.  **Gestión de Usuarios:** Como Admin, entrar al Panel y promover a otro usuario a `AUDITOR`.
3.  **Validación:** Cerrar sesión, entrar con el usuario promovido y verificar que ahora tiene acceso a funciones de Auditor.

> [!IMPORTANT]
> **Correo del Súper Admin:**
> ¿Deseas usar `admin@nicaexplorer.com` como el correo maestro o prefieres que usemos tu correo personal actual?
