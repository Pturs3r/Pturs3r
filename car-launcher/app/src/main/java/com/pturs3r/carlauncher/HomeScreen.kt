package com.pturs3r.carlauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val HA_REFRESH_MS = 15_000L

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    state: LauncherState,
    apps: List<AppEntry>,
    onLaunch: (String) -> Unit,
    onOpenAllApps: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val appMap = remember(apps) { apps.associateBy { it.packageName } }
    val favApps = state.favorites.mapNotNull { appMap[it] }

    var haStates by remember { mutableStateOf<Map<String, HaEntity>>(emptyMap()) }
    var haError by remember { mutableStateOf<String?>(null) }
    var refreshTick by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val selectedIds = state.haEntities.toList()

    // Alleen de gekozen entiteiten ophalen (scheelt mobiele data onderweg).
    LaunchedEffect(state.haConfigVersion, refreshTick, selectedIds) {
        val client = state.haClient() ?: return@LaunchedEffect
        if (selectedIds.isEmpty()) return@LaunchedEffect
        while (true) {
            try {
                haStates = client.states(selectedIds).associateBy { it.id }
                haError = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                haError = e.message ?: "Geen verbinding met Home Assistant"
            }
            delay(HA_REFRESH_MS)
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(CarColors.Background)
            .padding(24.dp),
    ) {
        val sideWidth = (maxWidth * 0.27f).coerceIn(200.dp, 340.dp)

        Row(Modifier.fillMaxSize()) {
            // Linkerkolom: klok en menu
            Column(
                modifier = Modifier.width(sideWidth).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Clock()
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BigButton("Alle apps", Icons.Filled.Menu, onOpenAllApps)
                    BigButton("Instellingen", Icons.Filled.Settings, onOpenSettings)
                }
            }

            Spacer(Modifier.width(28.dp))

            // Rechterdeel: favoriete apps en Home Assistant
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
            ) {
                SectionTitle("Apps")
                if (favApps.isEmpty()) {
                    Hint("Nog geen favorieten. Open \"Alle apps\" en tik op de ster bij bijvoorbeeld calimoto, Kurviger of Spotify.")
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        favApps.forEach { app ->
                            AppTile(app, onClick = { onLaunch(app.packageName) })
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Row(verticalAlignment = Alignment.Top) {
                    SectionTitle("Thuis")
                    haError?.let {
                        Spacer(Modifier.width(16.dp))
                        Text(it, color = CarColors.Error, fontSize = 14.sp)
                    }
                }
                when {
                    !state.haConfigured ->
                        Hint("Koppel Home Assistant via Instellingen.")
                    selectedIds.isEmpty() ->
                        Hint("Kies in Instellingen welke Home Assistant-knoppen je hier wilt zien.")
                    else -> FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        selectedIds.forEach { id ->
                            val entity = haStates[id]
                            HaTile(id, entity, onClick = {
                                val client = state.haClient()
                                if (entity != null && client != null) {
                                    scope.launch {
                                        try {
                                            client.trigger(entity)
                                            haError = null
                                        } catch (e: CancellationException) {
                                            throw e
                                        } catch (e: Exception) {
                                            haError = e.message ?: "Actie mislukt"
                                        }
                                        delay(700)
                                        refreshTick++
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Clock() {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1_000)
        }
    }
    val dutch = remember { Locale.forLanguageTag("nl-NL") }
    Column {
        Text(
            text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
            color = CarColors.TextPrimary,
            fontSize = 84.sp,
            fontWeight = FontWeight.Light,
        )
        Text(
            text = now.format(DateTimeFormatter.ofPattern("EEEE d MMMM", dutch))
                .replaceFirstChar { it.uppercase() },
            color = CarColors.TextSecondary,
            fontSize = 22.sp,
        )
    }
}
