package com.example.api

import android.util.Log
import com.example.data.model.BabyShowerEvent
import com.example.data.model.BabyShowerGift
import com.example.data.model.BabyShowerGuest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Cliente da API pública do Nanei (www.nanei.com.br).
 *
 * - syncShower: publica/atualiza a página do chá de bebê no site
 * - fetchManage: baixa RSVPs e reservas feitos na página pública
 * - aiProxy: proxy de IA (a chave do Gemini fica só no servidor)
 *
 * Autenticação: header X-Nanei-Key (chave compartilhada do app; barreira
 * anti-spam — o servidor também aplica rate limit por IP).
 */
object NaneiApi {

    private const val TAG = "NaneiApi"
    private const val BASE = "https://www.nanei.com.br"
    private const val APP_KEY = "2b1139cee93943accc6e77f579603d5ac318efe969ad7e10"
    private const val CONNECT_TIMEOUT = 15000
    private const val READ_TIMEOUT = 30000

    data class SyncResult(
        val ok: Boolean,
        val httpCode: Int,
        val latencyMs: Long,
        val slug: String? = null,
        val publicUrl: String? = null,
        val editKey: String? = null,
        val error: String? = null
    )

    data class ManageResult(
        val ok: Boolean,
        val httpCode: Int,
        val latencyMs: Long,
        val guests: List<BabyShowerGuest> = emptyList(),
        val gifts: List<BabyShowerGift> = emptyList(),
        val error: String? = null
    )

    private fun openConnection(url: String, method: String): HttpURLConnection {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.setRequestProperty("X-Nanei-Key", APP_KEY)
        conn.setRequestProperty("Accept", "application/json")
        conn.connectTimeout = CONNECT_TIMEOUT
        conn.readTimeout = READ_TIMEOUT
        return conn
    }

    private fun readBody(conn: HttpURLConnection): String {
        val stream = try {
            conn.inputStream
        } catch (e: Exception) {
            conn.errorStream
        } ?: return ""
        val reader = BufferedReader(InputStreamReader(stream))
        val sb = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            sb.append(line)
        }
        reader.close()
        return sb.toString()
    }

    /**
     * Publica o evento no site. Se [editKey] estiver vazia (primeiro sync),
     * o servidor gera uma e devolve — o caller deve gravá-la.
     */
    suspend fun syncShower(
        event: BabyShowerEvent,
        guests: List<BabyShowerGuest>,
        gifts: List<BabyShowerGift>,
        editKey: String
    ): SyncResult = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val body = JSONObject().apply {
                put("event", com.example.data.local.BabyShowerStore.eventToJson(event))
                put("editKey", editKey)
                put("guests", org.json.JSONArray().also { arr ->
                    guests.forEach { arr.put(com.example.data.local.BabyShowerStore.guestToJson(it)) }
                })
                put("gifts", org.json.JSONArray().also { arr ->
                    gifts.forEach { arr.put(com.example.data.local.BabyShowerStore.giftToJson(it)) }
                })
            }

            val conn = openConnection("$BASE/api/v1/events/sync", "POST")
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.doOutput = true
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body.toString()) }

            val code = conn.responseCode
            val latency = System.currentTimeMillis() - start
            val responseText = readBody(conn)
            conn.disconnect()

            val json = try { JSONObject(responseText) } catch (e: Exception) { null }
            if (code == 200 && json?.optBoolean("ok") == true) {
                SyncResult(
                    ok = true,
                    httpCode = code,
                    latencyMs = latency,
                    slug = json.optString("slug"),
                    publicUrl = json.optString("publicUrl"),
                    editKey = json.optString("editKey")
                )
            } else {
                SyncResult(
                    ok = false,
                    httpCode = code,
                    latencyMs = latency,
                    error = json?.optString("error")?.ifBlank { null } ?: "http_$code"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "syncShower falhou", e)
            SyncResult(
                ok = false,
                httpCode = 0,
                latencyMs = System.currentTimeMillis() - start,
                error = "network: ${e.javaClass.simpleName}"
            )
        }
    }

    /** Baixa RSVPs e reservas feitos na página pública. */
    suspend fun fetchManage(slug: String, editKey: String): ManageResult = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val url = "$BASE/api/v1/events/manage?slug=${URLEncoder.encode(slug, "UTF-8")}" +
                "&key=${URLEncoder.encode(editKey, "UTF-8")}"
            val conn = openConnection(url, "GET")
            val code = conn.responseCode
            val latency = System.currentTimeMillis() - start
            val responseText = readBody(conn)
            conn.disconnect()

            val json = try { JSONObject(responseText) } catch (e: Exception) { null }
            if (code != 200 || json?.optBoolean("ok") != true) {
                return@withContext ManageResult(
                    ok = false,
                    httpCode = code,
                    latencyMs = latency,
                    error = json?.optString("error")?.ifBlank { null } ?: "http_$code"
                )
            }

            val eventId = json.optJSONObject("event")?.optString("id") ?: ""
            val guests = mutableListOf<BabyShowerGuest>()
            val guestsArr = json.optJSONArray("guests")
            if (guestsArr != null) {
                for (i in 0 until guestsArr.length()) {
                    guests.add(com.example.data.local.BabyShowerStore.guestFromJson(guestsArr.getJSONObject(i), eventId))
                }
            }
            val gifts = mutableListOf<BabyShowerGift>()
            val giftsArr = json.optJSONArray("gifts")
            if (giftsArr != null) {
                for (i in 0 until giftsArr.length()) {
                    gifts.add(com.example.data.local.BabyShowerStore.giftFromJson(giftsArr.getJSONObject(i), eventId))
                }
            }
            ManageResult(ok = true, httpCode = code, latencyMs = latency, guests = guests, gifts = gifts)
        } catch (e: Exception) {
            Log.e(TAG, "fetchManage falhou", e)
            ManageResult(
                ok = false,
                httpCode = 0,
                latencyMs = System.currentTimeMillis() - start,
                error = "network: ${e.javaClass.simpleName}"
            )
        }
    }

    /**
     * Chama o proxy de IA no servidor. Lança exceção em qualquer falha —
     * o caller (GeminiClient) cai no modo offline local.
     */
    suspend fun aiProxy(prompt: String): String = withContext(Dispatchers.IO) {
        val conn = openConnection("$BASE/api/ia", "POST")
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        conn.doOutput = true
        val body = JSONObject().put("prompt", prompt)
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body.toString()) }

        val code = conn.responseCode
        val responseText = readBody(conn)
        conn.disconnect()

        if (code != 200) {
            Log.e(TAG, "aiProxy HTTP $code: $responseText")
            throw RuntimeException("IA proxy erro $code")
        }
        val json = JSONObject(responseText)
        val text = json.optString("text", "")
        if (text.isBlank()) throw RuntimeException("IA proxy resposta vazia")
        text
    }
}
