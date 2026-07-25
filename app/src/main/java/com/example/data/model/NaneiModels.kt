package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// --- Baby Entity ---
@Entity(tableName = "babies")
data class Baby(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val birthDateMs: Long,
    val estimatedDueDateMs: Long, // Essential for Leap calculation
    val gender: String = "OTHER", // "MALE", "FEMALE", "OTHER"
    val avatarUri: String? = null,
    val isSelected: Boolean = false
)

// --- Event Types ---
enum class EventType(val displayName: String, val iconResName: String) {
    BREASTFEEDING("Amamentação", "ic_breastfeeding"),
    BOTTLE("Mamadeira", "ic_bottle"),
    SOLIDS("Alimentação Sólida", "ic_solids"),
    PUMPING("Ordem/Bombeamento", "ic_pumping"),
    DIAPER("Fralda", "ic_diaper"),
    SLEEP("Sono", "ic_sleep"),
    BATH("Banho e Higiene", "ic_bath"),
    MEDICINE("Medicamento", "ic_medicine"),
    VACCINE("Vacina", "ic_vaccine"),
    TEMPERATURE("Temperatura", "ic_temperature"),
    GROWTH("Crescimento", "ic_growth"),
    MOOD("Humor/Sintomas", "ic_mood"),
    NOTE("Nota Livre", "ic_note")
}

// --- Event Entity ---
@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long,
    val type: EventType,
    val startTimeMs: Long,
    val endTimeMs: Long? = null,
    // Flexible attributes
    val side: String? = null, // "LEFT", "RIGHT", "BOTH"
    val durationLeftSec: Int = 0,
    val durationRightSec: Int = 0,
    val volumeMl: Int? = null,
    val bottleType: String? = null, // "FORMULA", "EXPRESSED_MILK"
    val diaperType: String? = null, // "PEE", "POOP", "BOTH", "CLEAN"
    val diaperColor: String? = null,
    val bristolScale: Int? = null, // 1 to 7
    val sleepQuality: String? = null, // "CALM", "RESTLESS", "CRYING"
    val medicineName: String? = null,
    val dosage: String? = null,
    val vaccineName: String? = null,
    val temperatureCelsius: Double? = null,
    val weightKg: Double? = null,
    val heightCm: Double? = null,
    val headCircumferenceCm: Double? = null,
    val moodEmoji: String? = null,
    val notes: String? = null,
    val photoUri: String? = null,
    val createdBy: String = "Mamãe",
    val createdAtMs: Long = System.currentTimeMillis()
)

// --- Milestone Entity ---
@Entity(tableName = "milestones")
data class Milestone(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long,
    val category: String, // "MOTOR_GROSS", "MOTOR_FINE", "COGNITIVE", "SOCIAL", "LANGUAGE"
    val title: String,
    val description: String,
    val targetAgeMonths: Int,
    val isAchieved: Boolean = false,
    val achievedAtMs: Long? = null,
    val photoUri: String? = null,
    val note: String? = null
)

// --- Reminder Entity ---
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long,
    val title: String,
    val type: String, // "FEEDING", "MEDICINE", "PUMPING", "VACCINE", "NAP"
    val hour: Int,
    val minute: Int,
    val intervalHours: Int = 0,
    val isActive: Boolean = true
)

// --- AuditLog Entity (LGPD) ---
@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val details: String,
    val timestampMs: Long = System.currentTimeMillis()
)

// --- Drug Info model for Lactation Gateway ---
data class DrugInfo(
    val id: String,
    val genericName: String,
    val brandNames: List<String>,
    val category: String,
    val riskLevel: RiskLevel,
    val SummaryPt: String,
    val lactMedUrl: String,
    val eLactanciaUrl: String,
    val ministryOfHealthUrl: String = "https://www.gov.br/saude/pt-br"
)

enum class RiskLevel(val label: String, val colorHex: Long) {
    VERY_LOW("Muito Baixo Risco (Seguro)", 0xFF2E7D32),
    LOW("Baixo Risco (Geralmente seguro)", 0xFF43A047),
    MODERATE("Risco Moderado (Usar com cautela)", 0xFFFB8C00),
    HIGH("Alto Risco (Evitar/Consultar médico)", 0xFFE53935)
}

// --- Mental Leap Info Model ---
data class LeapInfo(
    val leapNumber: Int,
    val name: String,
    val startWeek: Int,
    val endWeek: Int,
    val description: String,
    val fussySigns: List<String>,
    val newAbilities: List<String>,
    val howToHelp: String,
    val isStormyPhase: Boolean
)

// --- Chat Message Model ---
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: Sender,
    val text: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val parsedEvents: List<Event>? = null, // Extracted events awaiting user confirmation
    val isPendingConfirmation: Boolean = false
)

enum class Sender {
    USER, AI, SYSTEM
}

// --- SweetSpot Prediction Model ---
data class SweetSpotPrediction(
    val recommendedStartTimeMs: Long,
    val recommendedEndTimeMs: Long,
    val minWakeWindowMinutes: Int,
    val maxWakeWindowMinutes: Int,
    val minutesUntilWindow: Long,
    val statusText: String,
    val isWindowActiveNow: Boolean
)

// --- Mom Journal Entry (Diário e Livro de Memórias) ---
data class MomJournalEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val dateMs: Long = System.currentTimeMillis(),
    val gestationalWeek: Int? = null,
    val category: String = "Ultrassom", // "Ultrassom 🖼️", "Foto Barriga 📸", "Exame 🩺", "Pensamentos 💭", "Carta ao Bebê ✉️"
    val notes: String,
    val moodEmoji: String = "🥰",
    val photoUrl: String? = null
)

// --- Pregnancy Kick Counter ---
data class KickSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestampMs: Long = System.currentTimeMillis(),
    val kickCount: Int,
    val durationSeconds: Long
)

// --- Pregnancy Contraction Entry ---
data class ContractionEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestampMs: Long = System.currentTimeMillis(),
    val durationSeconds: Int,
    val intervalSeconds: Int
)

// --- Hospital Bag Item ---
data class HospitalBagItem(
    val id: String,
    val category: String, // "Mãe", "Bebê", "Acompanhante", "Documentos"
    val title: String,
    val isChecked: Boolean = false
)

// --- Prenatal Exam ---
data class PrenatalExam(
    val id: String,
    val title: String,
    val weekRange: String,
    val isCompleted: Boolean = false,
    val notes: String = ""
)

// --- Pregnancy Week Info ---
data class PregnancyWeekInfo(
    val week: Int,
    val fruitComparison: String,
    val fruitEmoji: String,
    val babySizeCm: Double,
    val babyWeightGrams: Double,
    val description: String,
    val momSymptoms: String
)

