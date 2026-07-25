package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContractionEntry
import com.example.data.model.HospitalBagItem
import com.example.data.model.KickSession
import com.example.data.model.PrenatalExam
import com.example.data.provider.NaneiStaticData
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyScreen(
    currentWeek: Int = 24,
    kickSessions: List<KickSession>,
    contractions: List<ContractionEntry>,
    hospitalBagItems: List<HospitalBagItem>,
    prenatalExams: List<PrenatalExam>,
    onAddKickSession: (count: Int, durationSec: Long) -> Unit,
    onAddContraction: (durationSec: Int, intervalSec: Int) -> Unit,
    onToggleBagItem: (String) -> Unit,
    onToggleExam: (String) -> Unit,
    onNavigateToJournal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Guia Semanal, 1: Mexidas, 2: Contrações, 3: Mala, 4: Exames
    var activeWeek by remember { mutableIntStateOf(currentWeek) }

    val weeksData = remember { NaneiStaticData.getPregnancyWeeksData() }
    val currentWeekInfo = weeksData[activeWeek] ?: weeksData[24]!!

    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale("pt", "BR")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PregnantWoman,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Módulo de Gravidez 🤰",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Acompanhamento da Gestação & Parto",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToJournal,
                        modifier = Modifier.testTag("btn_pregnancy_to_journal")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "Diário da Mamãe",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("pregnancy_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Hero Week Overview Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${activeWeek}ª Semana de Gestação",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Tamanho equivalente: ${currentWeekInfo.fruitComparison} ${currentWeekInfo.fruitEmoji}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = currentWeekInfo.fruitEmoji,
                            fontSize = 38.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { activeWeek / 40f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Aprox. ${currentWeekInfo.babySizeCm} cm • ${currentWeekInfo.babyWeightGrams.toInt()} g",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Faltam ${(40 - activeWeek)} semanas",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Scrollable Sub-Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Semana a Semana") },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Mexidas (${kickSessions.size})") },
                    icon = { Icon(Icons.Default.TouchApp, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Contrações (${contractions.size})") },
                    icon = { Icon(Icons.Default.Timer, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Mala Maternidade") },
                    icon = { Icon(Icons.Default.Work, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("Exames Pré-Natal") },
                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            when (selectedTab) {
                0 -> PregnancyWeeklyTab(
                    activeWeek = activeWeek,
                    weeksData = weeksData,
                    onSelectWeek = { activeWeek = it }
                )
                1 -> KickCounterTab(
                    kickSessions = kickSessions,
                    onSaveSession = onAddKickSession
                )
                2 -> ContractionTimerTab(
                    contractions = contractions,
                    onAddContraction = onAddContraction
                )
                3 -> HospitalBagTab(
                    items = hospitalBagItems,
                    onToggleItem = onToggleBagItem
                )
                4 -> PrenatalExamsTab(
                    exams = prenatalExams,
                    onToggleExam = onToggleExam
                )
            }
        }
    }
}

@Composable
private fun PregnancyWeeklyTab(
    activeWeek: Int,
    weeksData: Map<Int, com.example.data.model.PregnancyWeekInfo>,
    onSelectWeek: (Int) -> Unit
) {
    val currentInfo = weeksData[activeWeek] ?: weeksData[24]!!

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "Selecione a semana para explorar:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val keyWeeks = listOf(4, 8, 12, 16, 20, 24, 28, 32, 36, 40)
                keyWeeks.forEach { week ->
                    FilterChip(
                        selected = activeWeek == week,
                        onClick = { onSelectWeek(week) },
                        label = { Text("${week}w") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ChildCare, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Desenvolvimento do Bebê na ${activeWeek}ª Semana",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentInfo.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFE53935))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "O que a Mamãe pode sentir:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentInfo.momSymptoms,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun KickCounterTab(
    kickSessions: List<KickSession>,
    onSaveSession: (count: Int, durationSec: Long) -> Unit
) {
    var kickCount by remember { mutableIntStateOf(0) }
    var startTimeMs by remember { mutableLongStateOf(0L) }
    var isTimerRunning by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Contador de Mexidas e Chutes 🦶",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = "Recomendação médica: registre 10 mexidas em até 2 horas.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "$kickCount",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Mexidas Registradas",
                        style = MaterialTheme.typography.labelMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (!isTimerRunning) {
                                isTimerRunning = true
                                startTimeMs = System.currentTimeMillis()
                            }
                            kickCount++
                        },
                        modifier = Modifier
                            .size(110.dp)
                            .testTag("btn_tap_kick"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "TOQUE!\n🦶",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (kickCount > 0) {
                        Button(
                            onClick = {
                                val duration = (System.currentTimeMillis() - startTimeMs) / 1000
                                onSaveSession(kickCount, duration)
                                kickCount = 0
                                isTimerRunning = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Finalizar e Salvar Sessão")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Histórico de Sessões Recentes:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (kickSessions.isEmpty()) {
            item {
                Text(
                    text = "Nenhuma sessão de mexidas registrada ainda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            items(kickSessions) { session ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${session.kickCount} Mexidas registradas 🦶",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Duração: ${session.durationSeconds / 60} min ${session.durationSeconds % 60}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(session.timestampMs)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContractionTimerTab(
    contractions: List<ContractionEntry>,
    onAddContraction: (durationSec: Int, intervalSec: Int) -> Unit
) {
    var isContracting by remember { mutableStateOf(false) }
    var startTimeMs by remember { mutableLongStateOf(0L) }
    var lastContractionEndMs by remember { mutableLongStateOf(0L) }

    val has511Warning = remember(contractions) {
        if (contractions.size < 3) false else {
            val recent = contractions.take(3)
            recent.all { it.durationSeconds >= 45 && it.intervalSeconds in 240..360 }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            if (has511Warning) {
                Surface(
                    color = Color(0xFFE53935),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ALERTA 5-1-1 ATINGIDO! 🏥",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Contrações a cada 5 min com 1 min de duração. Hora de ligar para o médico ou ir à maternidade!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Cronômetro de Contrações ⏱️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val now = System.currentTimeMillis()
                            if (!isContracting) {
                                isContracting = true
                                startTimeMs = now
                            } else {
                                isContracting = false
                                val durationSec = ((now - startTimeMs) / 1000).toInt()
                                val intervalSec = if (lastContractionEndMs > 0) ((startTimeMs - lastContractionEndMs) / 1000).toInt() else 300
                                lastContractionEndMs = now
                                onAddContraction(durationSec, intervalSec)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_toggle_contraction"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isContracting) Color(0xFFE53935) else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(if (isContracting) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isContracting) "PARAR CONTRAÇÃO (CONTRAÇÃO ATIVA)" else "INICIAR CONTRAÇÃO",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Histórico de Contrações:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        items(contractions) { c ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Duração: ${c.durationSeconds}s | Intervalo: ${c.intervalSeconds / 60}m ${c.intervalSeconds % 60}s",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(c.timestampMs)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HospitalBagTab(
    items: List<HospitalBagItem>,
    onToggleItem: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Mãe") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "Checklist da Mala da Maternidade 🧳",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Mãe", "Bebê", "Acompanhante", "Documentos").forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        val filtered = items.filter { it.category == selectedCategory }

        items(filtered) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onToggleItem(item.id) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (item.isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = { onToggleItem(item.id) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun PrenatalExamsTab(
    exams: List<PrenatalExam>,
    onToggleExam: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "Agenda de Exames Pré-Natal 🩺",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Acompanhe todos os exames importantes recomendados no pré-natal.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        items(exams) { exam ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onToggleExam(exam.id) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (exam.isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = exam.isCompleted,
                        onCheckedChange = { onToggleExam(exam.id) }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exam.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Recomendado: ${exam.weekRange}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (exam.notes.isNotEmpty()) {
                            Text(
                                text = exam.notes,
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
