package com.example.data.repository

import com.example.api.GeminiClient
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.provider.NaneiStaticData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Calendar

class NaneiRepository(private val db: NaneiDatabase) {

    private val babyDao = db.babyDao()
    private val eventDao = db.eventDao()
    private val milestoneDao = db.milestoneDao()
    private val reminderDao = db.reminderDao()
    private val auditLogDao = db.auditLogDao()

    val allBabies: Flow<List<Baby>> = babyDao.getAllBabies()
    val selectedBaby: Flow<Baby?> = babyDao.getSelectedBaby()

    // --- Baby Operations ---
    suspend fun createInitialBabyIfNone(): Baby {
        val currentBabies = allBabies.first()
        if (currentBabies.isNotEmpty()) {
            val selected = currentBabies.find { it.isSelected } ?: currentBabies.first()
            return selected
        }

        // Create default initial baby
        val now = Calendar.getInstance()
        val birthCalendar = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
        }
        val estimatedCalendar = Calendar.getInstance().apply {
            timeInMillis = birthCalendar.timeInMillis
        }

        val defaultBaby = Baby(
            name = "Meu Bebê",
            birthDateMs = birthCalendar.timeInMillis,
            estimatedDueDateMs = estimatedCalendar.timeInMillis,
            gender = "UNKNOWN",
            isSelected = true
        )

        val babyId = babyDao.insertBaby(defaultBaby)

        // Populate initial milestones
        val initialMilestones = NaneiStaticData.getStandardMilestones(babyId)
        milestoneDao.insertMilestones(initialMilestones)

        // Log audit
        auditLogDao.insertAuditLog(
            AuditLog(
                action = "BABY_CREATED",
                details = "Bebê inicial criado"
            )
        )

        return defaultBaby.copy(id = babyId)
    }

    suspend fun switchSelectedBaby(babyId: Long) {
        babyDao.clearSelectedBabies()
        babyDao.setSelectedBaby(babyId)
        auditLogDao.insertAuditLog(AuditLog(action = "SWITCH_BABY", details = "Alternou para o bebê ID $babyId"))
    }

    suspend fun addNewBaby(name: String, birthDateMs: Long, estimatedDueDateMs: Long, gender: String, avatarUri: String? = null): Long {
        babyDao.clearSelectedBabies()
        val newBaby = Baby(
            name = name,
            birthDateMs = birthDateMs,
            estimatedDueDateMs = estimatedDueDateMs,
            gender = gender,
            avatarUri = avatarUri,
            isSelected = true
        )
        val babyId = babyDao.insertBaby(newBaby)
        milestoneDao.insertMilestones(NaneiStaticData.getStandardMilestones(babyId))
        auditLogDao.insertAuditLog(AuditLog(action = "CREATE_BABY", details = "Bebê cadastrado: $name"))
        return babyId
    }

    // --- Events Operations ---
    fun getEventsForBaby(babyId: Long): Flow<List<Event>> = eventDao.getEventsForBaby(babyId)

    suspend fun logEvent(event: Event): Long {
        val id = eventDao.insertEvent(event)
        auditLogDao.insertAuditLog(
            AuditLog(
                action = "LOG_EVENT",
                details = "Evento do tipo ${event.type.displayName} registrado por ${event.createdBy}"
            )
        )
        return id
    }

    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event)
        auditLogDao.insertAuditLog(AuditLog(action = "DELETE_EVENT", details = "Evento ID ${event.id} removido"))
    }

    // --- Milestones Operations ---
    fun getMilestonesForBaby(babyId: Long): Flow<List<Milestone>> = milestoneDao.getMilestonesForBaby(babyId)

    suspend fun toggleMilestone(milestone: Milestone) {
        val updated = milestone.copy(
            isAchieved = !milestone.isAchieved,
            achievedAtMs = if (!milestone.isAchieved) System.currentTimeMillis() else null
        )
        milestoneDao.updateMilestone(updated)
        auditLogDao.insertAuditLog(
            AuditLog(
                action = "TOGGLE_MILESTONE",
                details = "Marco '${milestone.title}' marcado como ${if (updated.isAchieved) "atingido" else "não atingido"}"
            )
        )
    }

    // --- Reminders Operations ---
    fun getRemindersForBaby(babyId: Long): Flow<List<Reminder>> = reminderDao.getRemindersForBaby(babyId)

    suspend fun saveReminder(reminder: Reminder) {
        if (reminder.id == 0L) {
            reminderDao.insertReminder(reminder)
        } else {
            reminderDao.updateReminder(reminder)
        }
    }

    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }

    // --- Audit Logs (LGPD) ---
    fun getAuditLogs(): Flow<List<AuditLog>> = auditLogDao.getAllAuditLogs()

    suspend fun purgeAllDataForBaby(babyId: Long) {
        eventDao.deleteAllEventsForBaby(babyId)
        babyDao.deleteBaby(babyId)
        auditLogDao.insertAuditLog(AuditLog(action = "PURGE_BABY_DATA", details = "Dados do bebê $babyId excluídos totalmente"))
    }

    // --- Bulk Database Backup & Restore (Premium Cloud Sync) ---
    suspend fun getAllBabiesSync(): List<Baby> = babyDao.getAllBabiesSync()
    suspend fun getAllEventsSync(): List<Event> = eventDao.getAllEventsSync()
    suspend fun getAllMilestonesSync(): List<Milestone> = milestoneDao.getAllMilestonesSync()
    suspend fun getAllRemindersSync(): List<Reminder> = reminderDao.getAllRemindersSync()

    suspend fun restoreAllEntitiesSync(
        babies: List<Baby>,
        events: List<Event>,
        milestones: List<Milestone>,
        reminders: List<Reminder>
    ) {
        if (babies.isNotEmpty()) {
            babyDao.deleteAllBabies()
            babyDao.insertBabies(babies)
            val selected = babies.find { it.isSelected } ?: babies.first()
            babyDao.setSelectedBaby(selected.id)
        }
        if (events.isNotEmpty()) {
            eventDao.deleteAllEvents()
            eventDao.insertEvents(events)
        }
        if (milestones.isNotEmpty()) {
            milestoneDao.deleteAllMilestones()
            milestoneDao.insertMilestones(milestones)
        }
        if (reminders.isNotEmpty()) {
            reminderDao.deleteAllReminders()
            reminderDao.insertReminders(reminders)
        }
        auditLogDao.insertAuditLog(
            AuditLog(
                action = "RESTORE_CLOUD_BACKUP",
                details = "Restaurados ${babies.size} bebês, ${events.size} eventos, ${milestones.size} marcos e ${reminders.size} lembretes."
            )
        )
    }

    // --- SweetSpot Sleep Algorithm (SLP) ---
    suspend fun calculateSweetSpot(baby: Baby): SweetSpotPrediction {
        val ageMs = System.currentTimeMillis() - baby.birthDateMs
        val ageDays = (ageMs / (1000 * 60 * 60 * 24)).toInt()
        val ageMonths = ageDays / 30

        // Determine wake window range based on age
        val (minWakeMin, maxWakeMin) = when {
            ageDays < 90 -> Pair(60, 90) // 0-3 months
            ageMonths < 6 -> Pair(90, 150) // 3-6 months
            ageMonths < 12 -> Pair(150, 210) // 6-12 months
            ageMonths < 18 -> Pair(180, 240) // 12-18 months
            else -> Pair(240, 360) // 18+ months
        }

        val lastSleepEvent = eventDao.getLastEventOfType(baby.id, EventType.SLEEP)
        val lastSleepEndTimeMs = lastSleepEvent?.endTimeMs ?: lastSleepEvent?.startTimeMs ?: (System.currentTimeMillis() - (minWakeMin * 60 * 1000L))

        val targetWindowStartMs = lastSleepEndTimeMs + (minWakeMin * 60 * 1000L)
        val targetWindowEndMs = lastSleepEndTimeMs + (maxWakeMin * 60 * 1000L)
        val now = System.currentTimeMillis()

        val minutesUntilWindow = ((targetWindowStartMs - now) / (1000 * 60))
        val isWindowActiveNow = now in targetWindowStartMs..targetWindowEndMs

        val statusText = when {
            isWindowActiveNow -> "Janela SweetSpot Ativa! Momento ideal para desacelerar e iniciar o ritual de sono."
            minutesUntilWindow > 0 -> "Próxima janela de soneca prevista para começar em ${minutesUntilWindow} minutos."
            else -> "O bebê está em vigília há bastante tempo. Fique atento a sinais de cansaço (coçar os olhos, bocejo)."
        }

        return SweetSpotPrediction(
            recommendedStartTimeMs = targetWindowStartMs,
            recommendedEndTimeMs = targetWindowEndMs,
            minWakeWindowMinutes = minWakeMin,
            maxWakeWindowMinutes = maxWakeMin,
            minutesUntilWindow = minutesUntilWindow,
            statusText = statusText,
            isWindowActiveNow = isWindowActiveNow
        )
    }

    // --- Gemini AI Assistant Integration ---
    suspend fun parseVoiceOrTextWithGemini(babyId: Long, text: String): List<Event> {
        return GeminiClient.parseVoiceInputToEvents(babyId, text)
    }

    suspend fun askGeminiQuestion(baby: Baby, query: String): String {
        val recentEvents = eventDao.getEventsForBaby(baby.id).firstOrNull() ?: emptyList()
        val summaryList = recentEvents.take(10).joinToString("\n") { ev ->
            val dateStr = java.text.SimpleDateFormat("HH:mm - dd/MM", java.util.Locale.getDefault()).format(ev.startTimeMs)
            "- [$dateStr] ${ev.type.displayName}: ${ev.notes ?: ev.diaperType ?: ev.side ?: ""}"
        }
        val ageWeeks = ((System.currentTimeMillis() - baby.birthDateMs) / (1000 * 60 * 60 * 24 * 7)).toInt()
        
        return GeminiClient.queryBabyHistory(
            babyName = baby.name,
            babyAgeWeeks = ageWeeks,
            recentEventsSummary = if (summaryList.isBlank()) "Nenhum evento registrado ainda hoje." else summaryList,
            userQuery = query
        )
    }

    // --- Private Helper to Seed Initial Live Data ---
    private suspend fun seedInitialSampleEvents(babyId: Long) {
        // No fictitious data seeded for production launch
    }
}
