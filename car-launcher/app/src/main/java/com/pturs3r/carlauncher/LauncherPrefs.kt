package com.pturs3r.carlauncher

import android.content.Context

/**
 * Eenvoudige opslag van instellingen.
 *
 * Let op: het Home Assistant-token staat onversleuteld in de app-opslag.
 * Gebruik daarom een apart token met alleen de rechten die nodig zijn
 * (bijvoorbeeld een aparte HA-gebruiker zonder beheerrechten).
 */
class LauncherPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("launcher", Context.MODE_PRIVATE)

    var favorites: List<String>
        get() = prefs.getString(KEY_FAVORITES, "").orEmpty().splitList()
        set(value) = prefs.edit().putString(KEY_FAVORITES, value.joinToString(SEP)).apply()

    var haUrl: String
        get() = prefs.getString(KEY_HA_URL, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_HA_URL, value.trim().trimEnd('/')).apply()

    var haToken: String
        get() = prefs.getString(KEY_HA_TOKEN, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_HA_TOKEN, value.trim()).apply()

    var haEntities: List<String>
        get() = prefs.getString(KEY_HA_ENTITIES, "").orEmpty().splitList()
        set(value) = prefs.edit().putString(KEY_HA_ENTITIES, value.joinToString(SEP)).apply()

    val haConfigured: Boolean
        get() = haUrl.isNotBlank() && haToken.isNotBlank()

    private fun String.splitList(): List<String> =
        split(SEP).map { it.trim() }.filter { it.isNotEmpty() }

    private companion object {
        const val SEP = ","
        const val KEY_FAVORITES = "favorites"
        const val KEY_HA_URL = "ha_url"
        const val KEY_HA_TOKEN = "ha_token"
        const val KEY_HA_ENTITIES = "ha_entities"
    }
}
