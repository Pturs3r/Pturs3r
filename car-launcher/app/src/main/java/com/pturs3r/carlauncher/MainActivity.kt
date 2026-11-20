package com.pturs3r.carlauncher

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class Screen { Home, AllApps, Settings }

class MainActivity : ComponentActivity() {
    private val screen = mutableStateOf(Screen.Home)
    private val resumeTick = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemBars()

        val state = LauncherState(LauncherPrefs(this))
        setContent {
            LauncherTheme {
                LauncherApp(
                    state = state,
                    screen = screen.value,
                    onScreen = { screen.value = it },
                    resumeTick = resumeTick.intValue,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Home-knop ingedrukt terwijl de launcher al open is: terug naar het startscherm.
        screen.value = Screen.Home
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        resumeTick.intValue++
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

@Composable
fun LauncherApp(
    state: LauncherState,
    screen: Screen,
    onScreen: (Screen) -> Unit,
    resumeTick: Int,
) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }

    // Apps opnieuw inlezen telkens als de launcher weer zichtbaar wordt
    // (bijv. na het installeren van een nieuwe app).
    LaunchedEffect(resumeTick) {
        apps = withContext(Dispatchers.Default) { AppRepository.load(context) }
    }

    val launch: (String) -> Unit = { pkg ->
        if (!AppRepository.launch(context, pkg)) {
            Toast.makeText(context, "App kan niet gestart worden", Toast.LENGTH_SHORT).show()
        }
    }

    // Terug-knop: altijd naar het startscherm; op het startscherm zelf niets doen.
    BackHandler(enabled = true) { onScreen(Screen.Home) }

    when (screen) {
        Screen.Home -> HomeScreen(
            state = state,
            apps = apps,
            onLaunch = launch,
            onOpenAllApps = { onScreen(Screen.AllApps) },
            onOpenSettings = { onScreen(Screen.Settings) },
        )
        Screen.AllApps -> AllAppsScreen(
            state = state,
            apps = apps,
            onLaunch = launch,
            onBack = { onScreen(Screen.Home) },
        )
        Screen.Settings -> SettingsScreen(
            state = state,
            apps = apps,
            onBack = { onScreen(Screen.Home) },
        )
    }
}
