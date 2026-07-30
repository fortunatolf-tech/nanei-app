package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.PregnantWoman
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ContractionEntry
import com.example.data.model.HospitalBagItem
import com.example.data.model.KickSession
import com.example.data.model.MomJournalEntry
import com.example.data.model.PrenatalExam

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyAndJournalScreen(
    currentWeek: Int,
    kickSessions: List<KickSession>,
    contractions: List<ContractionEntry>,
    hospitalBagItems: List<HospitalBagItem>,
    prenatalExams: List<PrenatalExam>,
    babyName: String,
    journalEntries: List<MomJournalEntry>,
    onAddKickSession: (Int, Long) -> Unit,
    onAddContraction: (Int, Int) -> Unit,
    onToggleBagItem: (String) -> Unit,
    onToggleExam: (String) -> Unit,
    onAddJournalEntry: (MomJournalEntry) -> Unit,
    onDeleteJournalEntry: (MomJournalEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Gestação, 1: Diário & Memórias PDF

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Minha Gestação") },
                icon = { Icon(Icons.Default.PregnantWoman, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_pregnancy_main")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Diário & Memórias") },
                icon = { Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_pregnancy_journal")
            )
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> PregnancyScreen(
                    currentWeek = currentWeek,
                    kickSessions = kickSessions,
                    contractions = contractions,
                    hospitalBagItems = hospitalBagItems,
                    prenatalExams = prenatalExams,
                    onAddKickSession = onAddKickSession,
                    onAddContraction = onAddContraction,
                    onToggleBagItem = onToggleBagItem,
                    onToggleExam = onToggleExam,
                    onNavigateToJournal = { selectedTab = 1 },
                    modifier = Modifier.fillMaxSize()
                )
                1 -> MomJournalScreen(
                    babyName = babyName,
                    journalEntries = journalEntries,
                    onAddEntry = onAddJournalEntry,
                    onDeleteEntry = onDeleteJournalEntry,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
