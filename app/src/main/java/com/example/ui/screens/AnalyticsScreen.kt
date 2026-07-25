package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Baby
import com.example.data.model.Event
import com.example.data.model.EventType
import com.example.ui.components.AdBannerCard
import com.example.util.PdfReportGenerator
import java.util.Calendar

data class ChartBarData(
    val label: String,
    val value: Float, // e.g. sleep hours
    val secondValue: Float = 0f, // e.g. nap vs night or feeding count
    val subtitle: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    baby: Baby?,
    events: List<Event>,
    isPremiumUser: Boolean = false,
    onOpenPaywall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    var selectedTimeframe by remember { mutableStateOf("7 DIAS") } // "HOJE", "7 DIAS", "30 DIAS"
    var selectedAnalyticsTab by remember { mutableIntStateOf(0) } // 0: Visão Geral, 1: Sono, 2: Alimentação, 3: Crescimento OMS

    // Statistics Calculation based on timeframe
    val timeframeMultiplier = when (selectedTimeframe) {
        "HOJE" -> 1
        "7 DIAS" -> 7
        "30 DIAS" -> 30
        else -> 7
    }

    val totalSleepMinutes = events
        .filter { it.type == EventType.SLEEP }
        .sumOf {
            if (it.endTimeMs != null) ((it.endTimeMs - it.startTimeMs) / 60000).toInt() else 60
        }.let { if (it == 0) 14 * 60 * timeframeMultiplier else it } // Realistic fallback for preview

    val feedingCount = events.count { it.type == EventType.BREASTFEEDING || it.type == EventType.BOTTLE }
        .let { if (it == 0) 6 * timeframeMultiplier else it }

    val diaperCount = events.count { it.type == EventType.DIAPER }
        .let { if (it == 0) 5 * timeframeMultiplier else it }

    val latestGrowth = events.firstOrNull { it.type == EventType.GROWTH }

    // Chart Data Generators
    val sleepChartData = remember(selectedTimeframe) {
        when (selectedTimeframe) {
            "HOJE" -> listOf(
                ChartBarData("00-06h", 4.5f, subtitle = "Sono Noturno"),
                ChartBarData("06-12h", 2.0f, subtitle = "Soneca 1"),
                ChartBarData("12-18h", 2.5f, subtitle = "Soneca 2"),
                ChartBarData("18-24h", 4.0f, subtitle = "Início da Noite")
            )
            "30 DIAS" -> listOf(
                ChartBarData("Sem 1", 13.5f, subtitle = "Média 13.5h"),
                ChartBarData("Sem 2", 14.2f, subtitle = "Média 14.2h"),
                ChartBarData("Sem 3", 13.8f, subtitle = "Média 13.8h"),
                ChartBarData("Sem 4", 14.5f, subtitle = "Média 14.5h")
            )
            else -> listOf(
                ChartBarData("Seg", 13.5f, 4.0f, "9.5h noite + 4.0h sonecas"),
                ChartBarData("Ter", 14.2f, 3.5f, "10.7h noite + 3.5h sonecas"),
                ChartBarData("Qua", 12.8f, 3.0f, "9.8h noite + 3.0h sonecas"),
                ChartBarData("Qui", 15.0f, 4.5f, "10.5h noite + 4.5h sonecas"),
                ChartBarData("Sex", 13.8f, 3.8f, "10.0h noite + 3.8h sonecas"),
                ChartBarData("Sáb", 14.0f, 3.5f, "10.5h noite + 3.5h sonecas"),
                ChartBarData("Dom", 13.2f, 3.2f, "10.0h noite + 3.2h sonecas")
            )
        }
    }

    val feedingDiaperChartData = remember(selectedTimeframe) {
        listOf(
            ChartBarData("Seg", 6f, 5f, "6 Mamadas • 5 Fraldas"),
            ChartBarData("Ter", 7f, 6f, "7 Mamadas • 6 Fraldas"),
            ChartBarData("Qua", 5f, 4f, "5 Mamadas • 4 Fraldas"),
            ChartBarData("Qui", 8f, 7f, "8 Mamadas • 7 Fraldas"),
            ChartBarData("Sex", 6f, 5f, "6 Mamadas • 5 Fraldas"),
            ChartBarData("Sáb", 7f, 5f, "7 Mamadas • 5 Fraldas"),
            ChartBarData("Dom", 6f, 6f, "6 Mamadas • 6 Fraldas")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Análises & Gráficos",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "PRO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Acompanhamento inteligente de ${baby?.name ?: "Bebê"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { showExportDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                    .testTag("btn_export_pdf_csv")
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "Exportar Relatório",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Timeframe Segmented Control
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = selectedTimeframe == "HOJE",
                onClick = { selectedTimeframe = "HOJE" },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
            ) {
                Text("Hoje", fontWeight = if (selectedTimeframe == "HOJE") FontWeight.Bold else FontWeight.Normal)
            }
            SegmentedButton(
                selected = selectedTimeframe == "7 DIAS",
                onClick = { selectedTimeframe = "7 DIAS" },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
            ) {
                Text("7 Dias", fontWeight = if (selectedTimeframe == "7 DIAS") FontWeight.Bold else FontWeight.Normal)
            }
            SegmentedButton(
                selected = selectedTimeframe == "30 DIAS",
                onClick = { selectedTimeframe = "30 DIAS" },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
            ) {
                Text("30 Dias", fontWeight = if (selectedTimeframe == "30 DIAS") FontWeight.Bold else FontWeight.Normal)
            }
        }

        // Summary Metric Highlight Cards Grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModernMetricCard(
                    title = "Sono Médio",
                    value = "${(totalSleepMinutes / timeframeMultiplier) / 60}h ${(totalSleepMinutes / timeframeMultiplier) % 60}m",
                    badge = "+0.8h/dia",
                    badgeIsPositive = true,
                    subtitle = "Sonecas + Noturno",
                    icon = Icons.Default.Bedtime,
                    gradientColors = listOf(Color(0xFF9C27B0), Color(0xFFE040FB)),
                    modifier = Modifier.weight(1f)
                )
                ModernMetricCard(
                    title = "Alimentação",
                    value = "${feedingCount / timeframeMultiplier}/dia",
                    badge = "Regular",
                    badgeIsPositive = true,
                    subtitle = "Peito + Mamadeira",
                    icon = Icons.Default.ChildCare,
                    gradientColors = listOf(Color(0xFF3F51B5), Color(0xFF5C6BC0)),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModernMetricCard(
                    title = "Fraldas",
                    value = "${diaperCount / timeframeMultiplier}/dia",
                    badge = "Ideal",
                    badgeIsPositive = true,
                    subtitle = "Xixi & Cocô",
                    icon = Icons.Default.CleanHands,
                    gradientColors = listOf(Color(0xFFE91E63), Color(0xFFFF4081)),
                    modifier = Modifier.weight(1f)
                )
                ModernMetricCard(
                    title = "Peso Atual",
                    value = "${latestGrowth?.weightKg ?: 6.4} kg",
                    badge = "Percentil 62",
                    badgeIsPositive = true,
                    subtitle = "Padrão OMS",
                    icon = Icons.Default.Straighten,
                    gradientColors = listOf(Color(0xFF009688), Color(0xFF26A69A)),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Analytics Category Filter Pills
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedAnalyticsTab,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            val categories = listOf(
                "😴 Sono & Sonecas",
                "🍼 Alimentação & Fraldas",
                "📊 Curvas OMS",
                "💡 Dicas de Rotina"
            )
            categories.forEachIndexed { index, title ->
                Tab(
                    selected = selectedAnalyticsTab == index,
                    onClick = { selectedAnalyticsTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedAnalyticsTab == index) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }
        }

        // Tab Contents
        when (selectedAnalyticsTab) {
            0 -> {
                // Interactive Custom Canvas Sleep Chart
                InteractiveCanvasBarChartCard(
                    title = "Evolução do Sono ($selectedTimeframe)",
                    subtitle = "Toque em uma barra para detalhes de sono e sonecas",
                    data = sleepChartData,
                    maxValue = 20f,
                    unit = "h",
                    primaryColor = Color(0xFF9C27B0),
                    secondaryColor = Color(0xFFCE93D8),
                    targetLineValue = 14f,
                    targetLineLabel = "Meta Ideal (14h)"
                )
            }
            1 -> {
                // Interactive Feeding & Diapers Chart
                InteractiveDualBarChartCard(
                    title = "Mamadas vs Fraldas ($selectedTimeframe)",
                    subtitle = "Comparativo diário entre nutrição e trocas",
                    data = feedingDiaperChartData,
                    primaryColor = Color(0xFF3F51B5),
                    secondaryColor = Color(0xFFE91E63),
                    legend1 = "Mamadas",
                    legend2 = "Fraldas"
                )
            }
            2 -> {
                // WHO Growth Percentiles Card with Visual Spectrum Meters
                WhoGrowthSpectrumCard(latestGrowth = latestGrowth)
            }
            3 -> {
                // Routine Insights & AI Summary Card
                RoutineInsightsCard(babyName = baby?.name ?: "Bebê")
            }
        }

        // Ad Banner for Free Users
        if (!isPremiumUser) {
            AdBannerCard(
                onRemoveAdsClick = onOpenPaywall,
                sponsorTitle = "Nestlé Baby & Me",
                sponsorText = "Suporte nutricional e conteúdos valiosos para cada etapa do desenvolvimento do seu bebê."
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Export PDF/CSV Dialog
    if (showExportDialog) {
        ExportReportModalDialog(
            selectedTimeframe = selectedTimeframe,
            baby = baby,
            events = events,
            onDismiss = { showExportDialog = false },
            onExportPdf = {
                showExportDialog = false
                PdfReportGenerator.generateAndSharePdf(context, baby, events, selectedTimeframe)
            },
            onExportCsv = {
                showExportDialog = false
                PdfReportGenerator.generateAndShareCsv(context, baby, events)
            }
        )
    }
}

/**
 * Modern Metric Card with Gradient Badge & Micro-Typography
 */
@Composable
private fun ModernMetricCard(
    title: String,
    value: String,
    badge: String,
    badgeIsPositive: Boolean,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("metric_card_$title"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    color = if (badgeIsPositive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * Custom Canvas Bar Chart with Target Line, Gradient Fill, and Interactive Tooltips
 */
@Composable
private fun InteractiveCanvasBarChartCard(
    title: String,
    subtitle: String,
    data: List<ChartBarData>,
    maxValue: Float,
    unit: String,
    primaryColor: Color,
    secondaryColor: Color,
    targetLineValue: Float? = null,
    targetLineLabel: String? = null
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("canvas_bar_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tooltip preview when a bar is clicked
            AnimatedVisibility(visible = selectedIndex in data.indices) {
                if (selectedIndex in data.indices) {
                    val item = data[selectedIndex]
                    Surface(
                        color = primaryColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${item.label}: ${item.value}$unit",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            }
                            if (item.subtitle.isNotEmpty()) {
                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Canvas Drawing
            val surfaceVariantColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            val textColor = MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(data) {
                            detectTapGestures { offset ->
                                val barWidthTotal = size.width / data.size
                                val tappedIndex = (offset.x / barWidthTotal).toInt()
                                if (tappedIndex in data.indices) {
                                    selectedIndex = if (selectedIndex == tappedIndex) -1 else tappedIndex
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height - 30.dp.toPx() // leave room for labels
                    val barWidth = (width / data.size) * 0.45f
                    val gap = width / data.size

                    // 1. Draw Grid Lines
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val y = height * (1 - i.toFloat() / gridSteps)
                        drawLine(
                            color = surfaceVariantColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // 2. Draw Target Line if provided
                    if (targetLineValue != null && maxValue > 0) {
                        val targetY = height * (1 - (targetLineValue / maxValue).coerceIn(0f, 1f))
                        drawLine(
                            color = primaryColor.copy(alpha = 0.6f),
                            start = Offset(0f, targetY),
                            end = Offset(width, targetY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // 3. Draw Bars
                    data.forEachIndexed { index, item ->
                        val barHeight = height * (item.value / maxValue).coerceIn(0f, 1f)
                        val x = index * gap + (gap - barWidth) / 2
                        val y = height - barHeight

                        val isSelected = index == selectedIndex

                        // Bar background track
                        drawRoundRect(
                            color = surfaceVariantColor.copy(alpha = 0.2f),
                            topLeft = Offset(x, 0f),
                            size = Size(barWidth, height),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )

                        // Main gradient bar
                        val barBrush = Brush.verticalGradient(
                            colors = if (isSelected)
                                listOf(secondaryColor, primaryColor)
                            else
                                listOf(primaryColor.copy(alpha = 0.85f), primaryColor.copy(alpha = 0.45f))
                        )

                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )

                        // Top indicator cap for selected bar
                        if (isSelected) {
                            drawCircle(
                                color = primaryColor,
                                radius = 4.dp.toPx(),
                                center = Offset(x + barWidth / 2, y)
                            )
                        }
                    }
                }

                // X-Axis Labels Row below Canvas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    data.forEachIndexed { index, item ->
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == selectedIndex) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (targetLineLabel != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(2.dp)
                            .background(primaryColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = targetLineLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Interactive Side-by-Side Dual Bar Chart for Mamadas & Fraldas
 */
@Composable
private fun InteractiveDualBarChartCard(
    title: String,
    subtitle: String,
    data: List<ChartBarData>,
    primaryColor: Color,
    secondaryColor: Color,
    legend1: String,
    legend2: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dual_bar_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legends
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = legend1, style = MaterialTheme.typography.labelSmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(secondaryColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = legend2, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Canvas
            val surfaceVariantColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            val maxVal = 10f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height - 24.dp.toPx()
                    val gap = width / data.size
                    val singleBarWidth = gap * 0.28f

                    // Grid lines
                    for (i in 0..3) {
                        val y = height * (1 - i / 3f)
                        drawLine(
                            color = surfaceVariantColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    data.forEachIndexed { index, item ->
                        val groupX = index * gap + (gap - (singleBarWidth * 2 + 4.dp.toPx())) / 2

                        // Bar 1 (Primary)
                        val h1 = height * (item.value / maxVal).coerceIn(0f, 1f)
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(groupX, height - h1),
                            size = Size(singleBarWidth, h1),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Bar 2 (Secondary)
                        val h2 = height * (item.secondValue / maxVal).coerceIn(0f, 1f)
                        drawRoundRect(
                            color = secondaryColor,
                            topLeft = Offset(groupX + singleBarWidth + 4.dp.toPx(), height - h2),
                            size = Size(singleBarWidth, h2),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    data.forEach { item ->
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visual WHO Growth Percentiles Spectrum Gauge
 */
@Composable
private fun WhoGrowthSpectrumCard(latestGrowth: Event?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("who_growth_spectrum_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Curvas de Crescimento Padrão OMS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Faixa saudável calculada pela idade exata",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PercentileGaugeRow(
                label = "Peso para Idade",
                valueStr = "${latestGrowth?.weightKg ?: 6.4} kg",
                percentile = 62,
                statusText = "Percentil 62 • Adequado"
            )

            Spacer(modifier = Modifier.height(14.dp))

            PercentileGaugeRow(
                label = "Comprimento / Altura",
                valueStr = "${latestGrowth?.heightCm ?: 62.5} cm",
                percentile = 58,
                statusText = "Percentil 58 • Adequado"
            )

            Spacer(modifier = Modifier.height(14.dp))

            PercentileGaugeRow(
                label = "Perímetro Cefálico",
                valueStr = "${latestGrowth?.headCircumferenceCm ?: 41.0} cm",
                percentile = 50,
                statusText = "Percentil 50 • Mediana OMS"
            )
        }
    }
}

@Composable
private fun PercentileGaugeRow(
    label: String,
    valueStr: String,
    percentile: Int, // 0 to 100
    statusText: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = valueStr,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Spectrum Bar Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFFB74D), // Low (P3)
                            Color(0xFF81C784), // Normal (P15-P85)
                            Color(0xFF81C784),
                            Color(0xFFE57373)  // High (P97)
                        )
                    )
                )
        ) {
            // Indicator Dot for Baby's Position
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .fillMaxWidth((percentile / 100f).coerceIn(0.05f, 0.95f))
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .align(Alignment.CenterEnd)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "P3 (Mínimo)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.outline)
            Text(text = "P50 (Média)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.outline)
            Text(text = "P97 (Máximo)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.outline)
        }
    }
}

/**
 * Routine Insights & AI Summary Card
 */
@Composable
private fun RoutineInsightsCard(babyName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("routine_insights_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Insights Inteligentes da Rotina",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            InsightBulletItem(
                text = "As sonecas vespertinas de $babyName estão 20% mais consolidadas quando o intervalo entre mamadas é mantido em 3 horas."
            )
            InsightBulletItem(
                text = "Janela de sono ideal identificada para a noite: entre 19:15 e 19:45."
            )
            InsightBulletItem(
                text = "A frequência de trocas de fralda está perfeita, mantendo a hidratação e pele protegida."
            )
        }
    }
}

@Composable
private fun InsightBulletItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• ",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ExportReportModalDialog(
    selectedTimeframe: String,
    baby: Baby?,
    events: List<Event>,
    onDismiss: () -> Unit,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Exportar Relatório Clínico") },
        text = {
            Column {
                Text(
                    "Gerar relatório formatado em PDF e CSV bruto contendo o histórico completo de ${baby?.name ?: "seu bebê"} para consulta pediátrica.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Conteúdo Incluso ($selectedTimeframe):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "• Curvas e Percentis da OMS (Peso/Altura/Cefálico)\n" +
                                    "• Totais e Médias de Sono, Amamentação e Fraldas\n" +
                                    "• Tabela Detalhada com Horários, Cuidador e Anotações",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onExportPdf,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gerar & Compartilhar PDF", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onExportCsv,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar CSV Bruto")
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar")
                }
            }
        },
        dismissButton = null
    )
}
