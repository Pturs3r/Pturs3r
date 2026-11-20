package com.pturs3r.carlauncher

import android.content.Intent
import android.provider.Settings as AndroidSettings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Entiteiten die op het startscherm zinvol zijn: bedienbaar of uitleesbaar. */
private val USEFUL_DOMAINS = setOf(
    "light", "switch", "fan", "input_boolean", "automation", "media_player", "climate",
    "cover", "lock", "script", "scene", "button", "input_button",
    "sensor", "binary_sensor", "person", "device_tracker",
)

@Composable
fun SettingsScreen(
    state: LauncherState,
    apps: List<AppEntry>,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var url by remember { mutableStateOf(state.haUrl) }
    var token by remember { mutableStateOf(state.haToken) }
    var status by remember { mutableStateOf<String?>(null) }
    var allEntities by remember { mutableStateOf<List<HaEntity>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val entityNames = allEntities.associate { it.id to it.name }
    val appNames = apps.associate { it.packageName to it.label }

    fun loadEntities() {
        val client = state.haClient() ?: run { status = "Vul eerst adres en token in."; return }
        loading = true
        scope.launch {
            try {
                allEntities = client.states()
                    .filter { it.domain in USEFUL_DOMAINS }
                    .sortedWith(compareBy({ it.domain }, { it.name.lowercase() }))
                status = "Verbonden. ${allEntities.size} bruikbare entiteiten gevonden."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                status = "Fout: ${e.message ?: e.javaClass.simpleName}"
            } finally {
                loading = false
            }
        }
    }

    val filtered = if (query.isBlank()) allEntities else allEntities.filter {
        it.name.contains(query, ignoreCase = true) || it.id.contains(query, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CarColors.Background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { TopBar("Instellingen", onBack) }

        // ---------- Home Assistant-verbinding ----------
        item { SectionTitle("Home Assistant", Modifier.padding(top = 8.dp)) }
        item {
            Hint(
                "Gebruik een adres dat ook buiten je thuisnetwerk werkt (bijvoorbeeld je Nabu Casa-adres " +
                    "https://….ui.nabu.casa), want in de auto zit de box op 4G of op de hotspot van je telefoon. " +
                    "Het token maak je in Home Assistant onder Profiel → Beveiliging → Langlevende toegangstokens.",
            )
        }
        item {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Adres, bijv. https://xxxx.ui.nabu.casa") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("Langlevend toegangstoken") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    state.saveHa(url, token)
                    loadEntities()
                }) { Text("Opslaan en verbinden", fontSize = 17.sp) }
                if (state.haConfigured) {
                    OutlinedButton(onClick = { loadEntities() }, enabled = !loading) {
                        Text(if (loading) "Bezig…" else "Entiteiten verversen", fontSize = 17.sp)
                    }
                }
            }
        }
        status?.let { msg ->
            item {
                Text(
                    msg,
                    color = if (msg.startsWith("Fout")) CarColors.Error else CarColors.Accent,
                    fontSize = 16.sp,
                )
            }
        }

        // ---------- Gekozen knoppen ----------
        item { SectionTitle("Knoppen op het startscherm", Modifier.padding(top = 24.dp)) }
        if (state.haEntities.isEmpty()) {
            item { Hint("Nog niets gekozen. Vink hieronder entiteiten aan.") }
        }
        itemsIndexed(state.haEntities.toList()) { index, id ->
            OrderRow(
                title = entityNames[id] ?: id,
                subtitle = id,
                onUp = { state.moveEntity(index, -1) },
                onDown = { state.moveEntity(index, +1) },
                onRemove = { state.toggleEntity(id) },
            )
        }

        // ---------- Beschikbare entiteiten ----------
        if (allEntities.isNotEmpty()) {
            item { SectionTitle("Beschikbare entiteiten", Modifier.padding(top = 24.dp)) }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Zoeken") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            items(filtered, key = { it.id }) { entity ->
                val checked = entity.id in state.haEntities
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { state.toggleEntity(entity.id) }
                        .padding(vertical = 4.dp),
                ) {
                    Checkbox(checked = checked, onCheckedChange = { state.toggleEntity(entity.id) })
                    Column(Modifier.weight(1f)) {
                        Text(entity.name, color = CarColors.TextPrimary, fontSize = 17.sp)
                        Text(
                            "${entity.id} · ${entity.displayState}",
                            color = CarColors.TextSecondary,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        // ---------- Volgorde favoriete apps ----------
        item { SectionTitle("Volgorde favoriete apps", Modifier.padding(top = 24.dp)) }
        if (state.favorites.isEmpty()) {
            item { Hint("Nog geen favorieten. Zet ze via \"Alle apps\" met de ster op het startscherm.") }
        }
        itemsIndexed(state.favorites.toList()) { index, pkg ->
            OrderRow(
                title = appNames[pkg] ?: pkg,
                subtitle = if (pkg in appNames) null else "Niet geïnstalleerd",
                onUp = { state.moveFavorite(index, -1) },
                onDown = { state.moveFavorite(index, +1) },
                onRemove = { state.toggleFavorite(pkg) },
            )
        }

        // ---------- Systeem ----------
        item { SectionTitle("Systeem", Modifier.padding(top = 24.dp)) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = {
                    val intent = Intent(AndroidSettings.ACTION_HOME_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    runCatching { context.startActivity(intent) }.onFailure {
                        context.startActivity(
                            Intent(AndroidSettings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                }) { Text("Standaard startscherm kiezen", fontSize = 17.sp) }
                OutlinedButton(onClick = {
                    context.startActivity(
                        Intent(AndroidSettings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }) { Text("Android-instellingen", fontSize = 17.sp) }
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun OrderRow(
    title: String,
    subtitle: String?,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CarColors.Surface)
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = CarColors.TextPrimary, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            subtitle?.let { Text(it, color = CarColors.TextSecondary, fontSize = 13.sp) }
        }
        IconButton(onClick = onUp) { Icon(Icons.Filled.KeyboardArrowUp, "Omhoog", tint = CarColors.TextPrimary) }
        IconButton(onClick = onDown) { Icon(Icons.Filled.KeyboardArrowDown, "Omlaag", tint = CarColors.TextPrimary) }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onRemove) { Icon(Icons.Filled.Delete, "Verwijderen", tint = CarColors.Error) }
    }
}
