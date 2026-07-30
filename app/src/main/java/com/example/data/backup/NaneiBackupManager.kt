package com.example.data.backup

import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject

data class NaneiCloudBackupPayload(
    val version: Int = 1,
    val backupTimestampMs: Long = System.currentTimeMillis(),
    val userEmail: String,
    val babies: List<Baby> = emptyList(),
    val events: List<Event> = emptyList(),
    val milestones: List<Milestone> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val momJournalEntries: List<MomJournalEntry> = emptyList(),
    val kickSessions: List<KickSession> = emptyList(),
    val contractions: List<ContractionEntry> = emptyList()
)

object NaneiBackupManager {

    fun toJson(payload: NaneiCloudBackupPayload): String {
        val root = JSONObject()
        root.put("version", payload.version)
        root.put("backupTimestampMs", payload.backupTimestampMs)
        root.put("userEmail", payload.userEmail)

        // Babies
        val babiesArr = JSONArray()
        payload.babies.forEach { b ->
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("name", b.name)
            obj.put("birthDateMs", b.birthDateMs)
            obj.put("estimatedDueDateMs", b.estimatedDueDateMs)
            obj.put("gender", b.gender)
            if (b.avatarUri != null) obj.put("avatarUri", b.avatarUri)
            obj.put("isSelected", b.isSelected)
            babiesArr.put(obj)
        }
        root.put("babies", babiesArr)

        // Events
        val eventsArr = JSONArray()
        payload.events.forEach { e ->
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("babyId", e.babyId)
            obj.put("type", e.type.name)
            obj.put("startTimeMs", e.startTimeMs)
            if (e.endTimeMs != null) obj.put("endTimeMs", e.endTimeMs)
            if (e.side != null) obj.put("side", e.side)
            obj.put("durationLeftSec", e.durationLeftSec)
            obj.put("durationRightSec", e.durationRightSec)
            if (e.volumeMl != null) obj.put("volumeMl", e.volumeMl)
            if (e.bottleType != null) obj.put("bottleType", e.bottleType)
            if (e.diaperType != null) obj.put("diaperType", e.diaperType)
            if (e.diaperColor != null) obj.put("diaperColor", e.diaperColor)
            if (e.bristolScale != null) obj.put("bristolScale", e.bristolScale)
            if (e.sleepQuality != null) obj.put("sleepQuality", e.sleepQuality)
            if (e.medicineName != null) obj.put("medicineName", e.medicineName)
            if (e.dosage != null) obj.put("dosage", e.dosage)
            if (e.vaccineName != null) obj.put("vaccineName", e.vaccineName)
            if (e.temperatureCelsius != null) obj.put("temperatureCelsius", e.temperatureCelsius)
            if (e.weightKg != null) obj.put("weightKg", e.weightKg)
            if (e.heightCm != null) obj.put("heightCm", e.heightCm)
            if (e.headCircumferenceCm != null) obj.put("headCircumferenceCm", e.headCircumferenceCm)
            if (e.moodEmoji != null) obj.put("moodEmoji", e.moodEmoji)
            if (e.notes != null) obj.put("notes", e.notes)
            if (e.photoUri != null) obj.put("photoUri", e.photoUri)
            obj.put("createdBy", e.createdBy)
            obj.put("createdAtMs", e.createdAtMs)
            eventsArr.put(obj)
        }
        root.put("events", eventsArr)

        // Milestones
        val milestonesArr = JSONArray()
        payload.milestones.forEach { m ->
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("babyId", m.babyId)
            obj.put("category", m.category)
            obj.put("title", m.title)
            obj.put("description", m.description)
            obj.put("targetAgeMonths", m.targetAgeMonths)
            obj.put("isAchieved", m.isAchieved)
            if (m.achievedAtMs != null) obj.put("achievedAtMs", m.achievedAtMs)
            if (m.photoUri != null) obj.put("photoUri", m.photoUri)
            if (m.note != null) obj.put("note", m.note)
            milestonesArr.put(obj)
        }
        root.put("milestones", milestonesArr)

        // Reminders
        val remindersArr = JSONArray()
        payload.reminders.forEach { r ->
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("babyId", r.babyId)
            obj.put("title", r.title)
            obj.put("type", r.type)
            obj.put("hour", r.hour)
            obj.put("minute", r.minute)
            obj.put("intervalHours", r.intervalHours)
            obj.put("isActive", r.isActive)
            remindersArr.put(obj)
        }
        root.put("reminders", remindersArr)

        // Mom Journal
        val journalArr = JSONArray()
        payload.momJournalEntries.forEach { j ->
            val obj = JSONObject()
            obj.put("id", j.id)
            obj.put("title", j.title)
            obj.put("dateMs", j.dateMs)
            if (j.gestationalWeek != null) obj.put("gestationalWeek", j.gestationalWeek)
            obj.put("category", j.category)
            obj.put("notes", j.notes)
            obj.put("moodEmoji", j.moodEmoji)
            if (j.photoUrl != null) obj.put("photoUrl", j.photoUrl)
            journalArr.put(obj)
        }
        root.put("momJournalEntries", journalArr)

        // Kicks
        val kicksArr = JSONArray()
        payload.kickSessions.forEach { k ->
            val obj = JSONObject()
            obj.put("id", k.id)
            obj.put("timestampMs", k.timestampMs)
            obj.put("kickCount", k.kickCount)
            obj.put("durationSeconds", k.durationSeconds)
            kicksArr.put(obj)
        }
        root.put("kickSessions", kicksArr)

        // Contractions
        val contArr = JSONArray()
        payload.contractions.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("timestampMs", c.timestampMs)
            obj.put("durationSeconds", c.durationSeconds)
            obj.put("intervalSeconds", c.intervalSeconds)
            contArr.put(obj)
        }
        root.put("contractions", contArr)

        return root.toString(2)
    }

    fun fromJson(jsonStr: String): NaneiCloudBackupPayload? {
        return try {
            val root = JSONObject(jsonStr)
            val version = root.optInt("version", 1)
            val backupTimestampMs = root.optLong("backupTimestampMs", System.currentTimeMillis())
            val userEmail = root.optString("userEmail", "usuario@nanei.app")

            // Babies
            val babiesList = mutableListOf<Baby>()
            val babiesArr = root.optJSONArray("babies")
            if (babiesArr != null) {
                for (i in 0 until babiesArr.length()) {
                    val obj = babiesArr.getJSONObject(i)
                    babiesList.add(
                        Baby(
                            id = obj.optLong("id", 0L),
                            name = obj.optString("name", "Bebê"),
                            birthDateMs = obj.optLong("birthDateMs", System.currentTimeMillis()),
                            estimatedDueDateMs = obj.optLong("estimatedDueDateMs", System.currentTimeMillis()),
                            gender = obj.optString("gender", "OTHER"),
                            avatarUri = if (obj.has("avatarUri")) obj.getString("avatarUri") else null,
                            isSelected = obj.optBoolean("isSelected", i == 0)
                        )
                    )
                }
            }

            // Events
            val eventsList = mutableListOf<Event>()
            val eventsArr = root.optJSONArray("events")
            if (eventsArr != null) {
                for (i in 0 until eventsArr.length()) {
                    val obj = eventsArr.getJSONObject(i)
                    val typeName = obj.optString("type", EventType.NOTE.name)
                    val eventType = try { EventType.valueOf(typeName) } catch (e: Exception) { EventType.NOTE }
                    eventsList.add(
                        Event(
                            id = obj.optLong("id", 0L),
                            babyId = obj.optLong("babyId", 1L),
                            type = eventType,
                            startTimeMs = obj.optLong("startTimeMs", System.currentTimeMillis()),
                            endTimeMs = if (obj.has("endTimeMs")) obj.getLong("endTimeMs") else null,
                            side = if (obj.has("side")) obj.getString("side") else null,
                            durationLeftSec = obj.optInt("durationLeftSec", 0),
                            durationRightSec = obj.optInt("durationRightSec", 0),
                            volumeMl = if (obj.has("volumeMl")) obj.getInt("volumeMl") else null,
                            bottleType = if (obj.has("bottleType")) obj.getString("bottleType") else null,
                            diaperType = if (obj.has("diaperType")) obj.getString("diaperType") else null,
                            diaperColor = if (obj.has("diaperColor")) obj.getString("diaperColor") else null,
                            bristolScale = if (obj.has("bristolScale")) obj.getInt("bristolScale") else null,
                            sleepQuality = if (obj.has("sleepQuality")) obj.getString("sleepQuality") else null,
                            medicineName = if (obj.has("medicineName")) obj.getString("medicineName") else null,
                            dosage = if (obj.has("dosage")) obj.getString("dosage") else null,
                            vaccineName = if (obj.has("vaccineName")) obj.getString("vaccineName") else null,
                            temperatureCelsius = if (obj.has("temperatureCelsius")) obj.getDouble("temperatureCelsius") else null,
                            weightKg = if (obj.has("weightKg")) obj.getDouble("weightKg") else null,
                            heightCm = if (obj.has("heightCm")) obj.getDouble("heightCm") else null,
                            headCircumferenceCm = if (obj.has("headCircumferenceCm")) obj.getDouble("headCircumferenceCm") else null,
                            moodEmoji = if (obj.has("moodEmoji")) obj.getString("moodEmoji") else null,
                            notes = if (obj.has("notes")) obj.getString("notes") else null,
                            photoUri = if (obj.has("photoUri")) obj.getString("photoUri") else null,
                            createdBy = obj.optString("createdBy", "Mamãe"),
                            createdAtMs = obj.optLong("createdAtMs", System.currentTimeMillis())
                        )
                    )
                }
            }

            // Milestones
            val milestonesList = mutableListOf<Milestone>()
            val milestonesArr = root.optJSONArray("milestones")
            if (milestonesArr != null) {
                for (i in 0 until milestonesArr.length()) {
                    val obj = milestonesArr.getJSONObject(i)
                    milestonesList.add(
                        Milestone(
                            id = obj.optLong("id", 0L),
                            babyId = obj.optLong("babyId", 1L),
                            category = obj.optString("category", "MOTOR_GROSS"),
                            title = obj.optString("title", "Marco"),
                            description = obj.optString("description", ""),
                            targetAgeMonths = obj.optInt("targetAgeMonths", 1),
                            isAchieved = obj.optBoolean("isAchieved", false),
                            achievedAtMs = if (obj.has("achievedAtMs")) obj.getLong("achievedAtMs") else null,
                            photoUri = if (obj.has("photoUri")) obj.getString("photoUri") else null,
                            note = if (obj.has("note")) obj.getString("note") else null
                        )
                    )
                }
            }

            // Reminders
            val remindersList = mutableListOf<Reminder>()
            val remindersArr = root.optJSONArray("reminders")
            if (remindersArr != null) {
                for (i in 0 until remindersArr.length()) {
                    val obj = remindersArr.getJSONObject(i)
                    remindersList.add(
                        Reminder(
                            id = obj.optLong("id", 0L),
                            babyId = obj.optLong("babyId", 1L),
                            title = obj.optString("title", "Lembrete"),
                            type = obj.optString("type", "FEEDING"),
                            hour = obj.optInt("hour", 8),
                            minute = obj.optInt("minute", 0),
                            intervalHours = obj.optInt("intervalHours", 0),
                            isActive = obj.optBoolean("isActive", true)
                        )
                    )
                }
            }

            // Mom Journal
            val journalList = mutableListOf<MomJournalEntry>()
            val journalArr = root.optJSONArray("momJournalEntries")
            if (journalArr != null) {
                for (i in 0 until journalArr.length()) {
                    val obj = journalArr.getJSONObject(i)
                    journalList.add(
                        MomJournalEntry(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            title = obj.optString("title", "Diário"),
                            dateMs = obj.optLong("dateMs", System.currentTimeMillis()),
                            gestationalWeek = if (obj.has("gestationalWeek")) obj.getInt("gestationalWeek") else null,
                            category = obj.optString("category", "Ultrassom"),
                            notes = obj.optString("notes", ""),
                            moodEmoji = obj.optString("moodEmoji", "🥰"),
                            photoUrl = if (obj.has("photoUrl")) obj.getString("photoUrl") else null
                        )
                    )
                }
            }

            // Kicks
            val kicksList = mutableListOf<KickSession>()
            val kicksArr = root.optJSONArray("kickSessions")
            if (kicksArr != null) {
                for (i in 0 until kicksArr.length()) {
                    val obj = kicksArr.getJSONObject(i)
                    kicksList.add(
                        KickSession(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            timestampMs = obj.optLong("timestampMs", System.currentTimeMillis()),
                            kickCount = obj.optInt("kickCount", 10),
                            durationSeconds = obj.optLong("durationSeconds", 600L)
                        )
                    )
                }
            }

            // Contractions
            val contList = mutableListOf<ContractionEntry>()
            val contArr = root.optJSONArray("contractions")
            if (contArr != null) {
                for (i in 0 until contArr.length()) {
                    val obj = contArr.getJSONObject(i)
                    contList.add(
                        ContractionEntry(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            timestampMs = obj.optLong("timestampMs", System.currentTimeMillis()),
                            durationSeconds = obj.optInt("durationSeconds", 45),
                            intervalSeconds = obj.optInt("intervalSeconds", 300)
                        )
                    )
                }
            }

            NaneiCloudBackupPayload(
                version = version,
                backupTimestampMs = backupTimestampMs,
                userEmail = userEmail,
                babies = babiesList,
                events = eventsList,
                milestones = milestonesList,
                reminders = remindersList,
                momJournalEntries = journalList,
                kickSessions = kicksList,
                contractions = contList
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
