package com.pturs3r.carlauncher

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class HaEntity(
    val id: String,
    val name: String,
    val state: String,
    val unit: String?,
) {
    val domain: String get() = id.substringBefore('.')

    /** Of de tegel "aan" moet ogen. */
    val isActive: Boolean
        get() = state in setOf("on", "open", "opening", "unlocked", "playing", "home", "heat", "cool")

    /** Wat er gebeurt bij een tik, of null als de entiteit alleen uitgelezen wordt. */
    val action: Pair<String, String>?
        get() = when (domain) {
            "light", "switch", "fan", "input_boolean", "automation", "media_player", "climate" ->
                "homeassistant" to "toggle"
            "cover" -> "cover" to "toggle"
            "lock" -> "lock" to if (state == "locked") "unlock" else "lock"
            "script" -> "script" to "turn_on"
            "scene" -> "scene" to "turn_on"
            "button", "input_button" -> domain to "press"
            else -> null
        }

    /** Tekst voor op de tegel. */
    val displayState: String
        get() = when {
            unit != null -> "$state $unit"
            domain in setOf("script", "scene", "button", "input_button") -> "Uitvoeren"
            else -> when (state) {
                "on" -> "Aan"
                "off" -> "Uit"
                "open" -> "Open"
                "closed" -> "Dicht"
                "opening" -> "Gaat open"
                "closing" -> "Gaat dicht"
                "locked" -> "Op slot"
                "unlocked" -> "Open"
                "unavailable" -> "Niet bereikbaar"
                "unknown" -> "Onbekend"
                "home" -> "Thuis"
                "not_home" -> "Weg"
                else -> state
            }
        }
}

/**
 * Minimale client voor de REST-API van Home Assistant.
 * Docs: https://developers.home-assistant.io/docs/api/rest/
 */
class HomeAssistantClient(baseUrl: String, private val token: String) {
    private val base = baseUrl.trim().trimEnd('/')

    suspend fun ping(): String = withContext(Dispatchers.IO) {
        JSONObject(request("GET", "/api/")!!).optString("message", "OK")
    }

    suspend fun states(): List<HaEntity> = withContext(Dispatchers.IO) {
        val arr = JSONArray(request("GET", "/api/states")!!)
        (0 until arr.length()).map { parse(arr.getJSONObject(it)) }
    }

    /** Haalt alleen de opgegeven entiteiten op; verwijderde entiteiten worden overgeslagen. */
    suspend fun states(ids: List<String>): List<HaEntity> = withContext(Dispatchers.IO) {
        ids.mapNotNull { id ->
            request("GET", "/api/states/" + URLEncoder.encode(id, "UTF-8"), allow404 = true)
                ?.let { parse(JSONObject(it)) }
        }
    }

    suspend fun trigger(entity: HaEntity) = withContext(Dispatchers.IO) {
        val (domain, service) = entity.action ?: return@withContext
        val body = JSONObject().put("entity_id", entity.id).toString()
        request("POST", "/api/services/$domain/$service", body)
        Unit
    }

    private fun parse(o: JSONObject): HaEntity {
        val attrs = o.optJSONObject("attributes") ?: JSONObject()
        val id = o.getString("entity_id")
        return HaEntity(
            id = id,
            name = attrs.optString("friendly_name").ifBlank { id },
            state = o.optString("state"),
            unit = attrs.optString("unit_of_measurement").ifBlank { null },
        )
    }

    private fun request(
        method: String,
        path: String,
        body: String? = null,
        allow404: Boolean = false,
    ): String? {
        val conn = URL(base + path).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method
            conn.connectTimeout = 5_000
            conn.readTimeout = 8_000
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/json")
            if (body != null) {
                conn.doOutput = true
                conn.outputStream.use { it.write(body.toByteArray()) }
            }
            val code = conn.responseCode
            if (code == 404 && allow404) return null
            if (code == 401) throw IOException("Token ongeldig (401)")
            if (code !in 200..299) throw IOException("Home Assistant gaf foutcode $code")
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}
