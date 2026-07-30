package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Baby
import com.example.data.model.DrugInfo
import com.example.data.model.Event
import com.example.data.model.Milestone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    baby: Baby?,
    events: List<Event>,
    milestones: List<Milestone>,
    searchQuery: String,
    filteredDrugs: List<DrugInfo>,
    onSearchQueryChange: (String) -> Unit,
    onToggleMilestone: (Milestone) -> Unit,
    isPremiumUser: Boolean,
    onOpenPaywall: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedHealthTab by remember { mutableStateOf(0) } // 0: Análises, 1: Marcos, 2: Fármacos

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Unified Health Top Tab Bar
        TabRow(
            selectedTabIndex = selectedHealthTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedHealthTab == 0,
                onClick = { selectedHealthTab = 0 },
                text = { Text("Análises & Sono") },
                icon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_health_analytics")
            )
            Tab(
                selected = selectedHealthTab == 1,
                onClick = { selectedHealthTab = 1 },
                text = { Text("Marcos") },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_health_milestones")
            )
            Tab(
                selected = selectedHealthTab == 2,
                onClick = { selectedHealthTab = 2 },
                text = { Text("Fármacos") },
                icon = { Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_health_medication")
            )
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        Box(modifier = Modifier.weight(1f)) {
            when (selectedHealthTab) {
                0 -> AnalyticsScreen(
                    baby = baby,
                    events = events,
                    isPremiumUser = isPremiumUser,
                    onOpenPaywall = onOpenPaywall,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> DevelopmentScreen(
                    baby = baby,
                    milestones = milestones,
                    onToggleMilestone = onToggleMilestone,
                    modifier = Modifier.fillMaxSize()
                )
                2 -> MedicationScreen(
                    searchQuery = searchQuery,
                    filteredDrugs = filteredDrugs,
                    onSearchQueryChange = onSearchQueryChange,
                    isPremiumUser = isPremiumUser,
                    onOpenPaywall = onOpenPaywall,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
