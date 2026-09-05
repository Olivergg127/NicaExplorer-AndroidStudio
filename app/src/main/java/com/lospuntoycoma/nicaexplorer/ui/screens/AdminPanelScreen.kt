package com.lospuntoycoma.nicaexplorer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lospuntoycoma.nicaexplorer.data.FirebaseRepository
import com.lospuntoycoma.nicaexplorer.model.UserProfile
import com.lospuntoycoma.nicaexplorer.model.UserRole
import com.lospuntoycoma.nicaexplorer.ui.viewmodels.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(userViewModel: UserViewModel, onBack: () -> Unit) {
    val currentProfile by userViewModel.userProfile.collectAsState()
    val canEditRoles = currentProfile?.rol == UserRole.ADMIN
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var users by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<UserRole?>(null) }
    var selectedUser by remember { mutableStateOf<UserProfile?>(null) }
    var pendingRole by remember { mutableStateOf<UserRole?>(null) }
    var changing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        users = FirebaseRepository.getAllUsers()
        isLoading = false
    }
    val filteredUsers = remember(users, query, filter) {
        users.filter { (query.isBlank() || it.nombre.contains(query, true) || it.correo.contains(query, true)) &&
            (filter == null || it.rol == filter) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (canEditRoles) "Panel de Administración" else "Consulta administrativa") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Buscar por nombre o correo") }
                    )
                }
                item {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RoleCount("Todos", users.size, filter == null) { filter = null }
                        RoleCount("Usuario", users.count { it.rol == UserRole.USUARIO }, filter == UserRole.USUARIO) { filter = UserRole.USUARIO }
                        RoleCount("Admin", users.count { it.rol == UserRole.ADMIN }, filter == UserRole.ADMIN) { filter = UserRole.ADMIN }
                        RoleCount("Auditor", users.count { it.rol == UserRole.AUDITOR }, filter == UserRole.AUDITOR) { filter = UserRole.AUDITOR }
                    }
                }
                item { Text(if (canEditRoles) "Gestión de usuarios" else "Usuarios (solo lectura)", style = MaterialTheme.typography.titleLarge) }
                items(filteredUsers, key = { it.uid }) { user ->
                    UserItem(user, canEditRoles) { selectedUser = user; pendingRole = null }
                }
            }
        }
    }

    if (selectedUser != null && canEditRoles && pendingRole == null) {
        RolePickerDialog(selectedUser!!, { selectedUser = null }) { pendingRole = it }
    }
    if (selectedUser != null && canEditRoles && pendingRole != null) {
        val target = selectedUser!!
        val role = pendingRole!!
        AlertDialog(
            onDismissRequest = { if (!changing) { selectedUser = null; pendingRole = null } },
            title = { Text("Confirmar cambio de rol") },
            text = { Text("¿Cambiar el rol de ${target.nombre} a ${role.displayName()}?") },
            confirmButton = {
                Button(enabled = !changing, onClick = {
                    scope.launch {
                        changing = true
                        val success = FirebaseRepository.updateUserRole(target.uid, role)
                        if (success) users = FirebaseRepository.getAllUsers()
                        changing = false
                        selectedUser = null
                        pendingRole = null
                        snackbarHostState.showSnackbar(if (success) "Rol actualizado correctamente" else "No se pudo actualizar el rol")
                    }
                }) { Text(if (changing) "Guardando..." else "Confirmar") }
            },
            dismissButton = { TextButton(onClick = { selectedUser = null; pendingRole = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun RoleCount(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text("${label}: ${count}") })
}

@Composable
private fun RolePickerDialog(user: UserProfile, onDismiss: () -> Unit, onRoleSelected: (UserRole) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar rol") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(user.correo, style = MaterialTheme.typography.bodySmall)
                UserRole.values().forEach { role ->
                    OutlinedButton(onClick = { onRoleSelected(role) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text(role.displayName())
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun UserItem(user: UserProfile, canEditRoles: Boolean, onRoleChange: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(user.nombre, style = MaterialTheme.typography.titleMedium)
                Text(user.correo, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(6.dp))
                AssistChip(onClick = {}, label = { Text(user.rol.displayName()) }, leadingIcon = { Icon(Icons.Default.Badge, null, Modifier.size(16.dp)) })
            }
            if (canEditRoles) {
                Button(onClick = onRoleChange, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                    Text("Cambiar rol")
                }
            }
        }
    }
}

private fun UserRole.displayName() = when (this) {
    UserRole.USUARIO -> "Usuario"
    UserRole.ADMIN -> "Administrador"
    UserRole.AUDITOR -> "Auditor"
}
