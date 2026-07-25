package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Event
import com.example.data.model.EventType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventItemCard(
    event: Event,
    onDeleteClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(event.startTimeMs))

    val (badgeColor, icon) = when (event.type) {
        EventType.BREASTFEEDING -> Pair(Color(0xFF818CF8), Icons.Default.ChildCare)
        EventType.BOTTLE -> Pair(Color(0xFF38BDF8), Icons.Default.WaterDrop)
        EventType.SOLIDS -> Pair(Color(0xFFF59E0B), Icons.Default.Restaurant)
        EventType.PUMPING -> Pair(Color(0xFF06B6D4), Icons.Default.InvertColors)
        EventType.DIAPER -> Pair(Color(0xFFF43F5E), Icons.Default.CleanHands)
        EventType.SLEEP -> Pair(Color(0xFFA855F7), Icons.Default.Bedtime)
        EventType.BATH -> Pair(Color(0xFF0EA5E9), Icons.Default.Bathtub)
        EventType.MEDICINE -> Pair(Color(0xFF10B981), Icons.Default.Medication)
        EventType.VACCINE -> Pair(Color(0xFFD97706), Icons.Default.Vaccines)
        EventType.TEMPERATURE -> Pair(Color(0xFFEF4444), Icons.Default.Thermostat)
        EventType.GROWTH -> Pair(Color(0xFF14B8A6), Icons.Default.Straighten)
        EventType.MOOD -> Pair(Color(0xFFEC4899), Icons.Default.Mood)
        EventType.NOTE -> Pair(Color(0xFF64748B), Icons.Default.EditNote)
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("event_item_${event.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Badge
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(48.dp)
            )

            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = event.type.displayName,
                    tint = badgeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = event.type.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Caretaker badge
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = event.createdBy,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                val detailSummary = buildEventSummaryText(event)
                if (detailSummary.isNotBlank()) {
                    Text(
                        text = detailSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!event.notes.isNullGlanceable()) {
                    Text(
                        text = "📝 ${event.notes}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Excluir evento",
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir evento?") },
            text = { Text("Deseja remover este registro da linha do tempo do bebê?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(event)
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun String?.isNullGlanceable(): Boolean = this.isNullOrBlank()

private fun buildEventSummaryText(event: Event): String {
    return when (event.type) {
        EventType.BREASTFEEDING -> {
            val sideStr = when (event.side) {
                "LEFT" -> "Seio Esquerdo"
                "RIGHT" -> "Seio Direito"
                else -> "Ambos os Seios"
            }
            val minLeft = event.durationLeftSec / 60
            val minRight = event.durationRightSec / 60
            val totalMin = minLeft + minRight
            "$sideStr • ${if (totalMin > 0) "$totalMin min" else "Iniciado"}"
        }
        EventType.BOTTLE -> {
            val vol = event.volumeMl?.let { "$it ml" } ?: ""
            val type = when (event.bottleType) {
                "FORMULA" -> "Fórmula infantil"
                "EXPRESSED_MILK" -> "Leite materno ordenhado"
                else -> "Leite"
            }
            "$vol ($type)".trim()
        }
        EventType.DIAPER -> {
            val dType = when (event.diaperType) {
                "PEE" -> "Xixi"
                "POOP" -> "Cocô"
                "BOTH" -> "Xixi + Cocô"
                "CLEAN" -> "Limpa"
                else -> "Fralda"
            }
            val color = event.diaperColor?.let { " • Cor: $it" } ?: ""
            "$dType$color"
        }
        EventType.SLEEP -> {
            if (event.endTimeMs != null) {
                val durMinutes = ((event.endTimeMs - event.startTimeMs) / 60000).toInt()
                val hrs = durMinutes / 60
                val mins = durMinutes % 60
                val durStr = if (hrs > 0) "${hrs}h ${mins}min" else "${mins}min"
                "Duração: $durStr"
            } else {
                "Sono em andamento..."
            }
        }
        EventType.MEDICINE -> {
            val name = event.medicineName ?: "Medicamento"
            val dos = event.dosage?.let { " • Dose: $it" } ?: ""
            "$name$dos"
        }
        EventType.VACCINE -> {
            event.vaccineName ?: "Vacina aplicada"
        }
        EventType.TEMPERATURE -> {
            event.temperatureCelsius?.let { "$it °C" } ?: "Temperatura aferida"
        }
        EventType.GROWTH -> {
            val w = event.weightKg?.let { "Peso: $it kg" } ?: ""
            val h = event.heightCm?.let { "Alt: $it cm" } ?: ""
            val hc = event.headCircumferenceCm?.let { "PC: $it cm" } ?: ""
            listOf(w, h, hc).filter { it.isNotBlank() }.joinToString(" • ")
        }
        else -> ""
    }
}
