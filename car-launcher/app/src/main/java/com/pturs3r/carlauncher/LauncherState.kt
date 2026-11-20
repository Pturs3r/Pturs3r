package com.pturs3r.carlauncher

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue

/** Gedeelde, waarneembare toestand van de launcher. Alles wordt direct opgeslagen. */
class LauncherState(private val prefs: LauncherPrefs) {

    val favorites = mutableStateListOf<String>().apply { addAll(prefs.favorites) }
    val haEntities = mutableStateListOf<String>().apply { addAll(prefs.haEntities) }

    /** Wordt opgehoogd als de HA-verbinding wijzigt, zodat schermen opnieuw laden. */
    var haConfigVersion by mutableIntStateOf(0)
        private set

    val haUrl: String get() = prefs.haUrl
    val haToken: String get() = prefs.haToken
    val haConfigured: Boolean get() = prefs.haConfigured

    fun haClient(): HomeAssistantClient? =
        if (prefs.haConfigured) HomeAssistantClient(prefs.haUrl, prefs.haToken) else null

    fun saveHa(url: String, token: String) {
        prefs.haUrl = url
        prefs.haToken = token
        haConfigVersion++
    }

    fun isFavorite(pkg: String) = pkg in favorites

    fun toggleFavorite(pkg: String) {
        if (!favorites.remove(pkg)) favorites.add(pkg)
        prefs.favorites = favorites.toList()
    }

    fun moveFavorite(index: Int, delta: Int) {
        val target = index + delta
        if (index !in favorites.indices || target !in favorites.indices) return
        val item = favorites.removeAt(index)
        favorites.add(target, item)
        prefs.favorites = favorites.toList()
    }

    fun toggleEntity(id: String) {
        if (!haEntities.remove(id)) haEntities.add(id)
        prefs.haEntities = haEntities.toList()
    }

    fun moveEntity(index: Int, delta: Int) {
        val target = index + delta
        if (index !in haEntities.indices || target !in haEntities.indices) return
        val item = haEntities.removeAt(index)
        haEntities.add(target, item)
        prefs.haEntities = haEntities.toList()
    }
}
