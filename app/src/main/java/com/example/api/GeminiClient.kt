package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Event
import com.example.data.model.EventType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GeminiClient {

    private const val TAG = "GeminiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    /**
     * Checks if Gemini API Key is configured in BuildConfig.
     */
    fun isKeyConfigured(): Boolean {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        } catch (e: Exception) {
            false
        }
    }

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Parse natural language voice/text input into structured Event objects.
     * Example input: "Amamentei 15 min no esquerdo e depois troquei fralda com xixi às 8h"
     */
    suspend fun parseVoiceInputToEvents(babyId: Long, text: String): List<Event> = withContext(Dispatchers.IO) {
        if (!isKeyConfigured()) {
            // Fallback parsing if key is missing
            return@withContext fallbackParseText(babyId, text)
        }

        val prompt = """
            Você é o assistente do app Nanei para mães e bebês.
            Analise a mensagem em português e extraia TODOS os eventos mencionados.
            Retorne APENAS um JSON válido no formato de array de objetos. Não inclua markdown ```json.
            
            Tipos válidos de eventos:
            - BREASTFEEDING (campos: side ("LEFT", "RIGHT", "BOTH"), durationLeftSec, durationRightSec, notes)
            - BOTTLE (campos: volumeMl, bottleType ("FORMULA", "EXPRESSED_MILK"), notes)
            - DIAPER (campos: diaperType ("PEE", "POOP", "BOTH", "CLEAN"), notes)
            - SLEEP (campos: durationLeftSec (minutos*60), notes)
            - MEDICINE (campos: medicineName, dosage, notes)
            - TEMPERATURE (campos: temperatureCelsius, notes)
            - GROWTH (campos: weightKg, heightCm, headCircumferenceCm, notes)
            - NOTE (campos: notes)

            Mensagem da mãe/cuidador: "$text"
            
            Exemplo de saída JSON esperada:
            [
              {
                "type": "BREASTFEEDING",
                "side": "LEFT",
                "durationLeftSec": 900,
                "notes": "Amamentou bem no seio esquerdo"
              },
              {
                "type": "DIAPER",
                "diaperType": "POOP",
                "notes": "Fralda com cocô"
              }
            ]
        """.trimIndent()

        try {
            val responseText = callGeminiRestApi(prompt)
            parseEventsFromJsonResponse(babyId, responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Error in parseVoiceInputToEvents", e)
            fallbackParseText(babyId, text)
        }
    }

    /**
     * Query baby history in natural language.
     * Example: "Quando foi a última mamada e quanto tempo durou?"
     */
    suspend fun queryBabyHistory(
        babyName: String,
        babyAgeWeeks: Int,
        recentEventsSummary: String,
        userQuery: String
    ): String = withContext(Dispatchers.IO) {
        val disclaimer = "\n\n⚠️ *Nota:* Esta resposta tem caráter informativo baseado nos registros e não substitui orientação médica."

        if (!isKeyConfigured()) {
            return@withContext "O assistente Gemini não está configurado com chave de API. No entanto, com base no seu histórico recente: $recentEventsSummary $disclaimer"
        }

        val systemInstruction = "Você é Nanei, uma assistente carinhosa, empática, especialista em cuidados infantis, amamentação, sono do bebê e desenvolvimento. Responda em português de forma clara, acolhedora e direta."
        
        val prompt = """
            $systemInstruction
            
            Informações do bebê:
            Nome: $babyName
            Idade: $babyAgeWeeks semanas
            
            Resumo dos eventos recentes registrados no app Nanei:
            $recentEventsSummary
            
            Pergunta da mãe/cuidador:
            "$userQuery"
            
            Responda à pergunta da mãe com base estrita nos dados registrados acima, com tom muito afetuoso, prático e encorajador.
        """.trimIndent()

        try {
            val answer = callGeminiRestApi(prompt)
            "$answer $disclaimer"
        } catch (e: Exception) {
            Log.e(TAG, "Error querying Gemini", e)
            "Desculpe, ocorreu um problema ao consultar o assistente Gemini. Com base nos seus registros: $recentEventsSummary $disclaimer"
        }
    }

    private fun callGeminiRestApi(prompt: String): String {
        val apiKey = getApiKey()
        val urlString = "$BASE_URL?key=$apiKey"
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.connectTimeout = 30000
        conn.readTimeout = 30000
        conn.doOutput = true

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)
        }

        OutputStreamWriter(conn.outputStream).use { writer ->
            writer.write(requestJson.toString())
            writer.flush()
        }

        val responseCode = conn.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(conn.inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            reader.close()

            val responseObj = JSONObject(sb.toString())
            val candidates = responseObj.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCand = candidates.getJSONObject(0)
                val content = firstCand.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "")
                }
            }
            return ""
        } else {
            val errorStream = conn.errorStream
            val errorText = errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            Log.e(TAG, "Gemini API Error $responseCode: $errorText")
            throw RuntimeException("Gemini API Error $responseCode")
        }
    }

    private fun parseEventsFromJsonResponse(babyId: Long, jsonStr: String): List<Event> {
        val events = mutableListOf<Event>()
        val cleanedJson = jsonStr.replace("```json", "").replace("```", "").trim()
        val now = System.currentTimeMillis()

        try {
            val jsonArray = if (cleanedJson.startsWith("[")) {
                JSONArray(cleanedJson)
            } else if (cleanedJson.startsWith("{")) {
                JSONArray().put(JSONObject(cleanedJson))
            } else {
                JSONArray()
            }

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val typeStr = obj.optString("type", "NOTE")
                val eventType = try {
                    EventType.valueOf(typeStr)
                } catch (e: Exception) {
                    EventType.NOTE
                }

                val event = Event(
                    babyId = babyId,
                    type = eventType,
                    startTimeMs = now,
                    side = obj.optString("side", null),
                    durationLeftSec = obj.optInt("durationLeftSec", 0),
                    durationRightSec = obj.optInt("durationRightSec", 0),
                    volumeMl = if (obj.has("volumeMl")) obj.optInt("volumeMl") else null,
                    bottleType = obj.optString("bottleType", null),
                    diaperType = obj.optString("diaperType", null),
                    medicineName = obj.optString("medicineName", null),
                    dosage = obj.optString("dosage", null),
                    temperatureCelsius = if (obj.has("temperatureCelsius")) obj.optDouble("temperatureCelsius") else null,
                    weightKg = if (obj.has("weightKg")) obj.optDouble("weightKg") else null,
                    heightCm = if (obj.has("heightCm")) obj.optDouble("heightCm") else null,
                    headCircumferenceCm = if (obj.has("headCircumferenceCm")) obj.optDouble("headCircumferenceCm") else null,
                    notes = obj.optString("notes", null),
                    createdBy = "Assistente IA Nanei",
                    createdAtMs = now
                )
                events.add(event)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON from Gemini", e)
        }

        return if (events.isNotEmpty()) events else fallbackParseText(babyId, jsonStr)
    }

    private fun fallbackParseText(babyId: Long, text: String): List<Event> {
        val lower = text.lowercase()
        val events = mutableListOf<Event>()
        val now = System.currentTimeMillis()

        if (lower.contains("mam") || lower.contains("peito") || lower.contains("seio") || lower.contains("amament")) {
            val side = if (lower.contains("direit")) "RIGHT" else if (lower.contains("esquerd")) "LEFT" else "BOTH"
            events.add(
                Event(
                    babyId = babyId,
                    type = EventType.BREASTFEEDING,
                    startTimeMs = now,
                    side = side,
                    durationLeftSec = 900,
                    notes = text,
                    createdBy = "Voz/Texto"
                )
            )
        }
        if (lower.contains("fralda") || lower.contains("xixi") || lower.contains("cocô") || lower.contains("coco")) {
            val diaperType = if (lower.contains("cocô") || lower.contains("coco")) "POOP" else "PEE"
            events.add(
                Event(
                    babyId = babyId,
                    type = EventType.DIAPER,
                    startTimeMs = now,
                    diaperType = diaperType,
                    notes = text,
                    createdBy = "Voz/Texto"
                )
            )
        }
        if (lower.contains("dorm") || lower.contains("soneca") || lower.contains("sono")) {
            events.add(
                Event(
                    babyId = babyId,
                    type = EventType.SLEEP,
                    startTimeMs = now - (60 * 60 * 1000), // 1 hour ago
                    endTimeMs = now,
                    notes = text,
                    createdBy = "Voz/Texto"
                )
            )
        }

        if (events.isEmpty()) {
            events.add(
                Event(
                    babyId = babyId,
                    type = EventType.NOTE,
                    startTimeMs = now,
                    notes = text,
                    createdBy = "Voz/Texto"
                )
            )
        }

        return events
    }
}
