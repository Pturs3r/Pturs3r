package com.pturs3r.carlauncher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TileShape = RoundedCornerShape(22.dp)

@Composable
fun AppTile(
    app: AppEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    corner: (@Composable () -> Unit)? = null,
) {
    Box(modifier) {
        Column(
            modifier = Modifier
                .size(size)
                .clip(TileShape)
                .background(CarColors.Surface)
                .clickable(onClick = onClick)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                bitmap = app.icon,
                contentDescription = null,
                modifier = Modifier.size(size * 0.45f),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = app.label,
                color = CarColors.TextPrimary,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
        if (corner != null) {
            Box(Modifier.align(Alignment.TopEnd)) { corner() }
        }
    }
}

@Composable
fun HaTile(id: String, entity: HaEntity?, onClick: () -> Unit) {
    val active = entity?.isActive == true
    val clickable = entity?.action != null
    Column(
        modifier = Modifier
            .width(190.dp)
            .height(110.dp)
            .clip(TileShape)
            .background(if (active) CarColors.Accent.copy(alpha = 0.18f) else CarColors.Surface)
            .border(
                BorderStroke(2.dp, if (active) CarColors.Accent else CarColors.SurfaceHigh),
                TileShape,
            )
            .clickable(enabled = clickable, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = entity?.name ?: id,
            color = CarColors.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = entity?.displayState ?: "…",
            color = if (active) CarColors.Accent else CarColors.TextSecondary,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun BigButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(TileShape)
            .background(CarColors.SurfaceHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = CarColors.Accent, modifier = Modifier.size(30.dp))
        Spacer(Modifier.width(16.dp))
        Text(text, color = CarColors.TextPrimary, fontSize = 20.sp)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = CarColors.TextSecondary,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 2.sp,
        modifier = modifier.padding(bottom = 12.dp),
    )
}

@Composable
fun Hint(text: String, modifier: Modifier = Modifier) {
    Text(text = text, color = CarColors.TextSecondary, fontSize = 16.sp, modifier = modifier)
}

@Composable
fun TopBar(title: String, onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(64.dp)) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Terug",
                tint = CarColors.TextPrimary,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(title, color = CarColors.TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Medium)
    }
}
