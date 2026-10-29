package com.example.data.local

import android.content.Context
import android.util.Log
import com.example.data.model.ContractionEntry
import com.example.data.model.KickSession
import com.example.data.model.MomJournalEntry
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistência local do módulo Gestação & Diário da Mamãe.
 *
 * Antes da v6 todos os dados da gestante (DPP, contagem de chutes,
 * contrações, diário e checklists) viviam apenas em memória e se
 * perdiam ao fechar o app. Agora são gravados em SharedPreferences
 * (JSON) a cada alteração e recarregados na abertura.
 */
object PregnancyStore {

    private const val TAG = "PregnancyStore"
    private const val PREFS = "nanei_pregnancy"
    private const val KEY_DUE_DATE = "due_date_ms"
    private const val KEY_KICKS = "kick_sessions_json"
    private const val KEY_CONTRACTIONS = "contractions_json"
    private const val KEY_JOURNAL = "journal_json"
    private const val KEY_BAG_CHECKED = "bag_checked_ids"
    private const val KEY_EXAMS_DONE = "exams_done_ids"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /* ------------------------------------------------------------ DPP */

    /** Data provável do parto em ms; 0L = gestação não registrada. */
    fun getDueDateMs(context: Context): Long = prefs(context).getLong(KEY_DUE_DATE, 0L)

    fun setDueDateMs(context: Context, dueDateMs: Long) {
        prefs(context).edit().putLong(KEY_DUE_DATE, dueDateMs).apply()
    }

    fun clearPregnancy(context: Context) {
        prefs(context).edit()
            .remove(KEY_DUE_DATE)
            .remove(KEY_KICKS)
            .remove(KEY_CONTRACTIONS)
            .apply()
    }

    /* ---------------------------------------------------- chutes */

    fun loadKickSessions(context: Context): List<KickSession> = try {
        val arr = JSONArray(prefs(context).getString(KEY_KICKS, "[]") ?: "[]")
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            KickSession(
                id = o.optString("id"),
                timestampMs = o.optLong("timestampMs"),
                kickCount = o.optInt("kickCount"),
                durationSeconds = o.optLong("durationSeconds")
            )
        }
    } catch (e: Exception) {
        Log.e(TAG, "Falha ao carregar contagem de chutes", e)
        emptyList()
    }

    fun saveKickSessions(context: Context, sessions: List<KickSession>) {
        try {
            val arr = JSONArray()
            sessions.forEach { s ->
                arr.put(JSONObject().apply {
                    put("id", s.id)
                    put("timestampMs", s.timestampMs)
                    put("kickCount", s.kickCount)
                    put("durationSeconds", s.durationSeconds)
                })
            }
            prefs(context).edit().putString(KEY_KICKS, arr.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao salvar contagem de chutes", e)
        }
    }

    /* ---------------------------------------------------- contrações */

    fun loadContractions(context: Context): List<ContractionEntry> = try {
        val arr = JSONArray(prefs(context).getString(KEY_CONTRACTIONS, "[]") ?: "[]")
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            ContractionEntry(
                id = o.optString("id"),
                timestampMs = o.optLong("timestampMs"),
                durationSeconds = o.optInt("durationSeconds"),
                intervalSeconds = o.optInt("intervalSeconds")
            )
        }
    } catch (e: Exception) {
        Log.e(TAG, "Falha ao carregar contrações", e)
        emptyList()
    }

    fun saveContractions(context: Context, entries: List<ContractionEntry>) {
        try {
            val arr = JSONArray()
            entries.forEach { c ->
                arr.put(JSONObject().apply {
                    put("id", c.id)
                    put("timestampMs", c.timestampMs)
                    put("durationSeconds", c.durationSeconds)
                    put("intervalSeconds", c.intervalSeconds)
                })
            }
            prefs(context).edit().putString(KEY_CONTRACTIONS, arr.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao salvar contrações", e)
        }
    }

    /* ---------------------------------------------------- diário da mamãe */

    fun loadJournalEntries(context: Context): List<MomJournalEntry> = try {
        val arr = JSONArray(prefs(context).getString(KEY_JOURNAL, "[]") ?: "[]")
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            MomJournalEntry(
                id = o.optString("id"),
                title = o.optString("title"),
                dateMs = o.optLong("dateMs"),
                gestationalWeek = if (o.has("gestationalWeek")) o.optInt("gestationalWeek") else null,
                category = o.optString("category", "Pensamentos 💭"),
                notes = o.optString("notes"),
                moodEmoji = o.optString("moodEmoji", "🥰"),
                photoUrl = o.optString("photoUrl").ifBlank { null }
            )
        }
    } catch (e: Exception) {
        Log.e(TAG, "Falha ao carregar diário da mamãe", e)
        emptyList()
    }

    fun saveJournalEntries(context: Context, entries: List<MomJournalEntry>) {
        try {
            val arr = JSONArray()
            entries.forEach { e ->
                arr.put(JSONObject().apply {
                    put("id", e.id)
                    put("title", e.title)
                    put("dateMs", e.dateMs)
                    if (e.gestationalWeek != null) put("gestationalWeek", e.gestationalWeek)
                    put("category", e.category)
                    put("notes", e.notes)
                    put("moodEmoji", e.moodEmoji)
                    put("photoUrl", e.photoUrl ?: "")
                })
            }
            prefs(context).edit().putString(KEY_JOURNAL, arr.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao salvar diário da mamãe", e)
        }
    }

    /* ---------------------------------------- checklists (mala/exames) */

    fun loadCheckedIds(context: Context, bag: Boolean): Set<String> {
        val key = if (bag) KEY_BAG_CHECKED else KEY_EXAMS_DONE
        return prefs(context).getStringSet(key, emptySet()) ?: emptySet()
    }

    fun saveCheckedIds(context: Context, ids: Set<String>, bag: Boolean) {
        val key = if (bag) KEY_BAG_CHECKED else KEY_EXAMS_DONE
        prefs(context).edit().putStringSet(key, ids).apply()
    }
}
