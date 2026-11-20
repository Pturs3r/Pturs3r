package com.pturs3r.carlauncher

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

data class AppEntry(
    val label: String,
    val packageName: String,
    val icon: ImageBitmap,
)

object AppRepository {
    private const val ICON_PX = 160

    /** Alle apps met een startpictogram, behalve deze launcher zelf. Niet op de UI-thread aanroepen. */
    fun load(context: Context): List<AppEntry> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .asSequence()
            .filter { it.activityInfo.packageName != context.packageName }
            .distinctBy { it.activityInfo.packageName }
            .map { info ->
                AppEntry(
                    label = info.loadLabel(pm).toString(),
                    packageName = info.activityInfo.packageName,
                    icon = info.loadIcon(pm).toBitmap(ICON_PX, ICON_PX).asImageBitmap(),
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    /** Start een app. Geeft false terug als de app niet (meer) bestaat. */
    fun launch(context: Context, packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(intent) }.isSuccess
    }
}
