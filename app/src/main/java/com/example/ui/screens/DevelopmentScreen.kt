package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.Baby
import com.example.data.model.LeapInfo
import com.example.data.model.Milestone
import com.example.data.provider.NaneiStaticData
import com.example.ui.theme.StormyCloudyColor
import com.example.ui.theme.SunnyYellowColor
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevelopmentScreen(
    baby: Baby?,
    milestones: List<Milestone>,
    onToggleMilestone: (Milestone) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Saltos, 1: Marcos OMS, 2: Atividades
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val ageWeeks = calculateAgeWeeks(baby?.estimatedDueDateMs ?: System.currentTimeMillis())
    val leaps = NaneiStaticData.getMentalLeaps()
    val currentLeap = leaps.find { ageWeeks in it.startWeek..it.endWeek } ?: leaps.first()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Desenvolvimento Infantil",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Top Navigation Tabs
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Saltos Mentais") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Marcos OMS") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Atividades") }
            )
        }

        when (selectedTab) {
            0 -> MentalLeapsTabContent(baby = baby, ageWeeks = ageWeeks, currentLeap = currentLeap, leaps = leaps)
            1 -> MilestonesTabContent(
                milestones = milestones,
                selectedCategory = selectedCategoryFilter,
                onCategorySelect = { selectedCategoryFilter = it },
                onToggleMilestone = onToggleMilestone
            )
            2 -> DailyActivitiesTabContent(ageWeeks = ageWeeks)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// --- TAB 1: Saltos de Desenvolvimento Mental ---
@Composable
private fun MentalLeapsTabContent(
    baby: Baby?,
    ageWeeks: Int,
    currentLeap: LeapInfo,
    leaps: List<LeapInfo>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (currentLeap.isStormyPhase) StormyCloudyColor.copy(alpha = 0.15f) else SunnyYellowColor.copy(alpha = 0.15f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (currentLeap.isStormyPhase) StormyCloudyColor else SunnyYellowColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (currentLeap.isStormyPhase) "🌧️ FASE NUBLADA (Salto em andamento)" else "☀️ FASE ENSOLARADA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = "Semana $ageWeeks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Salto ${currentLeap.leapNumber}: ${currentLeap.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = currentLeap.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "⚡ Sinais de Irritabilidade:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            currentLeap.fussySigns.forEach { sign ->
                Text(text = "• $sign", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "✨ Novas Habilidades Conquistadas:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            currentLeap.newAbilities.forEach { ability ->
                Text(text = "• $ability", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TipsAndUpdates,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Como ajudar: ${currentLeap.howToHelp}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "Todos os 10 Saltos de Desenvolvimento",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )

    leaps.forEach { leap ->
        val isCurrent = leap.leapNumber == currentLeap.leapNumber
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (leap.isStormyPhase) "🌧️" else "☀️",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Salto ${leap.leapNumber} — ${leap.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Semanas ${leap.startWeek} a ${leap.endWeek}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// --- TAB 2: Marcos de Desenvolvimento OMS ---
@Composable
private fun MilestonesTabContent(
    milestones: List<Milestone>,
    selectedCategory: String?,
    onCategorySelect: (String?) -> Unit,
    onToggleMilestone: (Milestone) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelect(null) },
                label = { Text("Todos") }
            )
            FilterChip(
                selected = selectedCategory == "MOTOR_GROSS",
                onClick = { onCategorySelect("MOTOR_GROSS") },
                label = { Text("Motor Grosso") }
            )
            FilterChip(
                selected = selectedCategory == "MOTOR_FINE",
                onClick = { onCategorySelect("MOTOR_FINE") },
                label = { Text("Motor Fino") }
            )
            FilterChip(
                selected = selectedCategory == "COGNITIVE",
                onClick = { onCategorySelect("COGNITIVE") },
                label = { Text("Cognitivo") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val filtered = if (selectedCategory == null) milestones else milestones.filter { it.category == selectedCategory }

        filtered.forEach { ms ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onToggleMilestone(ms) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (ms.isAchieved) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = ms.isAchieved,
                        onCheckedChange = { onToggleMilestone(ms) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ms.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ms.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${ms.targetAgeMonths}m",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- TAB 3: Sugestões Diárias de Atividades ---
@Composable
private fun DailyActivitiesTabContent(ageWeeks: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ActivityCard(
            title = "Brincadeira no Espelho",
            area = "Social e Cognitivo",
            description = "Sente-se com o bebê em frente ao espelho e faça expressões faciais alegres. Ajuda no reconhecimento do próprio corpo.",
            icon = Icons.Default.Face
        )
        ActivityCard(
            title = "Tummy Time no Rolo de Toalha",
            area = "Motor Grosso",
            description = "Coloque uma toalhinha enrolada sob o peito do bebê para incentivar a sustentação do pescoço e dos braços de forma confortável.",
            icon = Icons.Default.FitnessCenter
        )
        ActivityCard(
            title = "Cantando com Gestos",
            area = "Linguagem e Audição",
            description = "Cante 'Dona Aranha' ou 'A Barata Diz Que Tem' fazendo movimentos lentos com os dedos para estimular o acompanhamento visual.",
            icon = Icons.Default.MusicNote
        )
    }
}

@Composable
private fun ActivityCard(
    title: String,
    area: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = area,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun calculateAgeWeeks(estimatedDueDateMs: Long): Int {
    val diff = System.currentTimeMillis() - estimatedDueDateMs
    val weeks = (diff / (1000 * 60 * 60 * 24 * 7)).toInt()
    return weeks.coerceIn(1, 100)
}
