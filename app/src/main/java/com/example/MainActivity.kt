package com.example

import android.content.Intent
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
import com.example.data.model.EventType
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.NaneiTheme
import com.example.ui.viewmodel.NaneiDestination
import com.example.ui.viewmodel.NaneiViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: NaneiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleShortcutIntent(intent)

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
            val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
            val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
            val showPaywallDialog by viewModel.showPaywallDialog.collectAsStateWithLifecycle()
            val showOnboarding by viewModel.showOnboarding.collectAsStateWithLifecycle()
            val showFamilyDialog by viewModel.showFamilyDialog.collectAsStateWithLifecycle()
            val showSoundListenDialog by viewModel.showSoundListenDialog.collectAsStateWithLifecycle()
            val showCloudBackupDialog by viewModel.showCloudBackupDialog.collectAsStateWithLifecycle()
            val lastCloudBackupTimeMs by viewModel.lastCloudBackupTimeMs.collectAsStateWithLifecycle()

            val gestationalWeek by viewModel.gestationalWeek.collectAsStateWithLifecycle()
            val kickSessions by viewModel.kickSessions.collectAsStateWithLifecycle()
            val contractions by viewModel.contractions.collectAsStateWithLifecycle()
            val hospitalBagItems by viewModel.hospitalBagItems.collectAsStateWithLifecycle()
            val prenatalExams by viewModel.prenatalExams.collectAsStateWithLifecycle()
            val momJournalEntries by viewModel.momJournalEntries.collectAsStateWithLifecycle()

            val babyShowerEvent by viewModel.babyShowerEvent.collectAsStateWithLifecycle()
            val babyShowerGuests by viewModel.babyShowerGuests.collectAsStateWithLifecycle()
            val babyShowerGifts by viewModel.babyShowerGifts.collectAsStateWithLifecycle()
            val syncTestLogs by viewModel.syncTestLogs.collectAsStateWithLifecycle()
            val isShowerSyncing by viewModel.isShowerSyncing.collectAsStateWithLifecycle()

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
                                            imageVector = Icons.Default.Home,
                                            contentDescription = "Início"
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
                                    selected = currentDestination == NaneiDestination.BABY_SHOWER,
                                    onClick = { viewModel.selectDestination(NaneiDestination.BABY_SHOWER) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.CardGiftcard,
                                            contentDescription = "Chá & Eventos"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Chá & Eventos",
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    alwaysShowLabel = true,
                                    modifier = Modifier.testTag("nav_baby_shower")
                                )
                                NavigationBarItem(
                                    selected = currentDestination == NaneiDestination.HEALTH,
                                    onClick = { viewModel.selectDestination(NaneiDestination.HEALTH) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.MedicalServices,
                                            contentDescription = "Saúde"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Saúde",
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    alwaysShowLabel = true,
                                    modifier = Modifier.testTag("nav_health")
                                )
                                NavigationBarItem(
                                    selected = currentDestination == NaneiDestination.MOM_JOURNAL,
                                    onClick = { viewModel.selectDestination(NaneiDestination.MOM_JOURNAL) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.PregnantWoman,
                                            contentDescription = "Gestação"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Gestação",
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    alwaysShowLabel = true,
                                    modifier = Modifier.testTag("nav_mom_journal")
                                )
                                NavigationBarItem(
                                    selected = currentDestination == NaneiDestination.AI_ASSISTANT,
                                    onClick = { viewModel.selectDestination(NaneiDestination.AI_ASSISTANT) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
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
                                onOpenCloudBackupDialog = { viewModel.openCloudBackupDialog() },
                                onOpenOnboarding = { viewModel.openOnboarding() },
                                isPremiumUser = isPremiumUser,
                                onOpenPaywall = { viewModel.openPaywall() },
                                modifier = screenModifier
                            )
                            NaneiDestination.BABY_SHOWER -> BabyShowerScreen(
                                event = babyShowerEvent,
                                guests = babyShowerGuests,
                                gifts = babyShowerGifts,
                                syncLogs = syncTestLogs,
                                isSyncing = isShowerSyncing,
                                userEmail = userEmail,
                                onUpdateEvent = { newEvt -> viewModel.updateBabyShowerEvent(newEvt) },
                                onAddGuest = { gst -> viewModel.addBabyShowerGuest(gst) },
                                onUpdateGuestStatus = { id, st -> viewModel.updateGuestStatus(id, st) },
                                onDeleteGuest = { id -> viewModel.deleteBabyShowerGuest(id) },
                                onAddGift = { gft -> viewModel.addBabyShowerGift(gft) },
                                onToggleGiftReservation = { id, name -> viewModel.toggleGiftReservation(id, name) },
                                onRunSyncTests = { viewModel.runBabyShowerSyncTests() },
                                modifier = screenModifier
                            )
                            NaneiDestination.HEALTH -> HealthScreen(
                                baby = selectedBaby,
                                events = events,
                                milestones = milestones,
                                searchQuery = drugQuery,
                                filteredDrugs = filteredDrugs,
                                onSearchQueryChange = { query -> viewModel.setDrugSearchQuery(query) },
                                onToggleMilestone = { milestone -> viewModel.toggleMilestone(milestone) },
                                isPremiumUser = isPremiumUser,
                                onOpenPaywall = { viewModel.openPaywall() },
                                modifier = screenModifier
                            )
                            NaneiDestination.MOM_JOURNAL -> PregnancyAndJournalScreen(
                                currentWeek = gestationalWeek,
                                kickSessions = kickSessions,
                                contractions = contractions,
                                hospitalBagItems = hospitalBagItems,
                                prenatalExams = prenatalExams,
                                babyName = selectedBaby?.name ?: "Bebê",
                                journalEntries = momJournalEntries,
                                onAddKickSession = { count, dur -> viewModel.addKickSession(count, dur) },
                                onAddContraction = { dur, inter -> viewModel.addContraction(dur, inter) },
                                onToggleBagItem = { id -> viewModel.toggleHospitalBagItem(id) },
                                onToggleExam = { id -> viewModel.togglePrenatalExam(id) },
                                onAddJournalEntry = { entry -> viewModel.addMomJournalEntry(entry) },
                                onDeleteJournalEntry = { entry -> viewModel.deleteMomJournalEntry(entry) },
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
                        isLoggedIn = isLoggedIn,
                        userEmail = userEmail,
                        onPerformLogin = { email -> viewModel.performLogin(email) },
                        onDismiss = { viewModel.completeOnboarding() },
                        onCompleteOnboarding = { babyName, birthMs, estMs, gender, avatarUri, email ->
                            viewModel.addNewBaby(babyName, birthMs, estMs, gender, avatarUri)
                            viewModel.completeOnboarding(email)
                        }
                    )
                }

                // F4 — Família & Cuidadores
                if (showFamilyDialog) {
                    FamilyManagementDialog(
                        userEmail = userEmail,
                        onOpenCloudBackup = {
                            viewModel.closeFamilyDialog()
                            viewModel.openCloudBackupDialog()
                        },
                        onDismiss = { viewModel.closeFamilyDialog() }
                    )
                }

                // F8 — Modo Escuta e Detector de Choro
                if (showSoundListenDialog) {
                    SoundListenModeDialog(
                        onDismiss = { viewModel.closeSoundListenDialog() }
                    )
                }

                // Premium Cloud Backup & Phone Switch Restore
                if (showCloudBackupDialog) {
                    CloudBackupRestoreDialog(
                        userEmail = userEmail,
                        isPremiumUser = isPremiumUser,
                        lastBackupTimeMs = lastCloudBackupTimeMs,
                        onPerformBackup = { callback: (Boolean, String) -> Unit -> viewModel.performCloudBackup(callback) },
                        onRestoreBackup = { targetEmail: String, customJson: String?, callback: (Boolean, String) -> Unit -> viewModel.restoreCloudBackup(targetEmail, customJson, callback) },
                        onOpenPaywall = { viewModel.openPaywall() },
                        onDismiss = { viewModel.closeCloudBackupDialog() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShortcutIntent(intent)
    }

    private fun handleShortcutIntent(intent: Intent?) {
        val action = intent?.getStringExtra("shortcut_action") ?: return
        when (action) {
            "LOG_FEEDING" -> {
                viewModel.selectDestination(NaneiDestination.HOME)
                viewModel.openLogDialog(EventType.BREASTFEEDING)
            }
            "OPEN_AI" -> {
                viewModel.selectDestination(NaneiDestination.AI_ASSISTANT)
            }
            "OPEN_JOURNAL" -> {
                viewModel.selectDestination(NaneiDestination.MOM_JOURNAL)
            }
            "LOG_DIAPER" -> {
                viewModel.selectDestination(NaneiDestination.HOME)
                viewModel.openLogDialog(EventType.DIAPER)
            }
            "OPEN_BABY_SHOWER" -> {
                viewModel.selectDestination(NaneiDestination.BABY_SHOWER)
            }
        }
        intent.removeExtra("shortcut_action")
    }
}
