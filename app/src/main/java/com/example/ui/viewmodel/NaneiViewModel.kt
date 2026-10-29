package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.NaneiApi
import com.example.data.backup.NaneiBackupManager
import com.example.data.backup.NaneiCloudBackupPayload
import com.example.data.local.BabyShowerStore
import com.example.data.local.NaneiDatabase
import com.example.data.local.PregnancyStore
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

    // Pregnancy Module State — persistido via PregnancyStore (desde a v6;
    // antes tudo era simulado em memória e se perdia ao fechar o app).
    private val appContext: Application = application

    /** DPP em ms; null = gestação ainda não registrada. */
    private val _dueDateMs = MutableStateFlow<Long?>(
        PregnancyStore.getDueDateMs(application).takeIf { it > 0L }
    )
    val dueDateMs: StateFlow<Long?> = _dueDateMs.asStateFlow()

    /** Semana gestacional calculada da DPP; 0 = sem gestação registrada. */
    private val _gestationalWeek = MutableStateFlow(computeGestationalWeek(_dueDateMs.value))
    val gestationalWeek: StateFlow<Int> = _gestationalWeek.asStateFlow()

    private fun computeGestationalWeek(dueMs: Long?): Int {
        if (dueMs == null) return 0
        val weeksLeft = ((dueMs - System.currentTimeMillis()) / (7L * 24 * 3600 * 1000)).toInt()
        return (40 - weeksLeft).coerceIn(1, 42)
    }

    fun registerPregnancy(dueDateMs: Long) {
        PregnancyStore.setDueDateMs(appContext, dueDateMs)
        _dueDateMs.value = dueDateMs
        _gestationalWeek.value = computeGestationalWeek(dueDateMs)
    }

    fun clearPregnancy() {
        PregnancyStore.clearPregnancy(appContext)
        _dueDateMs.value = null
        _gestationalWeek.value = 0
        _kickSessions.value = emptyList()
        _contractions.value = emptyList()
    }

    private val _kickSessions = MutableStateFlow<List<KickSession>>(PregnancyStore.loadKickSessions(application))
    val kickSessions: StateFlow<List<KickSession>> = _kickSessions.asStateFlow()

    private val _contractions = MutableStateFlow<List<ContractionEntry>>(PregnancyStore.loadContractions(application))
    val contractions: StateFlow<List<ContractionEntry>> = _contractions.asStateFlow()

    private val _hospitalBagItems = MutableStateFlow<List<HospitalBagItem>>(
        NaneiStaticData.getDefaultHospitalBag().map { item ->
            item.copy(isChecked = item.id in PregnancyStore.loadCheckedIds(application, bag = true))
        }
    )
    val hospitalBagItems: StateFlow<List<HospitalBagItem>> = _hospitalBagItems.asStateFlow()

    private val _prenatalExams = MutableStateFlow<List<PrenatalExam>>(
        NaneiStaticData.getDefaultPrenatalExams().map { exam ->
            exam.copy(isCompleted = exam.id in PregnancyStore.loadCheckedIds(application, bag = false))
        }
    )
    val prenatalExams: StateFlow<List<PrenatalExam>> = _prenatalExams.asStateFlow()

    // Mom's Journal State — persistido; começa vazio (sem entradas de exemplo)
    private val _momJournalEntries = MutableStateFlow<List<MomJournalEntry>>(PregnancyStore.loadJournalEntries(application))
    val momJournalEntries: StateFlow<List<MomJournalEntry>> = _momJournalEntries.asStateFlow()

    fun addKickSession(count: Int, durationSec: Long) {
        val newSession = KickSession(kickCount = count, durationSeconds = durationSec)
        _kickSessions.value = listOf(newSession) + _kickSessions.value
        PregnancyStore.saveKickSessions(appContext, _kickSessions.value)
    }

    fun addContraction(durationSec: Int, intervalSec: Int) {
        val newEntry = ContractionEntry(durationSeconds = durationSec, intervalSeconds = intervalSec)
        _contractions.value = listOf(newEntry) + _contractions.value
        PregnancyStore.saveContractions(appContext, _contractions.value)
    }

    fun toggleHospitalBagItem(id: String) {
        _hospitalBagItems.value = _hospitalBagItems.value.map {
            if (it.id == id) it.copy(isChecked = !it.isChecked) else it
        }
        PregnancyStore.saveCheckedIds(
            appContext,
            _hospitalBagItems.value.filter { it.isChecked }.map { it.id }.toSet(),
            bag = true
        )
    }

    fun togglePrenatalExam(id: String) {
        _prenatalExams.value = _prenatalExams.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
        PregnancyStore.saveCheckedIds(
            appContext,
            _prenatalExams.value.filter { it.isCompleted }.map { it.id }.toSet(),
            bag = false
        )
    }

    fun addMomJournalEntry(entry: MomJournalEntry) {
        _momJournalEntries.value = listOf(entry) + _momJournalEntries.value
        PregnancyStore.saveJournalEntries(appContext, _momJournalEntries.value)
    }

    fun deleteMomJournalEntry(entry: MomJournalEntry) {
        _momJournalEntries.value = _momJournalEntries.value.filter { it.id != entry.id }
        PregnancyStore.saveJournalEntries(appContext, _momJournalEntries.value)
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

    // Persistent Login & Account State
    private val prefs = application.getSharedPreferences("nanei_monetization_prefs", android.content.Context.MODE_PRIVATE)
    private val _isLoggedIn = MutableStateFlow(
        prefs.getBoolean("key_is_logged_in", false) || prefs.getBoolean("key_onboarding_completed", false)
    )
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userEmail = MutableStateFlow(
        prefs.getString("key_user_email", "") ?: ""
    )
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    /** Nome da mãe/responsável na conta local do aparelho. */
    private val _userName = MutableStateFlow(prefs.getString("key_user_name", "") ?: "")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // Onboarding & Consent State (F1)
    private val _showOnboarding = MutableStateFlow(
        !prefs.getBoolean("key_onboarding_completed", false) && !prefs.getBoolean("key_is_logged_in", false)
    )
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    // Family & Caregiver Invite State (F4)
    private val _showFamilyDialog = MutableStateFlow(false)
    val showFamilyDialog: StateFlow<Boolean> = _showFamilyDialog.asStateFlow()

    // Sound Monitor & Cry Detector State (F8)
    private val _showSoundListenDialog = MutableStateFlow(false)
    val showSoundListenDialog: StateFlow<Boolean> = _showSoundListenDialog.asStateFlow()

    // Cloud Backup & Phone Switch Recovery
    private val _showCloudBackupDialog = MutableStateFlow(false)
    val showCloudBackupDialog: StateFlow<Boolean> = _showCloudBackupDialog.asStateFlow()

    private val _lastCloudBackupTimeMs = MutableStateFlow(prefs.getLong("key_last_cloud_backup_time", 0L))
    val lastCloudBackupTimeMs: StateFlow<Long> = _lastCloudBackupTimeMs.asStateFlow()

    init {
        // Nenhum bebê fictício é criado no primeiro acesso (v6): o SweetSpot
        // só é calculado quando existe um bebê real cadastrado pelo usuário.
        viewModelScope.launch {
            combine(selectedBaby, activeBabyEvents) { baby, _ ->
                baby
            }.collect { baby ->
                if (baby != null) {
                    refreshSweetSpot(baby)
                } else {
                    _sweetSpot.value = null
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

    /**
     * Criação de conta do primeiro acesso (v6). A conta é local do
     * aparelho: nome, e-mail e hash da senha ficam em SharedPreferences —
     * nada é enviado a servidores. NÃO marca o onboarding como concluído:
     * isso só acontece no passo final, após consentimentos e a escolha
     * opcional de cadastrar bebê ou gestação.
     */
    fun performSignup(name: String, email: String, password: String) {
        val cleanEmail = email.trim()
        prefs.edit()
            .putBoolean("key_is_logged_in", true)
            .putString("key_user_name", name.trim())
            .putString("key_user_email", cleanEmail)
            .putString("key_user_password_hash", password.hashCode().toString())
            .apply()
        _userName.value = name.trim()
        _userEmail.value = cleanEmail
        _isLoggedIn.value = true
    }

    fun completeOnboarding(email: String = "", dueDateMs: Long? = null) {
        val cleanEmail = if (email.isBlank()) _userEmail.value else email.trim()
        prefs.edit()
            .putBoolean("key_onboarding_completed", true)
            .putBoolean("key_is_logged_in", true)
            .putString("key_user_email", cleanEmail)
            .apply()
        _userEmail.value = cleanEmail
        _isLoggedIn.value = true
        if (dueDateMs != null) {
            registerPregnancy(dueDateMs)
        }
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

    fun openCloudBackupDialog() {
        _showCloudBackupDialog.value = true
    }

    fun closeCloudBackupDialog() {
        _showCloudBackupDialog.value = false
    }

    fun performCloudBackup(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val babies = repository.getAllBabiesSync()
                val events = repository.getAllEventsSync()
                val milestones = repository.getAllMilestonesSync()
                val reminders = repository.getAllRemindersSync()

                val payload = NaneiCloudBackupPayload(
                    userEmail = _userEmail.value,
                    babies = babies,
                    events = events,
                    milestones = milestones,
                    reminders = reminders,
                    momJournalEntries = _momJournalEntries.value,
                    kickSessions = _kickSessions.value,
                    contractions = _contractions.value
                )

                val json = NaneiBackupManager.toJson(payload)
                val now = System.currentTimeMillis()

                prefs.edit()
                    .putString("key_cloud_backup_json", json)
                    .putLong("key_last_cloud_backup_time", now)
                    .apply()

                _lastCloudBackupTimeMs.value = now
                onResult(
                    true,
                    "Backup realizado com sucesso! ${babies.size} bebê(s), ${events.size} evento(s) e ${_momJournalEntries.value.size} registro(s) do diário salvos neste dispositivo. Guarde também uma cópia exportada em local seguro."
                )
            } catch (e: Exception) {
                onResult(false, "Falha ao realizar backup em nuvem: ${e.message}")
            }
        }
    }

    fun restoreCloudBackup(targetEmail: String, customJson: String? = null, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val jsonStr = if (!customJson.isNullOrBlank()) customJson else prefs.getString("key_cloud_backup_json", null)
                if (jsonStr.isNullOrBlank()) {
                    onResult(false, "Nenhum backup foi encontrado neste dispositivo. Faça o backup primeiro ou cole um arquivo .json nas opções avançadas.")
                    return@launch
                }

                val payload = NaneiBackupManager.fromJson(jsonStr)
                if (payload == null) {
                    onResult(false, "O arquivo de backup selecionado ou na nuvem é inválido.")
                    return@launch
                }

                repository.restoreAllEntitiesSync(
                    babies = payload.babies,
                    events = payload.events,
                    milestones = payload.milestones,
                    reminders = payload.reminders
                )

                if (payload.momJournalEntries.isNotEmpty()) {
                    _momJournalEntries.value = payload.momJournalEntries
                }
                if (payload.kickSessions.isNotEmpty()) {
                    _kickSessions.value = payload.kickSessions
                }
                if (payload.contractions.isNotEmpty()) {
                    _contractions.value = payload.contractions
                }

                if (payload.userEmail.isNotBlank()) {
                    _userEmail.value = payload.userEmail
                    prefs.edit().putString("key_user_email", payload.userEmail).apply()
                }

                onResult(
                    true,
                    "Restauração concluída! Recuperados no novo celular: ${payload.babies.size} bebê(s), ${payload.events.size} evento(s) e ${payload.momJournalEntries.size} diário(s)."
                )
            } catch (e: Exception) {
                onResult(false, "Erro ao restaurar backup: ${e.message}")
            }
        }
    }

    private suspend fun refreshSweetSpot(baby: Baby) {
        _sweetSpot.value = repository.calculateSweetSpot(baby)
    }

    // --- Digital Baby Shower Module State ---
    private val _babyShowerEvent = MutableStateFlow(BabyShowerEvent())
    val babyShowerEvent: StateFlow<BabyShowerEvent> = _babyShowerEvent.asStateFlow()

    private val _babyShowerGuests = MutableStateFlow<List<BabyShowerGuest>>(emptyList())
    val babyShowerGuests: StateFlow<List<BabyShowerGuest>> = _babyShowerGuests.asStateFlow()

    private val _babyShowerGifts = MutableStateFlow<List<BabyShowerGift>>(emptyList())
    val babyShowerGifts: StateFlow<List<BabyShowerGift>> = _babyShowerGifts.asStateFlow()

    private val _syncTestLogs = MutableStateFlow<List<SyncTestLog>>(emptyList())
    val syncTestLogs: StateFlow<List<SyncTestLog>> = _syncTestLogs.asStateFlow()

    private val _isShowerSyncing = MutableStateFlow(false)
    val isShowerSyncing: StateFlow<Boolean> = _isShowerSyncing.asStateFlow()

    init {
        // Carrega o chá de bebê salvo no aparelho (antes da v5 os dados
        // eram simulados em memória e se perdiam ao fechar o app).
        BabyShowerStore.load(getApplication())?.let { saved ->
            _babyShowerEvent.value = saved.event
            _babyShowerGuests.value = saved.guests
            _babyShowerGifts.value = saved.gifts
        }
    }

    /** Persiste o estado atual do chá de bebê no armazenamento local. */
    private fun persistShower() {
        BabyShowerStore.save(
            getApplication(),
            _babyShowerEvent.value,
            _babyShowerGuests.value,
            _babyShowerGifts.value
        )
    }

    /** Marca que há alterações locais ainda não publicadas no site. */
    private fun markShowerDirty() {
        if (_babyShowerEvent.value.isSyncedWithWeb) {
            _babyShowerEvent.value = _babyShowerEvent.value.copy(isSyncedWithWeb = false)
        }
    }

    fun updateBabyShowerEvent(newEvent: BabyShowerEvent) {
        _babyShowerEvent.value = newEvent
        markShowerDirty()
        persistShower()
    }

    fun addBabyShowerGuest(guest: BabyShowerGuest) {
        _babyShowerGuests.value = _babyShowerGuests.value + guest
        markShowerDirty()
        persistShower()
    }

    fun updateGuestStatus(guestId: String, status: RsvpStatus) {
        _babyShowerGuests.value = _babyShowerGuests.value.map {
            if (it.id == guestId) it.copy(status = status) else it
        }
        markShowerDirty()
        persistShower()
    }

    fun deleteBabyShowerGuest(guestId: String) {
        _babyShowerGuests.value = _babyShowerGuests.value.filterNot { it.id == guestId }
        markShowerDirty()
        persistShower()
    }

    fun addBabyShowerGift(gift: BabyShowerGift) {
        _babyShowerGifts.value = _babyShowerGifts.value + gift
        markShowerDirty()
        persistShower()
    }

    fun toggleGiftReservation(giftId: String, guestName: String?) {
        _babyShowerGifts.value = _babyShowerGifts.value.map { gift ->
            if (gift.id == giftId) {
                gift.copy(
                    isReserved = guestName != null,
                    reservedByGuestName = guestName
                )
            } else gift
        }
        markShowerDirty()
        persistShower()
    }

    /**
     * Sincronização real com www.nanei.com.br:
     *  1. Salva os dados no aparelho
     *  2. Publica evento + convidados + presentes (POST /api/v1/events/sync)
     *  3. Baixa RSVPs e reservas feitos na página pública (GET /api/v1/events/manage)
     *
     * (Antes da v5 esta função apenas encenava logs falsos com sucesso
     * garantido — nenhum dado saía do aparelho.)
     */
    fun runBabyShowerSyncTests() {
        viewModelScope.launch {
            _isShowerSyncing.value = true
            val logs = mutableListOf<SyncTestLog>()
            fun push(log: SyncTestLog) {
                logs.add(log)
                _syncTestLogs.value = logs.toList()
            }

            // 1. Persistência local
            val t0 = System.currentTimeMillis()
            persistShower()
            push(SyncTestLog(
                stepName = "1. Salvamento local no aparelho",
                latencyMs = System.currentTimeMillis() - t0,
                isSuccess = true,
                details = "Evento, ${_babyShowerGuests.value.size} convidado(s) e ${_babyShowerGifts.value.size} presente(s) gravados no aparelho"
            ))

            // 2. Publicação no site
            val event = _babyShowerEvent.value
            val editKey = BabyShowerStore.getEditKey(getApplication())
            val syncResult = NaneiApi.syncShower(event, _babyShowerGuests.value, _babyShowerGifts.value, editKey)
            if (!syncResult.ok) {
                val motivo = when (syncResult.httpCode) {
                    0 -> "Sem conexão com a internet. Verifique e tente novamente."
                    401 -> "Versão do app não autorizada neste serviço. Atualize o Nanei."
                    403 -> "Este evento foi publicado por outro aparelho e não pode ser alterado daqui."
                    else -> "O servidor respondeu com erro (HTTP ${syncResult.httpCode}). Tente novamente em instantes."
                }
                push(SyncTestLog(
                    stepName = "2. Publicação da página no site",
                    latencyMs = syncResult.latencyMs,
                    isSuccess = false,
                    details = motivo
                ))
                _isShowerSyncing.value = false
                return@launch
            }
            push(SyncTestLog(
                stepName = "2. Publicação da página no site",
                latencyMs = syncResult.latencyMs,
                isSuccess = true,
                details = "Página publicada em ${syncResult.publicUrl}"
            ))

            // Guarda a chave de edição e o slug definitivo devolvidos pelo servidor
            if (!syncResult.editKey.isNullOrBlank()) {
                BabyShowerStore.setEditKey(getApplication(), syncResult.editKey)
            }
            val slugFinal = syncResult.slug ?: event.effectiveSlug

            // 3. Baixa RSVPs e reservas feitos na página pública
            val manage = NaneiApi.fetchManage(slugFinal, BabyShowerStore.getEditKey(getApplication()))
            if (manage.ok) {
                val localIds = _babyShowerGuests.value.map { it.id }.toSet()
                val webNews = manage.guests.filter { it.id !in localIds }
                if (webNews.isNotEmpty()) {
                    _babyShowerGuests.value = _babyShowerGuests.value + webNews
                }
                val serverGifts = manage.gifts.associateBy { it.id }
                _babyShowerGifts.value = _babyShowerGifts.value.map { local ->
                    val remote = serverGifts[local.id]
                    if (remote != null && remote.isReserved && !local.isReserved) {
                        local.copy(isReserved = true, reservedByGuestName = remote.reservedByGuestName)
                    } else local
                }
                push(SyncTestLog(
                    stepName = "3. Confirmações e reservas da página",
                    latencyMs = manage.latencyMs,
                    isSuccess = true,
                    details = if (webNews.isNotEmpty())
                        "${webNews.size} nova(s) confirmação(ões) recebida(s) do site 🎉"
                    else
                        "Nenhuma novidade — confirmações e presentes em dia"
                ))
            } else {
                push(SyncTestLog(
                    stepName = "3. Confirmações e reservas da página",
                    latencyMs = manage.latencyMs,
                    isSuccess = false,
                    details = "Publicado, mas não foi possível baixar as confirmações (HTTP ${manage.httpCode}). Sincronize de novo."
                ))
            }

            _babyShowerEvent.value = _babyShowerEvent.value.copy(
                customSlug = slugFinal,
                isSyncedWithWeb = true,
                lastSyncedAtMs = System.currentTimeMillis()
            )
            persistShower()
            _isShowerSyncing.value = false
        }
    }
}

enum class NaneiDestination(val title: String, val iconName: String) {
    HOME("Início", "ic_home"),
    BABY_SHOWER("Chá & Eventos", "ic_event"),
    HEALTH("Saúde", "ic_health"),
    MOM_JOURNAL("Gestação & Diário", "ic_journal"),
    AI_ASSISTANT("IA Nanei", "ic_assistant")
}

