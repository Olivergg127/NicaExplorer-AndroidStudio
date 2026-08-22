package com.lospuntoycoma.nicaexplorer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lospuntoycoma.nicaexplorer.data.FirebaseRepository
import com.lospuntoycoma.nicaexplorer.data.SampleData
import com.lospuntoycoma.nicaexplorer.data.UserPreferences
import com.lospuntoycoma.nicaexplorer.ui.components.EmptyState
import com.lospuntoycoma.nicaexplorer.ui.components.MonumentRow
import com.lospuntoycoma.nicaexplorer.ui.components.NicaTopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LugaresGuardadosScreen(
    onExplore: () -> Unit,
    onOpenMonument: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val uid = FirebaseRepository.getCurrentUser()?.uid ?: ""
    val savedIds by UserPreferences.savedPlacesFlow(uid).collectAsState(initial = emptySet())

    val monuments = remember(savedIds) {
        SampleData.allMonuments.filter { it.id in savedIds }
    }

    Scaffold(
        topBar = {
            NicaTopBar(
                title = "Lugares guardados",
                onBack = onBack
            )
        }
    ) { padding ->
        if (monuments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                EmptyState(
                    icon = Icons.Filled.Bookmark,
                    message = "No tienes lugares guardados todavía.",
                    buttonText = "Explorar lugares",
                    onButtonClick = onExplore
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(monuments, key = { it.id }) { monument ->
                    MonumentRow(
                        monument = monument,
                        onClick = { onOpenMonument(monument.cityId, monument.id) },
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        UserPreferences.toggleSavedPlace(uid, monument.id)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bookmark,
                                    contentDescription = "Quitar de guardados",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
