package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Event
import com.example.data.model.EventType
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogEventBottomSheet(
    eventType: EventType,
    babyId: Long,
    onDismiss: () -> Unit,
    onSaveEvent: (Event) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Registrar ${eventType.displayName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (eventType) {
                EventType.BREASTFEEDING -> BreastfeedingLogContent(babyId, onDismiss, onSaveEvent)
                EventType.BOTTLE -> BottleLogContent(babyId, onDismiss, onSaveEvent)
                EventType.DIAPER -> DiaperLogContent(babyId, onDismiss, onSaveEvent)
                EventType.SLEEP -> SleepLogContent(babyId, onDismiss, onSaveEvent)
                EventType.MEDICINE -> MedicineLogContent(babyId, onDismiss, onSaveEvent)
                EventType.TEMPERATURE -> TemperatureLogContent(babyId, onDismiss, onSaveEvent)
                EventType.GROWTH -> GrowthLogContent(babyId, onDismiss, onSaveEvent)
                else -> GenericLogContent(eventType, babyId, onDismiss, onSaveEvent)
            }
        }
    }
}

// --- 1. Amamentação com Cronômetro e Seio E/D ---
@Composable
private fun BreastfeedingLogContent(
    babyId: Long,
    onDismiss: () -> Unit,
    onSaveEvent: (Event) -> Unit
) {
    var selectedSide by remember { mutableStateOf("LEFT") } // "LEFT", "RIGHT", "BOTH"
    var leftSeconds by remember { mutableIntStateOf(600) } // Default 10 min
    var rightSeconds by remember { mutableIntStateOf(300) } // Default 5 min
    var notes by remember { mutableStateOf("") }
    var caretaker by remember { mutableStateOf("Mamãe") }

    Column {
        Text("Lado de início:", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(6.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = selectedSide == "LEFT",
                onClick = { selectedSide = "LEFT" },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
            ) {
                Text("Esquerdo (E)")
            }
            SegmentedButton(
                selected = selectedSide == "RIGHT",
                onClick = { selectedSide = "RIGHT" },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
            ) {
                Text("Direito (D)")
            }
            SegmentedButton(
                selected = selectedSide == "BOTH",
                onClick = { selectedSide = "BOTH" },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
            ) {
                Text("Ambos")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Duração no Seio Esquerdo: ${leftSeconds / 60} min", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = leftSeconds.toFloat(),
            onValueChange = { leftSeconds = it.toInt() },
            valueRange = 0f..2700f,
            steps = 44
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Duração no Seio Direito: ${rightSeconds / 60} min", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = rightSeconds.toFloat(),
            onValueChange = { rightSeconds = it.toInt() },
            valueRange = 0f..2700f,
            steps = 44
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Observação / Anotação (Opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                onSaveEvent(
                    Event(
                        babyId = babyId,
                        type = EventType.BREASTFEEDING,
                        startTimeMs = System.currentTimeMillis(),
                        side = selectedSide,
                        durationLeftSec = leftSeconds,
                        durationRightSec = rightSeconds,
                        notes = notes.ifBlank { null },
                        createdBy = caretaker
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_save_breastfeeding"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Salvar Mamada")
        }
    }
}

// --- 2. Mamadeira ---
@Composable
private fun BottleLogContent(
    babyId: Long,
    onDismiss: () -> Unit,
    onSaveEvent: (Event) -> Unit
) {
    var volumeMl by remember { mutableIntStateOf(120) }
    var bottleType by remember { mutableStateOf("FORMULA") } // "FORMULA", "EXPRESSED_MILK"
    var notes by remember { mutableStateOf("") }

    Column {
        Text("Volume oferecido: $volumeMl ml", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Slider(
            value = volumeMl.toFloat(),
            onValueChange = { volumeMl = it.toInt() },
            valueRange = 10f..300f,
            steps = 28
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Tipo de leite:", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = bottleType == "FORMULA",
                onClick = { bottleType = "FORMULA" },
                label = { Text("Fórmula Infantil") }
            )
            FilterChip(
                selected = bottleType == "EXPRESSED_MILK",
                onClick = { bottleType = "EXPRESSED_MILK" },
                label = { Text("Leite Materno Ordenhado") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Observação (Ex.: tomou tudo com facilidade)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                onSaveEvent(
                    Event(
                        babyId = babyId,
                        type = EventType.BOTTLE,
                        startTimeMs = System.currentTimeMillis(),
                        volumeMl = volumeMl,
                        bottleType = bottleType,
                        notes = notes.ifBlank { null }
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_save_bottle"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Salvar Mamadeira")
        }
    }
}

// --- 3. Fralda ---
@Composable
private fun DiaperLogContent(
    babyId: Long,
    onDismiss: () -> Unit,
    onSaveEvent: (Event) -> Unit
) {
    var diaperType by remember { mutableStateOf("PEE") } // "PEE", "POOP", "BOTH", "CLEAN"
    var diaperColor by remember { mutableStateOf("Amarelo") }
    var notes by remember { mutableStateOf("") }

    Column {
        Text("Conteúdo da fralda:", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = diaperType == "PEE", onClick = { diaperType = "PEE" }, label = { Text("💧 Xixi") })
            FilterChip(selected = diaperType == "POOP", onClick = { diaperType = "POOP" }, label = { Text("💩 Cocô") })
            FilterChip(selected = diaperType == "BOTH", onClick = { diaperType = "BOTH" }, label = { Text("💧+💩 Ambos") })
            FilterChip(selected = diaperType == "CLEAN", onClick = { diaperType = "CLEAN" }, label = { Text("✨ Limpa") })
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (diaperType == "POOP" || diaperType == "BOTH") {
            Text("Cor do cocô:", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Amarelo", "Esverdeado", "Castanho", "Outro").forEach { col ->
                    FilterChip(
                        selected = diaperColor == col,
                        onClick = { diaperColor = col },
                        label = { Text(col) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Observação (Ex.: consistência amolecida)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                onSaveEvent(
                    Event(
                        babyId = babyId,
                        type = EventType.DIAPER,
                        startTimeMs = System.currentTimeMillis(),
                        diaperType = diaperType,
                        diaperColor = if (diaperType == "POOP" || diaperType == "BOTH") diaperColor else null,
                        notes = notes.ifBlank { null }
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_save_diaper"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Salvar Fralda")
        }
    }
}

// --- 4. Sono ---
@Composable
private fun SleepLogContent(
    babyId: Long,
    onDismiss: () -> Unit,
    onSaveEvent: (Event) -> Unit
) {
    var sleepMinutes by remember { mutableIntStateOf(60) }
    var quality by remember { mutableStateOf("CALM") }
    var notes by remember { mutableStateOf("") }

    Column {
        Text("Duração da soneca: ${sleepMinutes / 60}h ${sleepMinutes % 60}min", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Slider(
            value = sleepMinutes.toFloat(),
            onValueChange = { sleepMinutes = it.toInt() },
            valueRange = 15f..360f,
            steps = 22
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Qualidade do sono:", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = quality == "CALM", onClick = { quality = "CALM" }, label = { Text("😴 Tranquilo") })
            FilterChip(selected = quality == "RESTLESS", onClick = { quality = "RESTLESS" }, label = { Text("🫨 Agitado") })
            FilterChip(selected = quality == "CRYING", onClick = { quality = "CRYING" }, label = { Text("😭 Despertou chorando") })
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Observação (Ex.: adormeceu com ruído branco)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val now = System.currentTimeMillis()
                val start = now - (sleepMinutes * 60 * 1000L)
                onSaveEvent(
                    Event(
                        babyId = babyId,
                        type = EventType.SLEEP,
                        startTimeMs = start,
                        endTimeMs = now,
                        sleepQuality = quality,
                        notes = notes.ifBlank { null }
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_save_sleep"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Salvar Registro de Sono")
        }
    }
}

// --- 5. Medicamento ---
@Composable
private fun MedicineLogContent(
    babyId: Long,
    onDismiss: () -> Unit,
    onSaveEvent: (Event) -> Unit
) {
    var medName by remember { mutableStateOf("Paracetamol em gotas") }
    var dosage by remember { mutableStateOf("4 gotas") }
    var notes by remember { mutableStateOf("") }

    Column {
        OutlinedTextField(
            value = medName,
            onValueChange = { medName = it },
            label = { Text("Nome do medicamento") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = dosage,
            onValueChange = { dosage = it },
            label = { Text("Dosagem (Ex.: 4 gotas, 2.5 ml)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Observação / Motivo (Ex.: febre leve)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                onSaveEvent(
                    Event(
                        babyId = babyId,
                        type = EventType.MEDICINE,
                        startTimeMs = System.currentTimeMillis(),
                        medicineName = medName,
                        dosage = dosage,
                        notes = notes.ifBlank { null }
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Salvar Medicamento")
        }
    }
}

// --- 6. Temperatura ---
@Composable
private fun TemperatureLogContent(
    babyId: Long,
    onDismiss: () -> Unit,
    onSaveEvent: (Event) -> Unit
) {
    var tempVal by remember { mutableStateOf("36.8") }
    var notes by remember { mutableStateOf("") }

    Column {
        OutlinedTextField(
            value = tempVal,
            onValueChange = { tempVal = it },
            label = { Text("Temperatura (°C)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Observação") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val dVal = tempVal.replace(",", ".").toDoubleOrNull() ?: 36.8
                onSaveEvent(
                    Event(
                        babyId = babyId,
                        type = EventType.TEMPERATURE,
                        startTimeMs = System.currentTimeMillis(),
                        temperatureCelsius = dVal,
                        notes = notes.ifBlank { null }
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Salvar Temperatura")
        }
    }
}

// --- 7. Crescimento (Peso / Altura / Perímetro Cefálico) ---
@Composable
private fun GrowthLogContent(
    babyId: Long,
    onDismiss: () -> Unit,
    onSaveEvent: (Event) -> Unit
) {
    var weightStr by remember { mutableStateOf("6.4") }
    var heightStr by remember { mutableStateOf("62.5") }
    var headStr by remember { mutableStateOf("41.0") }
    var notes by remember { mutableStateOf("") }

    Column {
        OutlinedTextField(
            value = weightStr,
            onValueChange = { weightStr = it },
            label = { Text("Peso (kg)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = heightStr,
            onValueChange = { heightStr = it },
            label = { Text("Comprimento / Altura (cm)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = headStr,
            onValueChange = { headStr = it },
            label = { Text("Perímetro Cefálico (cm)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Observação (Ex.: Consulta de 3 meses)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                onSaveEvent(
                    Event(
                        babyId = babyId,
                        type = EventType.GROWTH,
                        startTimeMs = System.currentTimeMillis(),
                        weightKg = weightStr.replace(",", ".").toDoubleOrNull(),
                        heightCm = heightStr.replace(",", ".").toDoubleOrNull(),
                        headCircumferenceCm = headStr.replace(",", ".").toDoubleOrNull(),
                        notes = notes.ifBlank { null }
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Salvar Crescimento")
        }
    }
}

// --- 8. Conteúdo Genérico ---
@Composable
private fun GenericLogContent(
    eventType: EventType,
    babyId: Long,
    onDismiss: () -> Unit,
    onSaveEvent: (Event) -> Unit
) {
    var notes by remember { mutableStateOf("") }

    Column {
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Descreva o evento de ${eventType.displayName}") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                onSaveEvent(
                    Event(
                        babyId = babyId,
                        type = eventType,
                        startTimeMs = System.currentTimeMillis(),
                        notes = notes.ifBlank { "Registrado" }
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Salvar")
        }
    }
}
