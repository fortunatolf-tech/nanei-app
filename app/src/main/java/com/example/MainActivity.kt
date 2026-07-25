package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AuditLog
import com.example.ui.components.FamilyManagementDialog
import com.example.ui.components.LogEventBottomSheet
import com.example.ui.components.NaneiPaywallDialog
import com.example.ui.components.OnboardingModal
import com.example.ui.components.SoundListenModeDialog
import com.example.ui.screens.*
import com.example.ui.theme.NaneiTheme
import com.example.ui.viewmodel.NaneiDestination
import com.example.ui.viewmodel.NaneiViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: NaneiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val selectedBaby by viewModel.selectedBaby.collectAsStateWithLifecycle()
            val allBabies by viewModel.allBabies.collectAsStateWithLifecycle()
            val events by viewModel.activeBabyEvents.collectAsStateWithLifecycle()
            val milestones by viewModel.activeBabyMilestones.collectAsStateWithLifecycle()
            val sweetSpot by viewModel.sweetSpot.collectAsStateWithLifecycle()
            val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
            val drugQuery by viewModel.drugSearchQuery.collectAsStateWithLifecycle()
            val filteredDrugs by viewModel.filteredDrugs.collectAsStateWithLifecycle()
            val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
            val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
            val activeLogDialogType by viewModel.activeLogDialogType.collectAsStateWithLifecycle()
            val isNightModeForce by viewModel.isNightModeForce.collectAsStateWithLifecycle()
            val isPremiumUser by viewModel.isPremiumUser.collectAsStateWithLifecycle()
            val showPaywallDialog by viewModel.showPaywallDialog.collectAsStateWithLifecycle()
            val showOnboarding by viewModel.showOnboarding.collectAsStateWithLifecycle()
            val showFamilyDialog by viewModel.showFamilyDialog.collectAsStateWithLifecycle()
            val showSoundListenDialog by viewModel.showSoundListenDialog.collectAsStateWithLifecycle()

            val gestationalWeek by viewModel.gestationalWeek.collectAsStateWithLifecycle()
            val kickSessions by viewModel.kickSessions.collectAsStateWithLifecycle()
            val contractions by viewModel.contractions.collectAsStateWithLifecycle()
            val hospitalBagItems by viewModel.hospitalBagItems.collectAsStateWithLifecycle()
            val prenatalExams by viewModel.prenatalExams.collectAsStateWithLifecycle()
            val momJournalEntries by viewModel.momJournalEntries.collectAsStateWithLifecycle()

            var showLgpdScreen by remember { mutableStateOf(false) }


            NaneiTheme(forceNightMode = isNightModeForce) {
                if (showLgpdScreen) {
                    LgpdPrivacyScreen(
                        auditLogs = listOf(
                            AuditLog(action = "APP_LAUNCH", details = "Sessão iniciada com segurança")
                        ),
                        onDismiss = { showLgpdScreen = false }
                    )
                } else {
                    Scaffold(
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier.testTag("nanei_bottom_navigation"),
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 3.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentDestination == NaneiDestination.HOME,
                                    onClick = { viewModel.selectDestination(NaneiDestination.HOME) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Timeline,
                                            contentDescription = "Linha do Tempo"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Início",
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    alwaysShowLabel = true,
                                    modifier = Modifier.testTag("nav_home")
                                )
                                NavigationBarItem(
                                    selected = currentDestination == NaneiDestination.PREGNANCY,
                                    onClick = { viewModel.selectDestination(NaneiDestination.PREGNANCY) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.PregnantWoman,
                                            contentDescription = "Gravidez"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Gravidez",
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    alwaysShowLabel = true,
                                    modifier = Modifier.testTag("nav_pregnancy")
                                )
                                NavigationBarItem(
                                    selected = currentDestination == NaneiDestination.MOM_JOURNAL,
                                    onClick = { viewModel.selectDestination(NaneiDestination.MOM_JOURNAL) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Book,
                                            contentDescription = "Diário"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Diário",
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    alwaysShowLabel = true,
                                    modifier = Modifier.testTag("nav_mom_journal")
                                )
                                NavigationBarItem(
                                    selected = currentDestination == NaneiDestination.ANALYTICS,
                                    onClick = { viewModel.selectDestination(NaneiDestination.ANALYTICS) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.BarChart,
                                            contentDescription = "Análises"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Análises",
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    alwaysShowLabel = true,
                                    modifier = Modifier.testTag("nav_analytics")
                                )

                                NavigationBarItem(
                                    selected = currentDestination == NaneiDestination.DEVELOPMENT,
                                    onClick = { viewModel.selectDestination(NaneiDestination.DEVELOPMENT) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Desenvolvimento"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Marcos",
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    alwaysShowLabel = true,
                                    modifier = Modifier.testTag("nav_development")
                                )
                                NavigationBarItem(
                                    selected = currentDestination == NaneiDestination.MEDICATION,
                                    onClick = { viewModel.selectDestination(NaneiDestination.MEDICATION) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Medication,
                                            contentDescription = "Medicamentos"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Fármacos",
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    alwaysShowLabel = true,
                                    modifier = Modifier.testTag("nav_medication")
                                )
                                NavigationBarItem(
                                    selected = currentDestination == NaneiDestination.AI_ASSISTANT,
                                    onClick = { viewModel.selectDestination(NaneiDestination.AI_ASSISTANT) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "IA Nanei"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "IA Nanei",
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    alwaysShowLabel = true,
                                    modifier = Modifier.testTag("nav_ai_assistant")
                                )
                            }
                        }
                    ) { innerPadding ->
                        val screenModifier = Modifier.padding(innerPadding)

                        when (currentDestination) {
                            NaneiDestination.HOME -> HomeScreen(
                                selectedBaby = selectedBaby,
                                allBabies = allBabies,
                                events = events,
                                sweetSpot = sweetSpot,
                                onSwitchBaby = { babyId -> viewModel.switchBaby(babyId) },
                                onAddNewBaby = { name, birthMs, estMs, gender, avatarUri ->
                                    viewModel.addNewBaby(name, birthMs, estMs, gender, avatarUri)
                                },
                                onLogActionClick = { eventType -> viewModel.openLogDialog(eventType) },
                                onDeleteEvent = { event -> viewModel.deleteEvent(event) },
                                onNightModeToggle = { viewModel.toggleNightMode() },
                                onOpenAiAssistant = { viewModel.selectDestination(NaneiDestination.AI_ASSISTANT) },
                                onOpenFamilyDialog = { viewModel.openFamilyDialog() },
                                onOpenSoundListenDialog = { viewModel.openSoundListenDialog() },
                                onOpenOnboarding = { viewModel.openOnboarding() },
                                isPremiumUser = isPremiumUser,
                                onOpenPaywall = { viewModel.openPaywall() },
                                modifier = screenModifier
                            )
                            NaneiDestination.PREGNANCY -> PregnancyScreen(
                                currentWeek = gestationalWeek,
                                kickSessions = kickSessions,
                                contractions = contractions,
                                hospitalBagItems = hospitalBagItems,
                                prenatalExams = prenatalExams,
                                onAddKickSession = { count, dur -> viewModel.addKickSession(count, dur) },
                                onAddContraction = { dur, inter -> viewModel.addContraction(dur, inter) },
                                onToggleBagItem = { id -> viewModel.toggleHospitalBagItem(id) },
                                onToggleExam = { id -> viewModel.togglePrenatalExam(id) },
                                onNavigateToJournal = { viewModel.selectDestination(NaneiDestination.MOM_JOURNAL) },
                                modifier = screenModifier
                            )
                            NaneiDestination.MOM_JOURNAL -> MomJournalScreen(
                                babyName = selectedBaby?.name ?: "Bebê",
                                journalEntries = momJournalEntries,
                                onAddEntry = { entry -> viewModel.addMomJournalEntry(entry) },
                                onDeleteEntry = { entry -> viewModel.deleteMomJournalEntry(entry) },
                                modifier = screenModifier
                            )

                            NaneiDestination.ANALYTICS -> AnalyticsScreen(
                                baby = selectedBaby,
                                events = events,
                                isPremiumUser = isPremiumUser,
                                onOpenPaywall = { viewModel.openPaywall() },
                                modifier = screenModifier
                            )
                            NaneiDestination.DEVELOPMENT -> DevelopmentScreen(
                                baby = selectedBaby,
                                milestones = milestones,
                                onToggleMilestone = { milestone -> viewModel.toggleMilestone(milestone) },
                                modifier = screenModifier
                            )
                            NaneiDestination.MEDICATION -> MedicationScreen(
                                searchQuery = drugQuery,
                                filteredDrugs = filteredDrugs,
                                onSearchQueryChange = { query -> viewModel.setDrugSearchQuery(query) },
                                isPremiumUser = isPremiumUser,
                                onOpenPaywall = { viewModel.openPaywall() },
                                modifier = screenModifier
                            )
                            NaneiDestination.AI_ASSISTANT -> AiAssistantScreen(
                                chatMessages = chatMessages,
                                isLoading = isAiLoading,
                                onSendMessage = { text -> viewModel.sendUserMessageToAi(text) },
                                onConfirmPendingEvents = { eventsToSave -> viewModel.confirmPendingEvents(eventsToSave) },
                                onCancelPendingEvents = { viewModel.cancelPendingEvents() },
                                onOpenLgpdPortal = { showLgpdScreen = true },
                                isPremiumUser = isPremiumUser,
                                onOpenPaywall = { viewModel.openPaywall() },
                                modifier = screenModifier
                            )
                        }
                    }
                }

                // Modal Bottom Sheet for logging events (Amamentação, Mamadeira, Fralda, Sono, etc)
                activeLogDialogType?.let { eventType ->
                    selectedBaby?.let { baby ->
                        LogEventBottomSheet(
                            eventType = eventType,
                            babyId = baby.id,
                            onDismiss = { viewModel.closeLogDialog() },
                            onSaveEvent = { event -> viewModel.logEvent(event) }
                        )
                    }
                }

                // Nanei Premium Paywall & Ad-Free Subscription Dialog
                if (showPaywallDialog) {
                    NaneiPaywallDialog(
                        isPremiumActive = isPremiumUser,
                        onDismiss = { viewModel.closePaywall() },
                        onPurchasePlan = { plan -> viewModel.purchasePremium(plan) },
                        onRestorePurchases = { viewModel.restorePurchases() },
                        onToggleTestMode = { viewModel.togglePremiumForTesting() }
                    )
                }

                // F1 — Onboarding e Consentimento
                if (showOnboarding) {
                    OnboardingModal(
                        onDismiss = { viewModel.completeOnboarding() },
                        onCompleteOnboarding = { babyName, birthMs, estMs, gender, avatarUri ->
                            viewModel.addNewBaby(babyName, birthMs, estMs, gender, avatarUri)
                            viewModel.completeOnboarding()
                        }
                    )
                }

                // F4 — Família & Cuidadores
                if (showFamilyDialog) {
                    FamilyManagementDialog(
                        onDismiss = { viewModel.closeFamilyDialog() }
                    )
                }

                // F8 — Modo Escuta e Detector de Choro
                if (showSoundListenDialog) {
                    SoundListenModeDialog(
                        onDismiss = { viewModel.closeSoundListenDialog() }
                    )
                }
            }
        }
    }
}
