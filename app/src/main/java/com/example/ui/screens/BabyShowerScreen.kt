package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BabyShowerScreen(
    event: BabyShowerEvent,
    guests: List<BabyShowerGuest>,
    gifts: List<BabyShowerGift>,
    syncLogs: List<SyncTestLog>,
    isSyncing: Boolean,
    userEmail: String,
    onUpdateEvent: (BabyShowerEvent) -> Unit,
    onAddGuest: (BabyShowerGuest) -> Unit,
    onUpdateGuestStatus: (String, RsvpStatus) -> Unit,
    onDeleteGuest: (String) -> Unit,
    onAddGift: (BabyShowerGift) -> Unit,
    onToggleGiftReservation: (String, String?) -> Unit,
    onRunSyncTests: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) } // 0: Link & Evento, 1: Convite & Envio, 2: Protótipo & Testes, 3: Doc Técnica

    // Dialog state for adding guest
    var showAddGuestDialog by remember { mutableStateOf(false) }
    var newGuestName by remember { mutableStateOf("") }
    var newGuestPhone by remember { mutableStateOf("") }
    var newGuestAdults by remember { mutableStateOf("1") }
    var newGuestChildren by remember { mutableStateOf("0") }

    // Dialog state for adding gift
    var showAddGiftDialog by remember { mutableStateOf(false) }
    var newGiftTitle by remember { mutableStateOf("") }
    var newGiftCategory by remember { mutableStateOf("Fraldas") }
    var newGiftPrice by remember { mutableStateOf("50") }
    var newGiftUrl by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Header Card: Brand & Realtime Web Sync Status ---
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFB7185).copy(alpha = 0.2f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🍼", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Menu Digital de Chá de Bebê",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Plataforma Oficial nanei.com.br",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Realtime Cloud Sync Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (event.isSyncedWithWeb) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSyncing) Icons.Default.Sync else if (event.isSyncedWithWeb) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = "Sync",
                                tint = if (event.isSyncedWithWeb) Color(0xFF166534) else Color(0xFF854D0E),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSyncing) "Sincronizando..." else if (event.isSyncedWithWeb) "Página no ar" else "Não publicado",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (event.isSyncedWithWeb) Color(0xFF166534) else Color(0xFF854D0E)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // User Account & Privacy Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sessão Ativa: $userEmail",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "LGPD",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Dados protegidos • LGPD",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFF0284C7)
                        )
                    }
                }
            }
        }

        // --- Navigation Tabs ---
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("1. Link & Evento", style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_baby_shower_link")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("2. Convites & Envio", style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Default.MarkEmailRead, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_baby_shower_invitation")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("3. Publicação & Sync", style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_baby_shower_prototype")
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("4. Como Funciona", style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_baby_shower_doc")
            )
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        // --- Tab Content Area ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> TabLinkAndEvent(
                    event = event,
                    gifts = gifts,
                    onUpdateEvent = onUpdateEvent,
                    onOpenAddGiftDialog = { showAddGiftDialog = true },
                    onToggleGiftReservation = onToggleGiftReservation
                )
                1 -> TabInvitationAndSending(
                    event = event,
                    guests = guests,
                    gifts = gifts,
                    onUpdateEvent = onUpdateEvent,
                    onOpenAddGuestDialog = { showAddGuestDialog = true },
                    onUpdateGuestStatus = onUpdateGuestStatus,
                    onDeleteGuest = onDeleteGuest
                )
                2 -> TabPublishAndSync(
                    event = event,
                    guests = guests,
                    gifts = gifts,
                    syncLogs = syncLogs,
                    isSyncing = isSyncing,
                    onRunSyncTests = onRunSyncTests
                )
                3 -> TabHowItWorks()
            }
        }
    }

    // --- Modal: Add Guest ---
    if (showAddGuestDialog) {
        AlertDialog(
            onDismissRequest = { showAddGuestDialog = false },
            title = { Text("Adicionar Convidado") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newGuestName,
                        onValueChange = { newGuestName = it },
                        label = { Text("Nome do Convidado") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newGuestPhone,
                        onValueChange = { newGuestPhone = it },
                        label = { Text("WhatsApp / E-mail") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = newGuestAdults,
                            onValueChange = { newGuestAdults = it },
                            label = { Text("Adultos") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newGuestChildren,
                            onValueChange = { newGuestChildren = it },
                            label = { Text("Crianças") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGuestName.isNotBlank()) {
                            onAddGuest(
                                BabyShowerGuest(
                                    eventId = event.id,
                                    name = newGuestName,
                                    phoneOrEmail = newGuestPhone,
                                    adultsCount = newGuestAdults.toIntOrNull() ?: 1,
                                    childrenCount = newGuestChildren.toIntOrNull() ?: 0,
                                    status = RsvpStatus.CONFIRMED
                                )
                            )
                            newGuestName = ""
                            newGuestPhone = ""
                            showAddGuestDialog = false
                        }
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGuestDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // --- Modal: Add Gift ---
    if (showAddGiftDialog) {
        AlertDialog(
            onDismissRequest = { showAddGiftDialog = false },
            title = { Text("Adicionar Item à Lista de Presentes") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newGiftTitle,
                        onValueChange = { newGiftTitle = it },
                        label = { Text("Nome do Presente (ex: Fralda M)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newGiftCategory,
                        onValueChange = { newGiftCategory = it },
                        label = { Text("Categoria (Fraldas, Roupas, Higiene, Cota R$)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newGiftPrice,
                        onValueChange = { newGiftPrice = it },
                        label = { Text("Valor Estimado (R$)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newGiftUrl,
                        onValueChange = { newGiftUrl = it },
                        label = { Text("Link da Loja Online (Opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGiftTitle.isNotBlank()) {
                            onAddGift(
                                BabyShowerGift(
                                    eventId = event.id,
                                    title = newGiftTitle,
                                    category = newGiftCategory,
                                    priceEstimate = newGiftPrice.toDoubleOrNull() ?: 50.0,
                                    externalStoreUrl = newGiftUrl.ifBlank { null }
                                )
                            )
                            newGiftTitle = ""
                            newGiftUrl = ""
                            showAddGiftDialog = false
                        }
                    }
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGiftDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// --- TAB 1: Link Personalizado & Gerenciamento do Evento ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabLinkAndEvent(
    event: BabyShowerEvent,
    gifts: List<BabyShowerGift>,
    onUpdateEvent: (BabyShowerEvent) -> Unit,
    onOpenAddGiftDialog: () -> Unit,
    onToggleGiftReservation: (String, String?) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // --- Live Shareable Link Card ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                border = CardDefaults.outlinedCardBorder(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Link Oficial e Exclusivo do Evento",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (event.isSyncedWithWeb) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = if (event.isSyncedWithWeb) "PUBLICADO" else "NÃO PUBLICADO",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (event.isSyncedWithWeb) Color(0xFF15803D) else Color(0xFF854D0E),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!event.isSyncedWithWeb) {
                        // Ainda não publicado: explica como gerar o link em vez
                        // de mostrar uma URL que não existe no ar.
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Sua página ainda não está no ar.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF854D0E)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Preencha as informações do evento abaixo e toque em \"Sincronizar agora\" na aba 3. Publicação & Sync para gerar o link oficial do seu chá de bebê.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = event.fullWebUrl,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Link Chá de Bebê Nanei", event.fullWebUrl)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Link copiado com sucesso!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar Link")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.fullWebUrl))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Abrir no Site")
                        }

                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Acesse a página do nosso Chá de Bebê: ${event.fullWebUrl}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Link Nanei"))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compartilhar")
                        }
                    }
                    }
                }
            }
        }

        // --- Event Configuration Form ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Informações do Evento",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = event.title,
                        onValueChange = { onUpdateEvent(event.copy(title = it)) },
                        label = { Text("Título do Evento") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = event.customSlug,
                        onValueChange = {
                            val sanitized = it.lowercase().replace(" ", "-").replace("[^a-z0-9-]".toRegex(), "")
                            onUpdateEvent(event.copy(customSlug = sanitized))
                        },
                        label = { Text("Slug do Link (nanei.com.br/cha-de-bebe/slug)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = event.parentsNames,
                        onValueChange = { onUpdateEvent(event.copy(parentsNames = it)) },
                        label = { Text("Nome dos Pais") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = dateFormat.format(Date(event.eventDateMs)),
                            onValueChange = { /* Read only preview for date picker */ },
                            label = { Text("Data do Evento") },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
                            modifier = Modifier.weight(1.2f)
                        )

                        OutlinedTextField(
                            value = event.eventTimeStr,
                            onValueChange = { onUpdateEvent(event.copy(eventTimeStr = it)) },
                            label = { Text("Horário") },
                            singleLine = true,
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    OutlinedTextField(
                        value = event.locationName,
                        onValueChange = { onUpdateEvent(event.copy(locationName = it)) },
                        label = { Text("Nome do Local (ex: Salão de Festas)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = event.address,
                        onValueChange = { onUpdateEvent(event.copy(address = it)) },
                        label = { Text("Endereço Completo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = event.mapsUrl,
                        onValueChange = { onUpdateEvent(event.copy(mapsUrl = it)) },
                        label = { Text("Link Google Maps") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // --- Gift Registry Section (Lista de Presentes) ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Lista de Presentes Integrada",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${gifts.size} itens • Sincronizado com o site",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onOpenAddGiftDialog,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Adicionar Item")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (gifts.isEmpty()) {
                        Text(
                            text = "Nenhum presente cadastrado ainda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        gifts.forEach { gift ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (gift.isReserved) Color(0xFFF1F5F9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = gift.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Categoria: ${gift.category} ${if (gift.priceEstimate != null) "• ~R$ ${gift.priceEstimate.toInt()}" else ""}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (gift.isReserved) {
                                            Text(
                                                text = "🔒 Reservado por: ${gift.reservedByGuestName ?: "Convidado"}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFF991B1B)
                                            )
                                        }
                                    }

                                    TextButton(
                                        onClick = {
                                            if (gift.isReserved) {
                                                onToggleGiftReservation(gift.id, null)
                                            } else {
                                                onToggleGiftReservation(gift.id, "Mamãe (Reserva Manual)")
                                            }
                                        }
                                    ) {
                                        Text(if (gift.isReserved) "Liberar" else "Reservar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 2: Ferramenta de Convites Digitais e Canais de Envio ---
@Composable
private fun TabInvitationAndSending(
    event: BabyShowerEvent,
    guests: List<BabyShowerGuest>,
    gifts: List<BabyShowerGift>,
    onUpdateEvent: (BabyShowerEvent) -> Unit,
    onOpenAddGuestDialog: () -> Unit,
    onUpdateGuestStatus: (String, RsvpStatus) -> Unit,
    onDeleteGuest: (String) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("pt", "BR")) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // --- Theme / Template Selector ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Escolha o Estilo do Convite Digital",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(BabyShowerThemeStyle.values()) { theme ->
                            val isSelected = event.themeStyle == theme
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(theme.bgHex),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(theme.accentHex)) else CardDefaults.outlinedCardBorder(),
                                modifier = Modifier
                                    .width(130.dp)
                                    .clickable { onUpdateEvent(event.copy(themeStyle = theme)) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(theme.emoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = theme.title,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = Color(theme.textHex),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Visual Invitation Card Preview ---
        item {
            val theme = event.themeStyle
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(theme.bgHex)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(theme.accentHex)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(theme.emoji, fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = event.title.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color(theme.textHex),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = event.parentsNames,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color(theme.textHex).copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "“${event.invitationMessage}”",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = Color(theme.textHex),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Divider(color = Color(theme.accentHex).copy(alpha = 0.3f))

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null, tint = Color(theme.textHex), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${dateFormat.format(Date(event.eventDateMs))} às ${event.eventTimeStr}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(theme.textHex)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = Color(theme.textHex), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${event.locationName} - ${event.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(theme.textHex),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(theme.textHex)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Confirmar Presença (RSVP)",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // --- Multi-channel Sharing Tools ---
        item {
            val linkOuAviso = if (event.isSyncedWithWeb) {
                event.fullWebUrl
            } else {
                "(publique a página na aba \"3. Publicação & Sync\" para gerar o link de confirmação)"
            }
            val formattedMsg = """
                ${event.themeStyle.emoji} *${event.title}*
                
                _${event.invitationMessage}_
                
                📅 *Data:* ${dateFormat.format(Date(event.eventDateMs))} às ${event.eventTimeStr}
                📍 *Local:* ${event.locationName} (${event.address})
                
                🎁 *Lista de Presentes & Confirmação de Presença (RSVP):*
                $linkOuAviso
                
                Esperamos por você! ❤️
            """.trimIndent()

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "2. Enviar Convite aos Cuidadores e Convidados",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    if (!event.isSyncedWithWeb) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "⚠️ O link de confirmação ainda não existe. Sincronize na aba \"3. Publicação & Sync\" antes de enviar os convites.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF854D0E),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(formattedMsg)}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp não instalado. Copiando texto...", Toast.LENGTH_SHORT).show()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Convite Nanei", formattedMsg))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviar por WhatsApp", color = Color.White, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:")
                                    putExtra(Intent.EXTRA_SUBJECT, event.title)
                                    putExtra(Intent.EXTRA_TEXT, formattedMsg)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Nenhum app de email encontrado", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("E-mail")
                        }

                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Convite Nanei", formattedMsg))
                                Toast.makeText(context, "Texto do convite copiado!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copiar Texto")
                        }
                    }
                }
            }
        }

        // --- Guest List & RSVP Management Table ---
        item {
            val confirmedCount = guests.count { it.status == RsvpStatus.CONFIRMED }
            val adultsTotal = guests.filter { it.status == RsvpStatus.CONFIRMED }.sumOf { it.adultsCount }
            val childrenTotal = guests.filter { it.status == RsvpStatus.CONFIRMED }.sumOf { it.childrenCount }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "3. Gestão de Confirmados (RSVP)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "$confirmedCount confirmados • $adultsTotal adultos, $childrenTotal crianças",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onOpenAddGuestDialog,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Novo")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (guests.isEmpty()) {
                        Text(
                            text = "Nenhum convidado na lista ainda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        guests.forEach { guest ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (guest.source == "web") "🌐 ${guest.name}" else guest.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${guest.adultsCount} Adulto(s) • ${guest.childrenCount} Criança(s)${if (!guest.assignedGiftTitle.isNullOrEmpty()) " • Presente: ${guest.assignedGiftTitle}" else ""}${if (guest.source == "web") " • via página web" else ""}${if (!guest.dietaryRestrictions.isNullOrBlank()) " • ⚠️ ${guest.dietaryRestrictions}" else ""}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = Color(guest.status.colorHex).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = guest.status.label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(guest.status.colorHex),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    IconButton(onClick = { onDeleteGuest(guest.id) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remover", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 3: Publicação da página no site e sincronização real ---
@Composable
private fun TabPublishAndSync(
    event: BabyShowerEvent,
    guests: List<BabyShowerGuest>,
    gifts: List<BabyShowerGift>,
    syncLogs: List<SyncTestLog>,
    isSyncing: Boolean,
    onRunSyncTests: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // --- Status da página pública ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (event.isSyncedWithWeb) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = if (event.isSyncedWithWeb) Color(0xFF166534) else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (event.isSyncedWithWeb) "Sua página está no ar 🎉" else "Publique sua página na internet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (event.isSyncedWithWeb)
                            "Os convidados já podem confirmar presença e reservar presentes em:"
                        else
                            "Ao sincronizar, criamos uma página linda do seu chá de bebê no site www.nanei.com.br, com confirmação de presença (RSVP) e lista de presentes. Você recebe as confirmações aqui no app a cada sincronização.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (event.isSyncedWithWeb) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = event.fullWebUrl,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Link Chá de Bebê Nanei", event.fullWebUrl))
                                        Toast.makeText(context, "Link copiado!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        if (event.lastSyncedAtMs > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Última sincronização: " + SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(Date(event.lastSyncedAtMs)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // --- Botão de sincronização + histórico real ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sincronização com o site",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Envia suas alterações e busca novas confirmações",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onRunSyncTests,
                            enabled = !isSyncing,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sincronizar")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (syncLogs.isEmpty()) {
                        Text(
                            text = "Toque em \"Sincronizar\" para publicar sua página. Cada etapa é verificada de verdade e o resultado aparece aqui.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        syncLogs.forEach { log ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (log.isSuccess) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${if (log.isSuccess) "✅" else "❌"} ${log.stepName}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (log.isSuccess) Color(0xFF166534) else Color(0xFF991B1B)
                                        )
                                        Text(
                                            text = log.details,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.DarkGray
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White
                                    ) {
                                        Text(
                                            text = "${log.latencyMs}ms",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Dica de uso ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Dica",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sincronize sempre que alterar informações do evento, a lista de convidados ou os presentes — e também para buscar as confirmações feitas pelos convidados na página.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// --- TAB 4: Como funciona (explicação honesta da arquitetura real) ---
@Composable
private fun TabHowItWorks() {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📚 Como funciona a página do seu chá de bebê",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "O Nanei cria uma página exclusiva do seu evento na internet, sem você precisar saber nada de tecnologia.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Divider()

                    Text(
                        text = "1. Você prepara tudo no app",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Informações do evento, estilo do convite, lista de convidados e de presentes ficam salvos no seu aparelho — funcionam mesmo sem internet.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "2. A sincronização publica sua página",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Ao tocar em \"Sincronizar\", o app envia os dados de forma segura (HTTPS) para www.nanei.com.br e sua página fica disponível no link www.nanei.com.br/cha-de-bebe/seu-evento.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "3. Os convidados interagem pela página",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Quem recebe o link pode confirmar presença (com número de acompanhantes e restrições alimentares) e reservar presentes da lista — sem instalar nada e sem criar conta.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "4. Você recebe as novidades no app",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "A cada sincronização, o app baixa as confirmações e reservas feitas na página. Convidados que vieram do site aparecem marcados com 🌐 na sua lista.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Divider()

                    Text(
                        text = "🔒 Privacidade",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Somente os dados do evento que você escolheu publicar vão para o site. As informações de saúde e rotina do seu bebê ficam apenas no seu aparelho, conforme nossa Política de Privacidade (LGPD).",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
