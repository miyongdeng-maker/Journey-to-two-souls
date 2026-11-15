package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.util.DateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.sin

@Composable
fun SunsetBackdrop(
    darkMode: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val gradientColors = if (darkMode) {
        listOf(
            Color(0xFF1A0F14),
            Color(0xFF28161E),
            Color(0xFF331B22)
        )
    } else {
        listOf(
            Color(0xFFFFF8F2),
            Color(0xFFFDE6DF),
            Color(0xFFFCE2CE)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        content()
    }
}

@Composable
fun FloatingHeartsCanvas(
    modifier: Modifier = Modifier,
    heartColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
) {
    val transition = rememberInfiniteTransition(label = "floating_hearts")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "heart_phase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val seeds = listOf(
            Triple(0.12f, 0.20f, 16f),
            Triple(0.84f, 0.15f, 12f),
            Triple(0.24f, 0.55f, 14f),
            Triple(0.76f, 0.45f, 18f),
            Triple(0.16f, 0.85f, 11f),
            Triple(0.88f, 0.78f, 15f)
        )
        seeds.forEachIndexed { idx, (xFrac, yBase, radius) ->
            val yOffset = ((yBase - phase + idx * 0.17f) % 1f + 1f) % 1f
            val xDrift = sin((phase * 6.283f) + idx) * 14f
            val cx = size.width * xFrac + xDrift
            val cy = size.height * yOffset
            drawMiniHeart(Offset(cx, cy), radius, heartColor)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMiniHeart(
    center: Offset,
    size: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y + size * 0.35f)
        cubicTo(
            center.x - size * 0.9f, center.y - size * 0.25f,
            center.x - size * 0.45f, center.y - size * 0.95f,
            center.x, center.y - size * 0.35f
        )
        cubicTo(
            center.x + size * 0.45f, center.y - size * 0.95f,
            center.x + size * 0.9f, center.y - size * 0.25f,
            center.x, center.y + size * 0.35f
        )
        close()
    }
    drawPath(path, color = color)
}

@Composable
fun IntertwinedHeartsEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val rose = MaterialTheme.colorScheme.primary
    val sunset = MaterialTheme.colorScheme.tertiary
    val gold = MaterialTheme.colorScheme.secondary

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        fun heartPath(cx: Float, cy: Float, scale: Float): Path {
            return Path().apply {
                moveTo(cx, cy + scale * 0.42f)
                cubicTo(
                    cx - scale * 0.95f, cy - scale * 0.15f,
                    cx - scale * 0.55f, cy - scale * 0.95f,
                    cx, cy - scale * 0.35f
                )
                cubicTo(
                    cx + scale * 0.55f, cy - scale * 0.95f,
                    cx + scale * 0.95f, cy - scale * 0.15f,
                    cx, cy + scale * 0.42f
                )
                close()
            }
        }

        drawPath(
            path = heartPath(w * 0.40f, h * 0.50f, w * 0.32f),
            color = rose,
            style = Stroke(width = w * 0.06f, cap = StrokeCap.Round)
        )
        drawPath(
            path = heartPath(w * 0.60f, h * 0.50f, w * 0.32f),
            color = sunset,
            style = Stroke(width = w * 0.06f, cap = StrokeCap.Round)
        )

        // Small golden infinity loop woven across the center
        val inf = Path().apply {
            moveTo(w * 0.50f, h * 0.54f)
            cubicTo(w * 0.36f, h * 0.40f, w * 0.32f, h * 0.68f, w * 0.50f, h * 0.54f)
            cubicTo(w * 0.68f, h * 0.40f, w * 0.64f, h * 0.68f, w * 0.50f, h * 0.54f)
        }
        drawPath(
            path = inf,
            color = gold,
            style = Stroke(width = w * 0.045f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun LocalPhotoImage(
    photoPath: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    fallbackDrawableRes: Int? = null,
    contentScale: ContentScale = ContentScale.Crop
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = photoPath) {
        value = if (photoPath.isNotBlank()) {
            withContext(Dispatchers.IO) {
                val file = File(photoPath)
                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
            }
        } else {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else if (fallbackDrawableRes != null) {
        Image(
            painter = painterResource(id = fallbackDrawableRes),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeepsakeTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back"
                )
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun ItemActionRow(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    extraActions: @Composable () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        extraActions()
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove from favorites" else "Save to favorites",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit item",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete item",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun DeleteConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RomanticDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { millis ->
                    onDateSelected(DateHelper.millisToIso(millis))
                }
                onDismiss()
            }) {
                Text("Save Date")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = state)
    }
}

@Composable
fun EmptyKeepsakeState(
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IntertwinedHeartsEmblem(size = 56.dp)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            OutlinedButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun DailyLoveMessageCard(
    message: String,
    startDateIso: String,
    anniversaryIso: String,
    onEditStartDate: () -> Unit,
    onEditAnniversary: () -> Unit
) {
    val daysTogether = DateHelper.daysSince(startDateIso)
    val daysUntilAnniversary = DateHelper.daysUntilNextAnniversary(anniversaryIso)

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "TODAY'S LOVE NOTE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"$message\"",
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = onEditStartDate,
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "Set start date",
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Days Together",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (daysTogether != null) "$daysTogether days" else "Tap to set date",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (startDateIso.isNotBlank()) {
                            Text(
                                text = "Since ${DateHelper.formatDisplayDate(startDateIso)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Surface(
                    onClick = onEditAnniversary,
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Set anniversary date",
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Anniversary",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (daysUntilAnniversary) {
                                null -> "Tap to set date"
                                0L -> "Happy Anniversary!"
                                1L -> "1 day left"
                                else -> "$daysUntilAnniversary days left"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        if (anniversaryIso.isNotBlank()) {
                            Text(
                                text = DateHelper.formatDisplayDate(anniversaryIso),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
