package com.example.data.model

import java.util.UUID

enum class BabyShowerThemeStyle(
    val title: String,
    val bgHex: Long,
    val textHex: Long,
    val accentHex: Long,
    val emoji: String
) {
    ROSA_SOFT("Rosa Soft Floral", 0xFFFFF1F2, 0xFF881337, 0xFFFB7185, "🌸"),
    DOURADO_REAL("Dourado & Nude", 0xFFFEFCE8, 0xFF713F12, 0xFFEAB308, "✨"),
    BOHEMIA_PASTEL("Boêmio Pastel", 0xFFFDF6F0, 0xFF4A3E3D, 0xFFD97706, "🌾"),
    AZUL_CELESTE("Azul Celeste Baby", 0xFFF0F9FF, 0xFF0C4A6E, 0xFF38BDF8, "🍼"),
    NOITE_LAVANDA("Noite de Lavanda", 0xFFFAF5FF, 0xFF581C87, 0xFFA855F7, "🌙")
}

data class BabyShowerEvent(
    val id: String = UUID.randomUUID().toString(),
    val customSlug: String = "",
    val title: String = "",
    val parentsNames: String = "",
    val eventDateMs: Long = System.currentTimeMillis() + (30L * 24 * 3600 * 1000), // 30 days ahead
    val eventTimeStr: String = "15:00",
    val locationName: String = "",
    val address: String = "",
    val mapsUrl: String = "",
    val themeStyle: BabyShowerThemeStyle = BabyShowerThemeStyle.ROSA_SOFT,
    val invitationMessage: String = "",
    val isOnlineEvent: Boolean = false,
    val onlineMeetingUrl: String? = null,
    val isSyncedWithWeb: Boolean = false,
    val lastSyncedAtMs: Long = 0L
) {
    /**
     * Slug efetivo usado na URL pública. Se a mãe não escolheu um, o servidor
     * deriva um a partir do id do evento no primeiro sync — e o app grava o
     * valor devolvido aqui.
     */
    val effectiveSlug: String
        get() = customSlug.ifBlank { "cha-" + id.filter { it.isLetterOrDigit() }.lowercase().take(8) }

    val fullWebUrl: String
        get() = "https://www.nanei.com.br/cha-de-bebe/$effectiveSlug"
}

enum class RsvpStatus(val label: String, val colorHex: Long) {
    CONFIRMED("Confirmado", 0xFF166534),
    PENDING("Pendente", 0xFF854D0E),
    DECLINED("Recusado", 0xFF991B1B)
}

data class BabyShowerGuest(
    val id: String = UUID.randomUUID().toString(),
    val eventId: String,
    val name: String,
    val phoneOrEmail: String = "",
    val status: RsvpStatus = RsvpStatus.CONFIRMED,
    val adultsCount: Int = 1,
    val childrenCount: Int = 0,
    val dietaryRestrictions: String? = null,
    val assignedGiftTitle: String? = null,
    val rsvpTimestampMs: Long = System.currentTimeMillis(),
    val source: String = "app" // "app" (cadastrado pela mãe) ou "web" (RSVP na página pública)
)

data class BabyShowerGift(
    val id: String = UUID.randomUUID().toString(),
    val eventId: String,
    val title: String,
    val category: String = "Fraldas", // "Fraldas", "Roupas", "Higiene", "Móveis & Acessórios", "Cota em R$"
    val isReserved: Boolean = false,
    val reservedByGuestName: String? = null,
    val externalStoreUrl: String? = null,
    val priceEstimate: Double? = null
)

data class SyncTestLog(
    val id: String = UUID.randomUUID().toString(),
    val timestampMs: Long = System.currentTimeMillis(),
    val endpoint: String = "nanei.com.br/api/v1/events/sync",
    val stepName: String,
    val latencyMs: Long,
    val isSuccess: Boolean,
    val details: String
)
