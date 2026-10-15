package com.example.data.local

import android.content.Context
import android.util.Log
import com.example.data.model.BabyShowerEvent
import com.example.data.model.BabyShowerGift
import com.example.data.model.BabyShowerGuest
import com.example.data.model.BabyShowerThemeStyle
import com.example.data.model.RsvpStatus
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistência local do módulo Chá de Bebê.
 *
 * Antes da v5 o módulo era 100% simulado em memória: os dados sumiam ao
 * fechar o app. Agora evento, convidados e presentes são gravados em
 * SharedPreferences (JSON) a cada alteração e recarregados na abertura.
 *
 * O escopo é um único evento por aparelho — SharedPreferences é suficiente
 * e evita criar tabelas Room para um módulo independente do restante do app.
 */
object BabyShowerStore {

    private const val TAG = "BabyShowerStore"
    private const val PREFS = "nanei_baby_shower"
    private const val KEY_EVENT = "event_json"
    private const val KEY_GUESTS = "guests_json"
    private const val KEY_GIFTS = "gifts_json"
    private const val KEY_EDIT_KEY = "edit_key"

    data class ShowerData(
        val event: BabyShowerEvent,
        val guests: List<BabyShowerGuest>,
        val gifts: List<BabyShowerGift>
    )

    fun load(context: Context): ShowerData? {
        return try {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val eventJson = prefs.getString(KEY_EVENT, null) ?: return null
            val event = eventFromJson(JSONObject(eventJson))
            val guests = mutableListOf<BabyShowerGuest>()
            val guestsArr = JSONArray(prefs.getString(KEY_GUESTS, "[]") ?: "[]")
            for (i in 0 until guestsArr.length()) {
                guests.add(guestFromJson(guestsArr.getJSONObject(i), event.id))
            }
            val gifts = mutableListOf<BabyShowerGift>()
            val giftsArr = JSONArray(prefs.getString(KEY_GIFTS, "[]") ?: "[]")
            for (i in 0 until giftsArr.length()) {
                gifts.add(giftFromJson(giftsArr.getJSONObject(i), event.id))
            }
            ShowerData(event, guests, gifts)
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao carregar chá de bebê salvo", e)
            null
        }
    }

    fun save(context: Context, event: BabyShowerEvent, guests: List<BabyShowerGuest>, gifts: List<BabyShowerGift>) {
        try {
            val guestsArr = JSONArray()
            guests.forEach { guestsArr.put(guestToJson(it)) }
            val giftsArr = JSONArray()
            gifts.forEach { giftsArr.put(giftToJson(it)) }
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_EVENT, eventToJson(event).toString())
                .putString(KEY_GUESTS, guestsArr.toString())
                .putString(KEY_GIFTS, giftsArr.toString())
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao salvar chá de bebê", e)
        }
    }

    /** Chave de edição do evento no servidor (criada no primeiro sync). */
    fun getEditKey(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_EDIT_KEY, "") ?: ""

    fun setEditKey(context: Context, key: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_EDIT_KEY, key)
            .apply()
    }

    /* ------------------------------------------------------------ evento */

    fun eventToJson(e: BabyShowerEvent): JSONObject = JSONObject().apply {
        put("id", e.id)
        put("customSlug", e.customSlug)
        put("title", e.title)
        put("parentsNames", e.parentsNames)
        put("eventDateMs", e.eventDateMs)
        put("eventTimeStr", e.eventTimeStr)
        put("locationName", e.locationName)
        put("address", e.address)
        put("mapsUrl", e.mapsUrl)
        put("themeStyle", e.themeStyle.name)
        put("invitationMessage", e.invitationMessage)
        put("isOnlineEvent", e.isOnlineEvent)
        put("onlineMeetingUrl", e.onlineMeetingUrl ?: "")
        put("isSyncedWithWeb", e.isSyncedWithWeb)
        put("lastSyncedAtMs", e.lastSyncedAtMs)
    }

    fun eventFromJson(o: JSONObject): BabyShowerEvent = BabyShowerEvent(
        id = o.optString("id"),
        customSlug = o.optString("customSlug"),
        title = o.optString("title"),
        parentsNames = o.optString("parentsNames"),
        eventDateMs = o.optLong("eventDateMs", System.currentTimeMillis() + 30L * 24 * 3600 * 1000),
        eventTimeStr = o.optString("eventTimeStr", "15:00"),
        locationName = o.optString("locationName"),
        address = o.optString("address"),
        mapsUrl = o.optString("mapsUrl"),
        themeStyle = try {
            BabyShowerThemeStyle.valueOf(o.optString("themeStyle"))
        } catch (e: Exception) {
            BabyShowerThemeStyle.ROSA_SOFT
        },
        invitationMessage = o.optString("invitationMessage"),
        isOnlineEvent = o.optBoolean("isOnlineEvent", false),
        onlineMeetingUrl = o.optString("onlineMeetingUrl").ifBlank { null },
        isSyncedWithWeb = o.optBoolean("isSyncedWithWeb", false),
        lastSyncedAtMs = o.optLong("lastSyncedAtMs", 0L)
    )

    /* ---------------------------------------------------------- convidado */

    fun guestToJson(g: BabyShowerGuest): JSONObject = JSONObject().apply {
        put("id", g.id)
        put("eventId", g.eventId)
        put("name", g.name)
        put("phoneOrEmail", g.phoneOrEmail)
        put("status", g.status.name)
        put("adultsCount", g.adultsCount)
        put("childrenCount", g.childrenCount)
        put("dietaryRestrictions", g.dietaryRestrictions ?: "")
        put("assignedGiftTitle", g.assignedGiftTitle ?: "")
        put("rsvpTimestampMs", g.rsvpTimestampMs)
        put("source", g.source)
    }

    fun guestFromJson(o: JSONObject, fallbackEventId: String): BabyShowerGuest = BabyShowerGuest(
        id = o.optString("id"),
        eventId = o.optString("eventId").ifBlank { fallbackEventId },
        name = o.optString("name"),
        phoneOrEmail = o.optString("phoneOrEmail"),
        status = try {
            RsvpStatus.valueOf(o.optString("status"))
        } catch (e: Exception) {
            RsvpStatus.PENDING
        },
        adultsCount = o.optInt("adultsCount", 1),
        childrenCount = o.optInt("childrenCount", 0),
        dietaryRestrictions = o.optString("dietaryRestrictions").ifBlank { null },
        assignedGiftTitle = o.optString("assignedGiftTitle").ifBlank { null },
        rsvpTimestampMs = o.optLong("rsvpTimestampMs", System.currentTimeMillis()),
        source = o.optString("source", "app")
    )

    /* ------------------------------------------------------------ presente */

    fun giftToJson(g: BabyShowerGift): JSONObject = JSONObject().apply {
        put("id", g.id)
        put("eventId", g.eventId)
        put("title", g.title)
        put("category", g.category)
        put("isReserved", g.isReserved)
        put("reservedByGuestName", g.reservedByGuestName ?: "")
        put("externalStoreUrl", g.externalStoreUrl ?: "")
        if (g.priceEstimate != null) put("priceEstimate", g.priceEstimate)
    }

    fun giftFromJson(o: JSONObject, fallbackEventId: String): BabyShowerGift = BabyShowerGift(
        id = o.optString("id"),
        eventId = o.optString("eventId").ifBlank { fallbackEventId },
        title = o.optString("title"),
        category = o.optString("category", "Fraldas"),
        isReserved = o.optBoolean("isReserved", false),
        reservedByGuestName = o.optString("reservedByGuestName").ifBlank { null },
        externalStoreUrl = o.optString("externalStoreUrl").ifBlank { null },
        priceEstimate = if (o.has("priceEstimate")) o.optDouble("priceEstimate") else null
    )
}
