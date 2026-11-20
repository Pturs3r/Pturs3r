package com.pturs3r.carlauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AllAppsScreen(
    state: LauncherState,
    apps: List<AppEntry>,
    onLaunch: (String) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(CarColors.Background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        TopBar("Alle apps", onBack)
        Hint(
            "Tik op een app om hem te starten. Tik op de ster om hem op het startscherm te zetten.",
            Modifier.padding(start = 8.dp, bottom = 16.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(140.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(apps, key = { it.packageName }) { app ->
                val favorite = state.isFavorite(app.packageName)
                AppTile(
                    app = app,
                    size = 140.dp,
                    onClick = { onLaunch(app.packageName) },
                    corner = {
                        IconButton(onClick = { state.toggleFavorite(app.packageName) }) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = if (favorite) "Verwijder van startscherm" else "Zet op startscherm",
                                tint = if (favorite) CarColors.AccentWarm else CarColors.SurfaceHigh,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    },
                )
            }
        }
    }
}
