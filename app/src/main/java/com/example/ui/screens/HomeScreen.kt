package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.Baby
import com.example.data.model.Event
import com.example.data.model.EventType
import com.example.data.model.SweetSpotPrediction
import com.example.ui.components.AdBannerCard
import com.example.ui.components.EventItemCard
import com.example.ui.components.QuickActionGrid
import com.example.ui.components.SweetSpotCard
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    selectedBaby: Baby?,
    allBabies: List<Baby>,
    events: List<Event>,
    sweetSpot: SweetSpotPrediction?,
    onSwitchBaby: (Long) -> Unit,
    onAddNewBaby: (String, Long, Long, String, String?) -> Unit,
    onLogActionClick: (EventType) -> Unit,
    onDeleteEvent: (Event) -> Unit,
    onNightModeToggle: () -> Unit,
    onOpenAiAssistant: () -> Unit,
    onOpenFamilyDialog: () -> Unit = {},
    onOpenSoundListenDialog: () -> Unit = {},
    onOpenOnboarding: () -> Unit = {},
    isPremiumUser: Boolean = false,
    onOpenPaywall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isBabyDropdownExpanded by remember { mutableStateOf(false) }
    var showAddBabyDialog by remember { mutableStateOf(false) }
    var selectedFilterType by remember { mutableStateOf<EventType?>(null) }

    val filteredEvents = if (selectedFilterType == null) {
        events
    } else {
        events.filter { it.type == selectedFilterType }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isBabyDropdownExpanded = true }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!selectedBaby?.avatarUri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = selectedBaby?.avatarUri,
                                    contentDescription = "Avatar do Bebê",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = selectedBaby?.name?.take(1)?.uppercase() ?: "B",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedBaby?.name ?: "Meu Bebê",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Alternar Bebê"
                                )
                            }

                            if (selectedBaby != null) {
                                val ageMonths = calculateAgeMonths(selectedBaby.birthDateMs)
                                Text(
                                    text = "$ageMonths meses",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isBabyDropdownExpanded,
                            onDismissRequest = { isBabyDropdownExpanded = false }
                        ) {
                            allBabies.forEach { baby ->
                                DropdownMenuItem(
                                    text = { Text(baby.name + if (baby.isSelected) " (Ativo)" else "") },
                                    leadingIcon = {
                                        if (!baby.avatarUri.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = baby.avatarUri,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp).clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Face,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    onClick = {
                                        onSwitchBaby(baby.id)
                                        isBabyDropdownExpanded = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("+ Cadastrar Novo Bebê") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    isBabyDropdownExpanded = false
                                    showAddBabyDialog = true
                                }
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenFamilyDialog,
                        modifier = Modifier.testTag("btn_topbar_family")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Família & Cuidadores",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onOpenSoundListenDialog,
                        modifier = Modifier.testTag("btn_topbar_sound_listen")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Modo Escuta",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onOpenPaywall,
                        modifier = Modifier.testTag("btn_topbar_premium")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Nanei Premium",
                            tint = if (isPremiumUser) androidx.compose.ui.graphics.Color(0xFFFFD700) else MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onNightModeToggle,
                        modifier = Modifier.testTag("btn_night_mode_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = "Modo Noturno",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenAiAssistant,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voz IA"
                    )
                },
                text = { Text("Voz / IA Nanei") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_voice_ai")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. SweetSpot Sleep Banner
            item {
                SweetSpotCard(
                    prediction = sweetSpot,
                    onLogSleepClick = { onLogActionClick(EventType.SLEEP) }
                )
            }

            // 2. Quick Action Grid (1 mão, 2 toques)
            item {
                QuickActionGrid(
                    onActionClick = onLogActionClick
                )
            }

            // 2.5 Ad Banner for Free Users
            if (!isPremiumUser) {
                item {
                    AdBannerCard(
                        onRemoveAdsClick = onOpenPaywall
                    )
                }
            }

            // 3. Timeline Filter Chips
            item {
                Column {
                    Text(
                        text = "Linha do Tempo Hoje",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilterType == null,
                                onClick = { selectedFilterType = null },
                                label = { Text("Todos") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilterType == EventType.BREASTFEEDING,
                                onClick = { selectedFilterType = EventType.BREASTFEEDING },
                                label = { Text("🤱 Amamentação") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilterType == EventType.SLEEP,
                                onClick = { selectedFilterType = EventType.SLEEP },
                                label = { Text("💤 Sono") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilterType == EventType.DIAPER,
                                onClick = { selectedFilterType = EventType.DIAPER },
                                label = { Text("🧷 Fralda") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilterType == EventType.MEDICINE,
                                onClick = { selectedFilterType = EventType.MEDICINE },
                                label = { Text("💊 Medicamentos") }
                            )
                        }
                    }
                }
            }

            // 4. Events Timeline List
            if (filteredEvents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nenhum evento registrado nesta categoria hoje.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredEvents,
                    key = { it.id }
                ) { event ->
                    EventItemCard(
                        event = event,
                        onDeleteClick = onDeleteEvent
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
            }
        }
    }

    if (showAddBabyDialog) {
        AddBabyDialog(
            onDismiss = { showAddBabyDialog = false },
            onSave = { name, birthMs, estimatedMs, gender, avatarUri ->
                onAddNewBaby(name, birthMs, estimatedMs, gender, avatarUri)
                showAddBabyDialog = false
            }
        )
    }
}

@Composable
private fun AddBabyDialog(
    onDismiss: () -> Unit,
    onSave: (String, Long, Long, String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("UNKNOWN") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cadastrar Novo Bebê 👶") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do bebê") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_add_baby_name")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Sexo do Bebê:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = gender == "FEMALE",
                            onClick = { gender = "FEMALE" },
                            label = { Text("Menina 👧") }
                        )
                        FilterChip(
                            selected = gender == "MALE",
                            onClick = { gender = "MALE" },
                            label = { Text("Menino 👦") }
                        )
                    }
                    FilterChip(
                        selected = gender == "UNKNOWN",
                        onClick = { gender = "UNKNOWN" },
                        label = { Text("Ainda não sei / Surpresa 💛") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Foto de Perfil do Bebê (Opcional):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri != null) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Foto do Bebê",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { photoLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f).testTag("btn_select_baby_photo")
                    ) {
                        Text(if (photoUri != null) "Trocar Foto 📸" else "Adicionar Foto / Ultrassom 📸")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val babyName = name.ifBlank {
                        when (gender) {
                            "FEMALE" -> "Minha Menina"
                            "MALE" -> "Meu Menino"
                            else -> "Meu Bebê"
                        }
                    }
                    val birthMs = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000L) // default 3 months
                    onSave(babyName, birthMs, birthMs, gender, photoUri?.toString())
                },
                modifier = Modifier.testTag("btn_save_new_baby")
            ) {
                Text("Cadastrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun calculateAgeMonths(birthDateMs: Long): Int {
    val diff = System.currentTimeMillis() - birthDateMs
    val days = (diff / (1000 * 60 * 60 * 24)).toInt()
    return (days / 30).coerceAtLeast(0)
}
