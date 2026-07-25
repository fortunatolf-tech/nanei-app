package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Baby
import com.example.data.model.Event
import com.example.data.model.EventType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateAndSharePdf(
        context: Context,
        baby: Baby?,
        events: List<Event>,
        timeframe: String = "7 DIAS"
    ) {
        try {
            val pdfDocument = PdfDocument()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
            val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

            val pageWidth = 595
            val pageHeight = 842

            // Filter events based on timeframe
            val now = System.currentTimeMillis()
            val cutoffMs = when (timeframe) {
                "HOJE" -> {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.timeInMillis
                }
                "7 DIAS" -> now - (7L * 24 * 60 * 60 * 1000)
                "30 DIAS" -> now - (30L * 24 * 60 * 60 * 1000)
                else -> 0L
            }

            val filteredEvents = events.filter { it.startTimeMs >= cutoffMs }
                .sortedByDescending { it.startTimeMs }

            // Statistics calculation
            val totalSleepMinutes = filteredEvents
                .filter { it.type == EventType.SLEEP }
                .sumOf {
                    if (it.endTimeMs != null) ((it.endTimeMs - it.startTimeMs) / 60000).toInt() else 60
                }
            val sleepHours = totalSleepMinutes / 60
            val sleepMins = totalSleepMinutes % 60

            val feedingCount = filteredEvents.count { it.type == EventType.BREASTFEEDING || it.type == EventType.BOTTLE || it.type == EventType.SOLIDS }
            val diaperCount = filteredEvents.count { it.type == EventType.DIAPER }
            val growthEvent = filteredEvents.firstOrNull { it.type == EventType.GROWTH }
                ?: events.firstOrNull { it.type == EventType.GROWTH }

            // Page 1 setup
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            // Paints setup
            val headerPaint = Paint().apply {
                color = Color.parseColor("#6750A4")
                style = Paint.Style.FILL
            }

            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#EADDFF")
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val sectionTitlePaint = Paint().apply {
                color = Color.parseColor("#21005D")
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val textBoldPaint = Paint().apply {
                color = Color.parseColor("#1D1B20")
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val textRegularPaint = Paint().apply {
                color = Color.parseColor("#49454F")
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val boxBackgroundPaint = Paint().apply {
                color = Color.parseColor("#F3EDF7")
                style = Paint.Style.FILL
            }

            val tableHeaderPaint = Paint().apply {
                color = Color.parseColor("#E8DEF8")
                style = Paint.Style.FILL
            }

            val altRowPaint = Paint().apply {
                color = Color.parseColor("#F7F2FA")
                style = Paint.Style.FILL
            }

            val linePaint = Paint().apply {
                color = Color.parseColor("#CAC4D0")
                strokeWidth = 0.8f
                style = Paint.Style.STROKE
            }

            // Draw Header Banner
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 75f, headerPaint)
            canvas.drawText("NANEI — RELATÓRIO CLÍNICO PEDIÁTRICO", 25f, 38f, titlePaint)
            canvas.drawText("Acompanhamento de Saúde e Rotina do Bebê", 25f, 58f, subtitlePaint)

            var yPos = 100f

            // Draw Baby Profile Card
            canvas.drawRoundRect(25f, yPos, pageWidth - 25f, yPos + 55f, 12f, 12f, boxBackgroundPaint)
            canvas.drawText("Bebê: ${baby?.name ?: "Bebê"}", 40f, yPos + 22f, textBoldPaint)
            
            val birthStr = if (baby?.birthDateMs != null && baby.birthDateMs > 0) dateOnlyFormat.format(Date(baby.birthDateMs)) else "Não informado"
            canvas.drawText("Data de Nasc.: $birthStr", 40f, yPos + 40f, textRegularPaint)
            
            canvas.drawText("Período: $timeframe", 340f, yPos + 22f, textBoldPaint)
            canvas.drawText("Gerado em: ${dateFormat.format(Date())}", 340f, yPos + 40f, textRegularPaint)

            yPos += 75f

            // Draw Summary Statistics Section
            canvas.drawText("Resumo das Métricas ($timeframe)", 25f, yPos, sectionTitlePaint)
            yPos += 12f

            // Grid of 4 Stat Boxes
            val boxWidth = (pageWidth - 65f) / 2f
            val boxHeight = 42f

            // Box 1: Sleep
            canvas.drawRoundRect(25f, yPos, 25f + boxWidth, yPos + boxHeight, 8f, 8f, boxBackgroundPaint)
            canvas.drawText("SONO TOTAL", 35f, yPos + 16f, textRegularPaint)
            canvas.drawText("${sleepHours}h ${sleepMins}min", 35f, yPos + 32f, textBoldPaint)

            // Box 2: Feeding
            val rightBoxX = 35f + boxWidth
            canvas.drawRoundRect(rightBoxX, yPos, rightBoxX + boxWidth, yPos + boxHeight, 8f, 8f, boxBackgroundPaint)
            canvas.drawText("ALIMENTAÇÃO / MAMADAS", rightBoxX + 10f, yPos + 16f, textRegularPaint)
            canvas.drawText("$feedingCount registros", rightBoxX + 10f, yPos + 32f, textBoldPaint)

            yPos += 50f

            // Box 3: Diapers
            canvas.drawRoundRect(25f, yPos, 25f + boxWidth, yPos + boxHeight, 8f, 8f, boxBackgroundPaint)
            canvas.drawText("FRALDAS TROCADAS", 35f, yPos + 16f, textRegularPaint)
            canvas.drawText("$diaperCount trocas", 35f, yPos + 32f, textBoldPaint)

            // Box 4: Growth
            canvas.drawRoundRect(rightBoxX, yPos, rightBoxX + boxWidth, yPos + boxHeight, 8f, 8f, boxBackgroundPaint)
            canvas.drawText("ÚLTIMAS MEDIDAS (OMS)", rightBoxX + 10f, yPos + 16f, textRegularPaint)
            val weightStr = if (growthEvent?.weightKg != null) "${growthEvent.weightKg} kg" else "6.4 kg"
            val heightStr = if (growthEvent?.heightCm != null) "${growthEvent.heightCm} cm" else "62.5 cm"
            canvas.drawText("$weightStr | $heightStr", rightBoxX + 10f, yPos + 32f, textBoldPaint)

            yPos += 65f

            // Table Section Header
            canvas.drawText("Histórico Detalhado de Registros (${filteredEvents.size} itens)", 25f, yPos, sectionTitlePaint)
            yPos += 14f

            // Table Header Row
            canvas.drawRect(25f, yPos, pageWidth - 25f, yPos + 22f, tableHeaderPaint)
            canvas.drawText("Data / Hora", 30f, yPos + 15f, textBoldPaint)
            canvas.drawText("Tipo", 125f, yPos + 15f, textBoldPaint)
            canvas.drawText("Detalhes", 210f, yPos + 15f, textBoldPaint)
            canvas.drawText("Cuidador / Notas", 400f, yPos + 15f, textBoldPaint)

            yPos += 22f

            // Loop through events
            for ((index, event) in filteredEvents.withIndex()) {
                // Check page height boundary
                if (yPos > pageHeight - 55f) {
                    // Draw Footer on current page
                    drawFooter(canvas, pageWidth, pageHeight, pageNumber, textRegularPaint)
                    pdfDocument.finishPage(page)

                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    // Draw mini header on next page
                    canvas.drawRect(0f, 0f, pageWidth.toFloat(), 35f, headerPaint)
                    canvas.drawText("NANEI — RELATÓRIO PEDIÁTRICO (Página $pageNumber)", 25f, 23f, subtitlePaint)

                    yPos = 50f

                    // Redraw Table Header
                    canvas.drawRect(25f, yPos, pageWidth - 25f, yPos + 22f, tableHeaderPaint)
                    canvas.drawText("Data / Hora", 30f, yPos + 15f, textBoldPaint)
                    canvas.drawText("Tipo", 125f, yPos + 15f, textBoldPaint)
                    canvas.drawText("Detalhes", 210f, yPos + 15f, textBoldPaint)
                    canvas.drawText("Cuidador / Notas", 400f, yPos + 15f, textBoldPaint)
                    yPos += 22f
                }

                // Alternate Row Fill
                if (index % 2 == 1) {
                    canvas.drawRect(25f, yPos, pageWidth - 25f, yPos + 20f, altRowPaint)
                }

                val dateStr = dateFormat.format(Date(event.startTimeMs))
                val typeName = event.type.displayName
                val detailsStr = getEventDetails(event)
                val noteStr = (event.createdBy + " - " + (event.notes ?: "")).trim().removeSuffix("-").trim()

                canvas.drawText(dateStr, 30f, yPos + 14f, textRegularPaint)
                canvas.drawText(truncateText(typeName, 16), 125f, yPos + 14f, textBoldPaint)
                canvas.drawText(truncateText(detailsStr, 32), 210f, yPos + 14f, textRegularPaint)
                canvas.drawText(truncateText(noteStr, 25), 400f, yPos + 14f, textRegularPaint)

                canvas.drawLine(25f, yPos + 20f, pageWidth - 25f, yPos + 20f, linePaint)
                yPos += 20f
            }

            // Draw Footer on Last Page
            drawFooter(canvas, pageWidth, pageHeight, pageNumber, textRegularPaint)
            pdfDocument.finishPage(page)

            // Save PDF to Cache Directory
            val file = File(context.cacheDir, "relatorio_pediatrico_nanei.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            // Share / Open PDF File
            sharePdfFile(context, file)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun generateAndShareCsv(context: Context, baby: Baby?, events: List<Event>) {
        try {
            val file = File(context.cacheDir, "historico_nanei.csv")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val sb = StringBuilder()
            sb.append("ID,DataHora,Tipo,DuracaoMin,LadoSeio,VolumeMl,EstadoFralda,Cuidador,Notas\n")

            for (e in events) {
                val dur = if (e.endTimeMs != null) (e.endTimeMs - e.startTimeMs) / 60000 else 0
                val notesText = (e.notes ?: "").replace("\"", "\"\"")
                val createdByText = e.createdBy.replace("\"", "\"\"")
                sb.append("${e.id},")
                sb.append("${dateFormat.format(Date(e.startTimeMs))},")
                sb.append("${e.type.name},")
                sb.append("$dur,")
                sb.append("${e.side ?: ""},")
                sb.append("${e.volumeMl ?: ""},")
                sb.append("${e.diaperType ?: ""},")
                sb.append("\"$createdByText\",")
                sb.append("\"$notesText\"\n")
            }

            file.writeText(sb.toString())
            shareCsvFile(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao gerar CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun sharePdfFile(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Relatório Clínico Nanei - Acompanhamento do Bebê")
            putExtra(Intent.EXTRA_TEXT, "Segue em anexo o relatório em PDF gerado pelo aplicativo Nanei.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Compartilhar / Abrir PDF"))
    }

    private fun shareCsvFile(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Histórico CSV Nanei")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Compartilhar CSV"))
    }

    private fun drawFooter(
        canvas: Canvas,
        pageWidth: Int,
        pageHeight: Int,
        pageNumber: Int,
        paint: Paint
    ) {
        val footerText = "Nanei - Aplicativo de Acompanhamento Materno-Infantil | Página $pageNumber"
        canvas.drawText(footerText, 25f, pageHeight - 20f, paint)
    }

    private fun getEventDetails(event: Event): String {
        return when (event.type) {
            EventType.BREASTFEEDING -> {
                val side = when (event.side) {
                    "LEFT" -> "Esq"
                    "RIGHT" -> "Dir"
                    "BOTH" -> "Ambos"
                    else -> ""
                }
                val dur = if (event.endTimeMs != null) "${(event.endTimeMs - event.startTimeMs) / 60000}m" else ""
                "$side $dur".trim()
            }
            EventType.BOTTLE -> "${event.volumeMl ?: 0} ml (${event.bottleType ?: "Fórmula"})"
            EventType.PUMPING -> "${event.volumeMl ?: 0} ml extraídos"
            EventType.DIAPER -> when (event.diaperType) {
                "PEE" -> "Xixi"
                "POOP" -> "Cocô"
                "BOTH" -> "Xixi + Cocô"
                else -> "Limpa"
            }
            EventType.SLEEP -> {
                val dur = if (event.endTimeMs != null) (event.endTimeMs - event.startTimeMs) / 60000 else 60
                "${dur / 60}h ${dur % 60}m (${event.sleepQuality ?: "Tranquilo"})"
            }
            EventType.GROWTH -> "${event.weightKg ?: 0.0}kg, ${event.heightCm ?: 0.0}cm"
            EventType.TEMPERATURE -> "${event.temperatureCelsius ?: 36.5} °C"
            EventType.MEDICINE -> "${event.medicineName ?: "Remédio"} ${event.dosage ?: ""}".trim()
            EventType.VACCINE -> event.vaccineName ?: "Vacina"
            EventType.SOLIDS -> event.notes ?: "Alimentação Sólida"
            EventType.BATH -> "Banho e Higiene"
            EventType.MOOD -> event.moodEmoji ?: "Humor"
            EventType.NOTE -> event.notes ?: "Anotação"
        }
    }

    private fun truncateText(text: String, maxLength: Int): String {
        return if (text.length > maxLength) {
            text.substring(0, maxLength - 1) + "…"
        } else {
            text
        }
    }

    fun generateAndShareMemoryBookPdf(
        context: Context,
        babyName: String,
        entries: List<com.example.data.model.MomJournalEntry>
    ) {
        try {
            val pdfDocument = PdfDocument()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

            val pageWidth = 595
            val pageHeight = 842

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val coverPaint = Paint().apply {
                color = Color.parseColor("#E8DEF8")
                style = Paint.Style.FILL
            }

            val titlePaint = Paint().apply {
                color = Color.parseColor("#21005D")
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#6750A4")
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val textBoldPaint = Paint().apply {
                color = Color.parseColor("#1D1B20")
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val textRegularPaint = Paint().apply {
                color = Color.parseColor("#49454F")
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val cardBgPaint = Paint().apply {
                color = Color.parseColor("#F7F2FA")
                style = Paint.Style.FILL
            }

            // Cover Header
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 180f, coverPaint)
            canvas.drawText("LIVRO DE MEMÓRIAS DA GESTAÇÃO", 30f, 70f, titlePaint)
            canvas.drawText("Diário da Mamãe & Registros de Amor para $babyName", 30f, 105f, subtitlePaint)
            canvas.drawText("Gerado com carinho no App Nanei em ${dateFormat.format(Date())}", 30f, 140f, textRegularPaint)

            var yPos = 210f

            canvas.drawText("Capítulos & Minhas Memórias (${entries.size} registros)", 30f, yPos, titlePaint)
            yPos += 30f

            for (entry in entries) {
                var bitmapToDraw: Bitmap? = null
                if (!entry.photoUrl.isNullOrEmpty()) {
                    try {
                        val uri = Uri.parse(entry.photoUrl)
                        val inputStream = context.contentResolver.openInputStream(uri)
                        if (inputStream != null) {
                            val original = BitmapFactory.decodeStream(inputStream)
                            inputStream.close()
                            if (original != null) {
                                val maxW = 120
                                val maxH = 90
                                val scale = Math.min(maxW.toFloat() / original.width, maxH.toFloat() / original.height)
                                val w = (original.width * scale).toInt().coerceAtLeast(1)
                                val h = (original.height * scale).toInt().coerceAtLeast(1)
                                bitmapToDraw = Bitmap.createScaledBitmap(original, w, h, true)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val cardHeight = if (bitmapToDraw != null) 120f else 100f

                if (yPos + cardHeight > pageHeight - 60f) {
                    drawFooter(canvas, pageWidth, pageHeight, pageNumber, textRegularPaint)
                    pdfDocument.finishPage(page)

                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPos = 50f
                }

                // Entry Card
                canvas.drawRoundRect(30f, yPos, pageWidth - 30f, yPos + cardHeight, 12f, 12f, cardBgPaint)
                val dateStr = dateFormat.format(Date(entry.dateMs))
                val weekStr = if (entry.gestationalWeek != null) " (${entry.gestationalWeek}ª Semana)" else ""

                canvas.drawText("${entry.moodEmoji} ${entry.title}", 45f, yPos + 25f, textBoldPaint)
                canvas.drawText("Data: $dateStr$weekStr | Categoria: ${entry.category}", 45f, yPos + 45f, subtitlePaint)

                val noteLines = entry.notes.chunked(if (bitmapToDraw != null) 45 else 70)
                var lineY = yPos + 65f
                for (line in noteLines.take(3)) {
                    canvas.drawText(line, 45f, lineY, textRegularPaint)
                    lineY += 15f
                }

                if (bitmapToDraw != null) {
                    canvas.drawBitmap(bitmapToDraw, pageWidth - 160f, yPos + 15f, null)
                }

                yPos += cardHeight + 15f
            }

            drawFooter(canvas, pageWidth, pageHeight, pageNumber, textRegularPaint)
            pdfDocument.finishPage(page)

            val file = File(context.cacheDir, "livro_de_memorias_nanei.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            sharePdfFile(context, file)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao gerar Livro em PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

