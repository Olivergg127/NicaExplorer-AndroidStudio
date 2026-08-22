package com.lospuntoycoma.nicaexplorer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lospuntoycoma.nicaexplorer.data.FirebaseRepository
import com.lospuntoycoma.nicaexplorer.data.SampleData
import com.lospuntoycoma.nicaexplorer.data.UserPreferences
import com.lospuntoycoma.nicaexplorer.model.Monument
import com.lospuntoycoma.nicaexplorer.ui.components.EmptyState
import com.lospuntoycoma.nicaexplorer.ui.components.MonumentRow
import com.lospuntoycoma.nicaexplorer.ui.components.NicaTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialExploracionScreen(
    onExplore: () -> Unit,
    onOpenMonument: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val uid = FirebaseRepository.getCurrentUser()?.uid ?: ""
    val history by UserPreferences.historyFlow(uid).collectAsState(initial = emptyMap())

    val entries = remember(history) {
        history.entries
            .mapNotNull { (monumentId, timestamp) ->
                SampleData.allMonuments.find { it.id == monumentId }
                    ?.let { monument -> HistoryEntry(monument, timestamp) }
            }
            .sortedByDescending { it.timestamp }
    }

    Scaffold(
        topBar = {
            NicaTopBar(
                title = "Historial de exploración",
                onBack = onBack
            )
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                EmptyState(
                    icon = Icons.Filled.History,
                    message = "Todavía no has explorado ningún lugar.",
                    buttonText = "Explorar ahora",
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
                items(entries, key = { it.monument.id }) { entry ->
                    MonumentRow(
                        monument = entry.monument,
                        subtitle = "Visto por última vez: ${formatTimestamp(entry.timestamp)}",
                        onClick = { onOpenMonument(entry.monument.cityId, entry.monument.id) }
                    )
                }
            }
        }
    }
}

private data class HistoryEntry(
    val monument: Monument,
    val timestamp: Long
)

private fun formatTimestamp(timestamp: Long): String {
    return try {
        val date = Date(timestamp)
        SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(date)
    } catch (e: Exception) {
        ""
    }
}
