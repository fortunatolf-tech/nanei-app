package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
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

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return emptyList()
        val lines = mutableListOf<String>()
        val paragraphs = text.split("\n")
        for (paragraph in paragraphs) {
            val words = paragraph.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) <= maxWidth) {
                    currentLine = testLine
                } else {
                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine)
                    }
                    if (paint.measureText(word) > maxWidth) {
                        var remaining = word
                        while (remaining.isNotEmpty()) {
                            val count = paint.breakText(remaining, true, maxWidth, null)
                            lines.add(remaining.substring(0, count))
                            remaining = remaining.substring(count)
                        }
                        currentLine = ""
                    } else {
                        currentLine = word
                    }
                }
            }
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
            }
        }
        return lines
    }

    private fun loadHighQualityBitmap(context: Context, photoUrl: String, maxTargetW: Int, maxTargetH: Int): Bitmap? {
        return try {
            val uri = Uri.parse(photoUrl)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            val rawW = options.outWidth
            val rawH = options.outHeight
            if (rawW <= 0 || rawH <= 0) return null

            // Target high density resolution (2x for PDF print rendering)
            val renderW = maxTargetW * 2
            val renderH = maxTargetH * 2

            var sampleSize = 1
            while (rawW / (sampleSize * 2) >= renderW && rawH / (sampleSize * 2) >= renderH) {
                sampleSize *= 2
            }

            val decodeStream = context.contentResolver.openInputStream(uri) ?: return null
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = BitmapFactory.decodeStream(decodeStream, null, decodeOptions)
            decodeStream.close()

            if (decoded != null) {
                val scale = Math.min(renderW.toFloat() / decoded.width, renderH.toFloat() / decoded.height)
                val finalW = (decoded.width * scale).toInt().coerceAtLeast(1)
                val finalH = (decoded.height * scale).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(decoded, finalW, finalH, true)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
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

            // Color Palette for cozy maternal theme
            val bgPaint = Paint().apply {
                color = Color.parseColor("#FFFDF9") // Warm ivory background
                style = Paint.Style.FILL
            }

            val coverBannerPaint = Paint().apply {
                color = Color.parseColor("#FFF1F2") // Soft rose blush banner
                style = Paint.Style.FILL
            }

            val ribbonPaint = Paint().apply {
                color = Color.parseColor("#FB7185") // Rose accent line
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }

            val titlePaint = Paint().apply {
                color = Color.parseColor("#881337") // Deep rose burgundy
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#BE185D") // Warm rose
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val quotePaint = Paint().apply {
                color = Color.parseColor("#9F1239")
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                isAntiAlias = true
            }

            val cardBgPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }

            val cardBorderPaint = Paint().apply {
                color = Color.parseColor("#FECDD3") // Soft rose card border
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
                isAntiAlias = true
            }

            val pillBgPaint = Paint().apply {
                color = Color.parseColor("#FCE7F3") // Soft rose pill badge
                style = Paint.Style.FILL
            }

            val pillTextPaint = Paint().apply {
                color = Color.parseColor("#9D174D")
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val dateTextPaint = Paint().apply {
                color = Color.parseColor("#64748B")
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val entryTitlePaint = Paint().apply {
                color = Color.parseColor("#1E293B")
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val entryTextPaint = Paint().apply {
                color = Color.parseColor("#334155")
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val photoFrameBorderPaint = Paint().apply {
                color = Color.parseColor("#F472B6") // Soft rose photo border
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
                isAntiAlias = true
            }

            val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG).apply {
                isFilterBitmap = true
                isDither = true
            }

            val footerTextPaint = Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            // Draw full page warm background
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

            // Draw Cover Header Banner
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 160f, coverBannerPaint)
            canvas.drawLine(0f, 160f, pageWidth.toFloat(), 160f, ribbonPaint)

            canvas.drawText("🌸 LIVRO DE MEMÓRIAS DA GESTAÇÃO", 28f, 48f, titlePaint)
            canvas.drawText("Diário de Amor & Acompanhamento de $babyName", 28f, 75f, subtitlePaint)
            canvas.drawText("“Cada batida do coração, cada semana de gestação, um amor que cresce sem limites.”", 28f, 98f, quotePaint)
            canvas.drawText("Gerado com carinho no App Nanei em ${dateFormat.format(Date())}", 28f, 125f, dateTextPaint)

            var yPos = 185f

            val chapterTitlePaint = Paint().apply {
                color = Color.parseColor("#881337")
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("💖 Recordações & Capítulo das Memórias (${entries.size} momentos)", 28f, yPos, chapterTitlePaint)
            yPos += 22f

            for (entry in entries) {
                // High-fidelity Photo Loading
                val bitmapToDraw = if (!entry.photoUrl.isNullOrEmpty()) {
                    loadHighQualityBitmap(context, entry.photoUrl, maxTargetW = 160, maxTargetH = 120)
                } else null

                // Compute Layout
                val hasPhoto = bitmapToDraw != null
                val photoDisplayW = if (hasPhoto) 150f else 0f
                val photoDisplayH = if (hasPhoto) {
                    val aspect = bitmapToDraw!!.height.toFloat() / bitmapToDraw.width.toFloat()
                    (photoDisplayW * aspect).coerceIn(80f, 120f)
                } else 0f

                val availableTextW = if (hasPhoto) (pageWidth - 56f - photoDisplayW - 35f) else (pageWidth - 76f)
                val noteLines = wrapText(entry.notes, entryTextPaint, availableTextW)

                val textContentH = noteLines.size * 14.5f
                val minHeightForText = 50f + textContentH + 15f
                val minHeightForPhoto = if (hasPhoto) photoDisplayH + 30f else 0f

                val cardHeight = maxOf(minHeightForText, minHeightForPhoto, 85f)

                // Check page height limit
                if (yPos + cardHeight > pageHeight - 55f) {
                    // Draw Footer on previous page
                    canvas.drawText("🌸 Nanei App — Livro de Memórias de $babyName • Página $pageNumber", 28f, pageHeight - 20f, footerTextPaint)
                    pdfDocument.finishPage(page)

                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    // Draw Page background and Mini Header on new page
                    canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)
                    canvas.drawRect(0f, 0f, pageWidth.toFloat(), 40f, coverBannerPaint)
                    canvas.drawLine(0f, 40f, pageWidth.toFloat(), 40f, ribbonPaint)
                    canvas.drawText("🌸 Nanei — Livro de Memórias ($babyName) • Pág. $pageNumber", 28f, 26f, subtitlePaint)

                    yPos = 55f
                }

                // Draw Card Outer Box
                val cardRect = RectF(28f, yPos, pageWidth - 28f, yPos + cardHeight)
                canvas.drawRoundRect(cardRect, 14f, 14f, cardBgPaint)
                canvas.drawRoundRect(cardRect, 14f, 14f, cardBorderPaint)

                // Draw Gestational Week Pill Badge
                val weekText = if (entry.gestationalWeek != null) "🌸 ${entry.gestationalWeek}ª SEMANA" else "💖 MEMÓRIA"
                val pillWidth = pillTextPaint.measureText(weekText) + 16f
                val pillRect = RectF(40f, yPos + 12f, 40f + pillWidth, yPos + 28f)
                canvas.drawRoundRect(pillRect, 8f, 8f, pillBgPaint)
                canvas.drawText(weekText, 48f, yPos + 23f, pillTextPaint)

                // Category & Date Info
                val dateStr = dateFormat.format(Date(entry.dateMs))
                val categoryText = "🏷️ ${entry.category}  |  📅 $dateStr"
                canvas.drawText(categoryText, 40f + pillWidth + 12f, yPos + 23f, dateTextPaint)

                // Entry Title with Mood Emoji
                val displayTitle = "${entry.moodEmoji} ${entry.title}"
                canvas.drawText(truncateText(displayTitle, 40), 40f, yPos + 46f, entryTitlePaint)

                // Note Lines
                var lineY = yPos + 63f
                for (line in noteLines.take(8)) {
                    canvas.drawText(line, 40f, lineY, entryTextPaint)
                    lineY += 14.5f
                }

                // Render Photo (High Quality, Framed with Rounded Corners)
                if (hasPhoto && bitmapToDraw != null) {
                    val photoLeft = pageWidth - 28f - 12f - photoDisplayW
                    val photoTop = yPos + 12f
                    val photoRect = RectF(photoLeft, photoTop, photoLeft + photoDisplayW, photoTop + photoDisplayH)

                    val photoPath = Path().apply {
                        addRoundRect(photoRect, 10f, 10f, Path.Direction.CW)
                    }

                    canvas.save()
                    canvas.clipPath(photoPath)
                    canvas.drawBitmap(bitmapToDraw, null, photoRect, photoPaint)
                    canvas.restore()

                    // Photo Border Line
                    canvas.drawRoundRect(photoRect, 10f, 10f, photoFrameBorderPaint)
                }

                yPos += cardHeight + 14f
            }

            // Draw Footer on Last Page
            canvas.drawText("🌸 Nanei App — Livro de Memórias de $babyName • Página $pageNumber", 28f, pageHeight - 20f, footerTextPaint)
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

