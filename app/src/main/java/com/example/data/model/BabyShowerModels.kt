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
    val customSlug: String = "gabriel-2026",
    val title: String = "Chá de Bebê do Gabriel 🍼",
    val parentsNames: String = "Mamãe Juliana & Papai Ricardo",
    val eventDateMs: Long = System.currentTimeMillis() + (30L * 24 * 3600 * 1000), // 30 days ahead
    val eventTimeStr: String = "15:00",
    val locationName: String = "Espaço Villa Baby",
    val address: String = "Rua das Flores, 123 - Jardins, São Paulo",
    val mapsUrl: String = "https://maps.google.com/?q=Espaco+Villa+Baby+SP",
    val themeStyle: BabyShowerThemeStyle = BabyShowerThemeStyle.ROSA_SOFT,
    val invitationMessage: String = "Querida família e amigos, venham comemorar com a gente a doce espera e abençoar a chegada do nosso Gabriel!",
    val isOnlineEvent: Boolean = false,
    val onlineMeetingUrl: String? = null,
    val isSyncedWithWeb: Boolean = true,
    val lastSyncedAtMs: Long = System.currentTimeMillis(),
    val baseUrl: String = "https://nanei.com.br/cha-de-bebe/",
    val webhookUrl: String = "https://nanei.com.br/api/v1/webhooks/rsvp",
    val apiKey: String = "nan_live_9f83a21bc789e02d4",
    val integrationType: String = "Sincronização Direta REST & Webhook"
) {
    val fullWebUrl: String
        get() = if (baseUrl.endsWith("/")) "$baseUrl$customSlug" else "$baseUrl/$customSlug"
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
    val rsvpTimestampMs: Long = System.currentTimeMillis()
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
