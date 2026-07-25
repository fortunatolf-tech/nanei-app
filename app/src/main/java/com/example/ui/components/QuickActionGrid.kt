package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventType

@Composable
fun QuickActionGrid(
    onActionClick: (EventType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Registro Rápido (1 Mão, 2 Toques)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionButton(
                label = "Amamentar",
                icon = Icons.Default.ChildCare,
                badgeColor = Color(0xFF818CF8),
                onClick = { onActionClick(EventType.BREASTFEEDING) },
                testTag = "btn_quick_breastfeeding",
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Mamadeira",
                icon = Icons.Default.WaterDrop,
                badgeColor = Color(0xFF38BDF8),
                onClick = { onActionClick(EventType.BOTTLE) },
                testTag = "btn_quick_bottle",
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Fralda",
                icon = Icons.Default.CleanHands,
                badgeColor = Color(0xFFF43F5E),
                onClick = { onActionClick(EventType.DIAPER) },
                testTag = "btn_quick_diaper",
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Sono",
                icon = Icons.Default.Bedtime,
                badgeColor = Color(0xFFA855F7),
                onClick = { onActionClick(EventType.SLEEP) },
                testTag = "btn_quick_sleep",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionButton(
                label = "Medicamento",
                icon = Icons.Default.Medication,
                badgeColor = Color(0xFF10B981),
                onClick = { onActionClick(EventType.MEDICINE) },
                testTag = "btn_quick_medicine",
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Vacina",
                icon = Icons.Default.Vaccines,
                badgeColor = Color(0xFFF59E0B),
                onClick = { onActionClick(EventType.VACCINE) },
                testTag = "btn_quick_vaccine",
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Crescimento",
                icon = Icons.Default.Straighten,
                badgeColor = Color(0xFF06B6D4),
                onClick = { onActionClick(EventType.GROWTH) },
                testTag = "btn_quick_growth",
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Temperatura",
                icon = Icons.Default.Thermostat,
                badgeColor = Color(0xFFEF4444),
                onClick = { onActionClick(EventType.TEMPERATURE) },
                testTag = "btn_quick_temp",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    badgeColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 3.dp)
            .testTag(testTag)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .padding(vertical = 8.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = badgeColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
