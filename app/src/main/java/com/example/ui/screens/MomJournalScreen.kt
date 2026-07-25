package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MomJournalEntry
import com.example.util.PdfReportGenerator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MomJournalScreen(
    babyName: String,
    journalEntries: List<MomJournalEntry>,
    onAddEntry: (entry: MomJournalEntry) -> Unit,
    onDeleteEntry: (MomJournalEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedViewMode by remember { mutableIntStateOf(0) } // 0: Linha do Tempo, 1: Álbum de Fotos, 2: Prévia do Livro
    var showAddDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Diário & Livro de Memórias 📖",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Eternize cada emoção, ultrassom e fotos",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            PdfReportGenerator.generateAndShareMemoryBookPdf(
                                context = context,
                                babyName = babyName,
                                entries = journalEntries
                            )
                        },
                        modifier = Modifier.testTag("btn_export_memory_book_pdf"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gerar Livro PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_journal_entry")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Memória")
            }
        },
        modifier = modifier.testTag("mom_journal_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // View Mode Selector Tabs
            TabRow(
                selectedTabIndex = selectedViewMode,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedViewMode == 0,
                    onClick = { selectedViewMode = 0 },
                    text = { Text("Linha do Tempo") },
                    icon = { Icon(Icons.Default.ViewList, contentDescription = null) }
                )
                Tab(
                    selected = selectedViewMode == 1,
                    onClick = { selectedViewMode = 1 },
                    text = { Text("Álbum Fotos") },
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) }
                )
                Tab(
                    selected = selectedViewMode == 2,
                    onClick = { selectedViewMode = 2 },
                    text = { Text("Livro Impresso 📖") },
                    icon = { Icon(Icons.Default.AutoStories, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedViewMode) {
                0 -> TimelineView(
                    entries = journalEntries,
                    dateFormat = dateFormat,
                    onDelete = onDeleteEntry
                )
                1 -> PhotoGalleryView(entries = journalEntries)
                2 -> MemoryBookPreviewView(
                    babyName = babyName,
                    entries = journalEntries,
                    dateFormat = dateFormat,
                    onExportPdf = {
                        PdfReportGenerator.generateAndShareMemoryBookPdf(context, babyName, journalEntries)
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddJournalEntryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { entry ->
                onAddEntry(entry)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TimelineView(
    entries: List<MomJournalEntry>,
    dateFormat: SimpleDateFormat,
    onDelete: (MomJournalEntry) -> Unit
) {
    if (entries.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nenhuma memória registrada ainda.\nClique no + para escrever a primeira página do seu livro! ✨",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(entries) { entry ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(entry.moodEmoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = entry.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${dateFormat.format(Date(entry.dateMs))} • ${entry.category}${if (entry.gestationalWeek != null) " (${entry.gestationalWeek}ª Semana)" else ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            IconButton(onClick = { onDelete(entry) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.outline)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = entry.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (!entry.photoUrl.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            AsyncImage(
                                model = entry.photoUrl,
                                contentDescription = "Foto da Memória",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Badge / Photo card
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Guardado no Livro de Memórias",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoGalleryView(entries: List<MomJournalEntry>) {
    val photoEntries = entries.filter { !it.photoUrl.isNullOrEmpty() || it.category.contains("Ultrassom") || it.category.contains("Foto") }

    if (photoEntries.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Nenhuma foto ou ultrassom cadastrado ainda 🖼️\nAdicione clicando no botão + com foto!", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.outline)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(photoEntries) { item ->
                Card(
                    modifier = Modifier
                        .padding(6.dp)
                        .height(160.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!item.photoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = item.photoUrl,
                                contentDescription = item.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = item.title,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = item.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Text(if (item.category.contains("Ultrassom")) "🖼️" else "📸", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = item.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryBookPreviewView(
    babyName: String,
    entries: List<MomJournalEntry>,
    dateFormat: SimpleDateFormat,
    onExportPdf: () -> Unit
) {
    var currentPageIndex by remember { mutableIntStateOf(0) } // 0: Capa, 1..N: Páginas

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF8F2)), // Warm book paper tone
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .border(2.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentPageIndex == 0) {
                    // BOOK COVER PAGE
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📖", fontSize = 30.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "LIVRO DE MEMÓRIAS",
                            style = MaterialTheme.typography.labelLarge,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Diário da Gestação de $babyName",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF21005D)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Uma história de amor escrita dia a dia ✨",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${entries.size} Páginas de Amor Registradas",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // PAGE CONTENT
                    val currentEntry = entries.getOrNull(currentPageIndex - 1)
                    if (currentEntry != null) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Página $currentPageIndex de ${entries.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = dateFormat.format(Date(currentEntry.dateMs)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "${currentEntry.moodEmoji} ${currentEntry.title}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF21005D)
                            )

                            Text(
                                text = "Categoria: ${currentEntry.category}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = currentEntry.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF1D1B20),
                                lineHeight = 20.sp
                            )

                            if (!currentEntry.photoUrl.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                AsyncImage(
                                    model = currentEntry.photoUrl,
                                    contentDescription = "Foto no Livro",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Page Navigation Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                enabled = currentPageIndex > 0
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Página Anterior")
            }

            Text(
                text = if (currentPageIndex == 0) "Capa do Livro" else "Página $currentPageIndex",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )

            IconButton(
                onClick = { if (currentPageIndex < entries.size) currentPageIndex++ },
                enabled = currentPageIndex < entries.size
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Próxima Página")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onExportPdf,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_export_memory_book_pdf_bottom"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("BAIXAR LIVRO IMPRESSO EM PDF", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun AddJournalEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: (MomJournalEntry) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Ultrassom 🖼️") }
    var weekText by remember { mutableStateOf("24") }
    var notes by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("🥰") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri
        }
    }

    val categories = listOf("Ultrassom 🖼️", "Foto Barriga 📸", "Exame 🩺", "Pensamentos 💭", "Carta ao Bebê ✉️")
    val moods = listOf("🥰", "🥹", "💓", "✨", "😴", "🤰")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoStories, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nova Página no Livro 📖")
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título da Memória") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_journal_title")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Categoria:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                ScrollableTabRow(selectedTabIndex = categories.indexOf(category).coerceAtLeast(0)) {
                    categories.forEach { cat ->
                        Tab(
                            selected = category == cat,
                            onClick = { category = cat },
                            text = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weekText,
                        onValueChange = { weekText = it },
                        label = { Text("Semana (ex: 24)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Sentimento / Humor:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    moods.forEach { emoji ->
                        FilterChip(
                            selected = selectedMood == emoji,
                            onClick = { selectedMood = emoji },
                            label = { Text(emoji, fontSize = 18.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Escreva suas memórias, pensamentos e detalhes...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("input_journal_notes")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { photoLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth().testTag("btn_pick_journal_photo")
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (photoUri != null) "Foto Anexada ✓ (Trocar)" else "Carregar Foto / Ultrassom 📸",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (photoUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Prévia da Foto",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty() && notes.isNotEmpty()) {
                        val weekInt = weekText.toIntOrNull()
                        onConfirm(
                            MomJournalEntry(
                                title = title,
                                category = category,
                                gestationalWeek = weekInt,
                                notes = notes,
                                moodEmoji = selectedMood,
                                photoUrl = photoUri?.toString()
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("btn_save_journal_entry")
            ) {
                Text("Guardar Memória")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
