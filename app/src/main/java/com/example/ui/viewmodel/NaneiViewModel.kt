package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.NaneiDatabase
import com.example.data.model.*
import com.example.data.provider.NaneiStaticData
import com.example.data.repository.NaneiRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NaneiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NaneiRepository(NaneiDatabase.getDatabase(application))

    // All registered babies
    val allBabies: StateFlow<List<Baby>> = repository.allBabies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active selected baby
    val selectedBaby: StateFlow<Baby?> = repository.selectedBaby
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Events for selected baby
    val activeBabyEvents: StateFlow<List<Event>> = selectedBaby.flatMapLatest { baby ->
        if (baby != null) {
            repository.getEventsForBaby(baby.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Milestones for selected baby
    val activeBabyMilestones: StateFlow<List<Milestone>> = selectedBaby.flatMapLatest { baby ->
        if (baby != null) {
            repository.getMilestonesForBaby(baby.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reminders for selected baby
    val activeBabyReminders: StateFlow<List<Reminder>> = selectedBaby.flatMapLatest { baby ->
        if (baby != null) {
            repository.getRemindersForBaby(baby.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // SweetSpot Sleep Prediction
    private val _sweetSpot = MutableStateFlow<SweetSpotPrediction?>(null)
    val sweetSpot: StateFlow<SweetSpotPrediction?> = _sweetSpot.asStateFlow()

    // Pregnancy Module State
    private val _gestationalWeek = MutableStateFlow(24)
    val gestationalWeek: StateFlow<Int> = _gestationalWeek.asStateFlow()

    private val _kickSessions = MutableStateFlow<List<KickSession>>(emptyList())
    val kickSessions: StateFlow<List<KickSession>> = _kickSessions.asStateFlow()

    private val _contractions = MutableStateFlow<List<ContractionEntry>>(emptyList())
    val contractions: StateFlow<List<ContractionEntry>> = _contractions.asStateFlow()

    private val _hospitalBagItems = MutableStateFlow<List<HospitalBagItem>>(NaneiStaticData.getDefaultHospitalBag())
    val hospitalBagItems: StateFlow<List<HospitalBagItem>> = _hospitalBagItems.asStateFlow()

    private val _prenatalExams = MutableStateFlow<List<PrenatalExam>>(NaneiStaticData.getDefaultPrenatalExams())
    val prenatalExams: StateFlow<List<PrenatalExam>> = _prenatalExams.asStateFlow()

    // Mom's Journal State
    private val _momJournalEntries = MutableStateFlow<List<MomJournalEntry>>(NaneiStaticData.getDefaultMomJournalEntries())
    val momJournalEntries: StateFlow<List<MomJournalEntry>> = _momJournalEntries.asStateFlow()

    fun addKickSession(count: Int, durationSec: Long) {
        val newSession = KickSession(kickCount = count, durationSeconds = durationSec)
        _kickSessions.value = listOf(newSession) + _kickSessions.value
    }

    fun addContraction(durationSec: Int, intervalSec: Int) {
        val newEntry = ContractionEntry(durationSeconds = durationSec, intervalSeconds = intervalSec)
        _contractions.value = listOf(newEntry) + _contractions.value
    }

    fun toggleHospitalBagItem(id: String) {
        _hospitalBagItems.value = _hospitalBagItems.value.map {
            if (it.id == id) it.copy(isChecked = !it.isChecked) else it
        }
    }

    fun togglePrenatalExam(id: String) {
        _prenatalExams.value = _prenatalExams.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
    }

    fun addMomJournalEntry(entry: MomJournalEntry) {
        _momJournalEntries.value = listOf(entry) + _momJournalEntries.value
    }

    fun deleteMomJournalEntry(entry: MomJournalEntry) {
        _momJournalEntries.value = _momJournalEntries.value.filter { it.id != entry.id }
    }

    // Navigation state
    private val _currentDestination = MutableStateFlow(NaneiDestination.HOME)
    val currentDestination: StateFlow<NaneiDestination> = _currentDestination.asStateFlow()


    // Medication search query
    private val _drugSearchQuery = MutableStateFlow("")
    val drugSearchQuery: StateFlow<String> = _drugSearchQuery.asStateFlow()

    private fun normalizeString(text: String): String {
        val normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "").lowercase()
    }

    val filteredDrugs: StateFlow<List<DrugInfo>> = _drugSearchQuery.map { query ->
        val list = NaneiStaticData.getDrugDatabase()
        if (query.isBlank()) {
            list
        } else {
            val q = normalizeString(query)
            list.filter { drug ->
                normalizeString(drug.genericName).contains(q) ||
                        drug.brandNames.any { normalizeString(it).contains(q) } ||
                        normalizeString(drug.category).contains(q) ||
                        normalizeString(drug.SummaryPt).contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NaneiStaticData.getDrugDatabase())

    // AI Chat History
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = Sender.AI,
                text = "Olá! Eu sou a Nanei, sua assistente com inteligência Gemini. Você pode gravar sua voz ou digitar comandos como: 'Amamentei 15 min no esquerdo' ou perguntar: 'Quando foi a última mamada?'"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Pending Voice Parse Result awaiting user confirmation
    private val _pendingVoiceEvents = MutableStateFlow<List<Event>?>(null)
    val pendingVoiceEvents: StateFlow<List<Event>?> = _pendingVoiceEvents.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Dialog state for logging events
    private val _activeLogDialogType = MutableStateFlow<EventType?>(null)
    val activeLogDialogType: StateFlow<EventType?> = _activeLogDialogType.asStateFlow()

    // Night Mode / Theme State
    private val _isNightModeForce = MutableStateFlow<Boolean?>(null)
    val isNightModeForce: StateFlow<Boolean?> = _isNightModeForce.asStateFlow()

    // Monetization & Premium Ad-Free State
    private val prefs = application.getSharedPreferences("nanei_monetization_prefs", android.content.Context.MODE_PRIVATE)
    private val _isPremiumUser = MutableStateFlow(prefs.getBoolean("key_is_premium", false))
    val isPremiumUser: StateFlow<Boolean> = _isPremiumUser.asStateFlow()

    private val _showPaywallDialog = MutableStateFlow(false)
    val showPaywallDialog: StateFlow<Boolean> = _showPaywallDialog.asStateFlow()

    // Onboarding & Consent State (F1)
    private val _showOnboarding = MutableStateFlow(!prefs.getBoolean("key_onboarding_completed", false))
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    // Family & Caregiver Invite State (F4)
    private val _showFamilyDialog = MutableStateFlow(false)
    val showFamilyDialog: StateFlow<Boolean> = _showFamilyDialog.asStateFlow()

    // Sound Monitor & Cry Detector State (F8)
    private val _showSoundListenDialog = MutableStateFlow(false)
    val showSoundListenDialog: StateFlow<Boolean> = _showSoundListenDialog.asStateFlow()

    init {
        // Ensure at least one baby exists
        viewModelScope.launch {
            val baby = repository.createInitialBabyIfNone()
            refreshSweetSpot(baby)
        }

        // Re-calculate SweetSpot whenever events or selected baby change
        viewModelScope.launch {
            combine(selectedBaby, activeBabyEvents) { baby, _ ->
                baby
            }.collect { baby ->
                if (baby != null) {
                    refreshSweetSpot(baby)
                }
            }
        }
    }

    fun selectDestination(destination: NaneiDestination) {
        _currentDestination.value = destination
    }

    fun setDrugSearchQuery(query: String) {
        _drugSearchQuery.value = query
    }

    fun switchBaby(babyId: Long) {
        viewModelScope.launch {
            repository.switchSelectedBaby(babyId)
        }
    }

    fun addNewBaby(name: String, birthDateMs: Long, estimatedDueDateMs: Long, gender: String, avatarUri: String? = null) {
        viewModelScope.launch {
            repository.addNewBaby(name, birthDateMs, estimatedDueDateMs, gender, avatarUri)
        }
    }

    fun openLogDialog(eventType: EventType) {
        _activeLogDialogType.value = eventType
    }

    fun closeLogDialog() {
        _activeLogDialogType.value = null
    }

    fun logEvent(event: Event) {
        viewModelScope.launch {
            repository.logEvent(event)
            closeLogDialog()
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    fun toggleMilestone(milestone: Milestone) {
        viewModelScope.launch {
            repository.toggleMilestone(milestone)
        }
    }

    fun toggleNightMode() {
        val current = _isNightModeForce.value
        _isNightModeForce.value = when (current) {
            true -> false
            false -> null // Back to automatic
            null -> true
        }
    }

    // --- Gemini Voice / Text Input handling ---
    fun sendUserMessageToAi(userText: String) {
        if (userText.isBlank()) return

        val userMessage = ChatMessage(sender = Sender.USER, text = userText)
        _chatMessages.value = _chatMessages.value + userMessage
        _isAiLoading.value = true

        val currentBaby = selectedBaby.value ?: return

        viewModelScope.launch {
            try {
                val isQuestion = userText.contains("?") ||
                        userText.lowercase().startsWith("quando") ||
                        userText.lowercase().startsWith("quanto") ||
                        userText.lowercase().startsWith("como") ||
                        userText.lowercase().startsWith("qual")

                if (isQuestion) {
                    val answer = repository.askGeminiQuestion(currentBaby, userText)
                    _chatMessages.value = _chatMessages.value + ChatMessage(sender = Sender.AI, text = answer)
                } else {
                    // Command to log events
                    val parsedEvents = repository.parseVoiceOrTextWithGemini(currentBaby.id, userText)
                    _pendingVoiceEvents.value = parsedEvents

                    val confirmPrompt = "Compreendi! Extraí ${parsedEvents.size} evento(s) da sua mensagem. Por favor, confirme abaixo para salvar na linha do tempo."
                    _chatMessages.value = _chatMessages.value + ChatMessage(
                        sender = Sender.AI,
                        text = confirmPrompt,
                        parsedEvents = parsedEvents,
                        isPendingConfirmation = true
                    )
                }
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    sender = Sender.AI,
                    text = "Desculpe, ocorreu uma falha ao processar sua solicitação: ${e.message}"
                )
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun confirmPendingEvents(events: List<Event>) {
        viewModelScope.launch {
            events.forEach { event ->
                repository.logEvent(event)
            }
            _pendingVoiceEvents.value = null
            _chatMessages.value = _chatMessages.value + ChatMessage(
                sender = Sender.SYSTEM,
                text = "✅ ${events.size} evento(s) confirmados e registrados na linha do tempo com sucesso!"
            )
        }
    }

    fun cancelPendingEvents() {
        _pendingVoiceEvents.value = null
        _chatMessages.value = _chatMessages.value + ChatMessage(
            sender = Sender.SYSTEM,
            text = "❌ Registro cancelado."
        )
    }

    fun openPaywall() {
        _showPaywallDialog.value = true
    }

    fun closePaywall() {
        _showPaywallDialog.value = false
    }

    fun purchasePremium(plan: String) {
        prefs.edit().putBoolean("key_is_premium", true).apply()
        _isPremiumUser.value = true
        _showPaywallDialog.value = false
        _chatMessages.value = _chatMessages.value + ChatMessage(
            sender = Sender.SYSTEM,
            text = "🎉 Parabéns! Sua assinatura Nanei Premium ($plan) foi ativada com sucesso. Todos os anúncios foram removidos!"
        )
    }

    fun restorePurchases() {
        prefs.edit().putBoolean("key_is_premium", true).apply()
        _isPremiumUser.value = true
        _showPaywallDialog.value = false
        _chatMessages.value = _chatMessages.value + ChatMessage(
            sender = Sender.SYSTEM,
            text = "✨ Suas compras foram restauradas com sucesso. Acesso Nanei Premium ativo!"
        )
    }

    fun togglePremiumForTesting() {
        val next = !_isPremiumUser.value
        prefs.edit().putBoolean("key_is_premium", next).apply()
        _isPremiumUser.value = next
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("key_onboarding_completed", true).apply()
        _showOnboarding.value = false
    }

    fun openOnboarding() {
        _showOnboarding.value = true
    }

    fun openFamilyDialog() {
        _showFamilyDialog.value = true
    }

    fun closeFamilyDialog() {
        _showFamilyDialog.value = false
    }

    fun openSoundListenDialog() {
        _showSoundListenDialog.value = true
    }

    fun closeSoundListenDialog() {
        _showSoundListenDialog.value = false
    }

    private suspend fun refreshSweetSpot(baby: Baby) {
        _sweetSpot.value = repository.calculateSweetSpot(baby)
    }
}

enum class NaneiDestination(val title: String, val iconName: String) {
    HOME("Linha do Tempo", "ic_home"),
    PREGNANCY("Gravidez", "ic_pregnancy"),
    MOM_JOURNAL("Diário & Livro", "ic_journal"),
    ANALYTICS("Análises", "ic_analytics"),
    DEVELOPMENT("Desenvolvimento", "ic_development"),
    MEDICATION("Medicamentos", "ic_medication"),
    AI_ASSISTANT("Assistente IA", "ic_assistant")
}

