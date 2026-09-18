package com.lospuntoycoma.nicaexplorer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Contenedor de pull-to-refresh reutilizable.
 *
 * Muestra el indicador cuando [isRefreshing] es true y llama a [onRefresh] al
 * deslizar hacia abajo. Se usa como refuerzo del refresco automático del catálogo.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NicaRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = onRefresh)

    Box(modifier = modifier.pullRefresh(state)) {
        content()

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = state,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
        )
    }
}
