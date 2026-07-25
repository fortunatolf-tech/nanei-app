package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * F1 — Onboarding e Consentimento (RF-ACC-01, 04, 08, 09)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingModal(
    onDismiss: () -> Unit,
    onCompleteOnboarding: (babyName: String, birthMs: Long, estMs: Long, gender: String, avatarUri: String?) -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Welcome/Auth, 2: Consents, 3: Baby Details, 4: Tour & PWA

    // Form states
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Consents
    var consentBabyData by remember { mutableStateOf(true) } // MANDATORY LGPD Art. 14
    var consentNotifications by remember { mutableStateOf(true) }
    var consentPhotos by remember { mutableStateOf(true) }
    var consentAnalytics by remember { mutableStateOf(false) }

    // Baby Info
    var babyName by remember { mutableStateOf("Bebê Nanei") }
    var gender by remember { mutableStateOf("Ainda não sei") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("onboarding_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bem-vindo ao Nanei 💙",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Etapa $step de 4",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { step / 4f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            when (step) {
                1 -> {
                    // Step 1: Welcome & Account Creation (F1)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChildCare,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Acompanhamento Inteligente de Sono e Cuidados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Sua rotina materna e fraterna mais leve com inteligência artificial e sincronização em tempo real.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("E-mail do Responsável") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_onboarding_email")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Senha Segura") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_onboarding_password")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { step = 2 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_onboarding_step1_next"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Criar Conta e Continuar", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                2 -> {
                    // Step 2: Granular Consents & LGPD (RF-ACC-04, 08, 09)
                    Column {
                        Text(
                            text = "Termos & Consentimentos Granulares",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Respeitamos sua privacidade conforme o Art. 14 da LGPD.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        ConsentCardRow(
                            title = "Tratamento de Dados do Bebê (Obrigatório)",
                            subtitle = "Base legal: consentimento do responsável para registros de sono e mamadas.",
                            checked = consentBabyData,
                            onCheckedChange = { consentBabyData = it },
                            isMandatory = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ConsentCardRow(
                            title = "Notificações de Sono SweetSpot (Opcional)",
                            subtitle = "Receba alertas 15 minutos antes da janela ideal de sono.",
                            checked = consentNotifications,
                            onCheckedChange = { consentNotifications = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ConsentCardRow(
                            title = "Acesso a Fotos e Anexos (Opcional)",
                            subtitle = "Permite salvar fotos nos marcos de desenvolvimento.",
                            checked = consentPhotos,
                            onCheckedChange = { consentPhotos = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ConsentCardRow(
                            title = "Métricas de Analytics (Opcional)",
                            subtitle = "Ajude a melhorar o aplicativo com dados de uso anônimos.",
                            checked = consentAnalytics,
                            onCheckedChange = { consentAnalytics = it }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { if (consentBabyData) step = 3 },
                            enabled = consentBabyData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_onboarding_step2_next"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                if (consentBabyData) "Aceitar e Cadastrar Bebê" else "Obrigatório aceitar dados do bebê",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                3 -> {
                    // Step 3: Register Baby Information
                    Column {
                        Text(
                            text = "Cadastrar seu Bebê",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Utilizado para calcular idade corrigida e janelas SweetSpot.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = babyName,
                            onValueChange = { babyName = it },
                            label = { Text("Nome do Bebê") },
                            leadingIcon = { Icon(Icons.Default.Face, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_baby_name")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Sexo do Bebê:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = gender == "Feminino",
                                    onClick = { gender = "Feminino" },
                                    label = { Text("Feminino 👧") }
                                )
                                FilterChip(
                                    selected = gender == "Masculino",
                                    onClick = { gender = "Masculino" },
                                    label = { Text("Masculino 👦") }
                                )
                            }
                            FilterChip(
                                selected = gender == "Ainda não sei",
                                onClick = { gender = "Ainda não sei" },
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
                                modifier = Modifier.weight(1f).testTag("btn_onboarding_select_photo")
                            ) {
                                Text(if (photoUri != null) "Trocar Foto 📸" else "Adicionar Foto / Ultrassom 📸")
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { step = 4 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_onboarding_step3_next"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Avançar para o Tour", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                4 -> {
                    // Step 4: Installation / PWA & Tour
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Tudo Pronto! Tour Rápido 🚀",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tour items
                        TourStepItem(
                            stepNumber = "1",
                            title = "Registro Rápido em 2 Toques",
                            description = "Registre amamentação, fraldas e sonecas com apenas uma mão na tela inicial."
                        )
                        TourStepItem(
                            stepNumber = "2",
                            title = "Previsão SweetSpot",
                            description = "Saiba exatamente quando colocar seu bebê para dormir antes do efeito vulcão."
                        )
                        TourStepItem(
                            stepNumber = "3",
                            title = "IA e Relatórios Pediátricos",
                            description = "Conversas por voz, consultas de medicamentos e relatórios em PDF com curvas da OMS."
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // PWA / App Home shortcut info
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Smartphone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Dica: Adicione o Nanei à tela inicial para notificações em tempo real e acesso offline instantâneo.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val now = System.currentTimeMillis()
                                onCompleteOnboarding(babyName, now, now, gender, photoUri?.toString())
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_complete_onboarding"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("INICIAR NANEI AGORA", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ConsentCardRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isMandatory: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = checked,
                onCheckedChange = if (isMandatory) null else onCheckedChange,
                enabled = !isMandatory
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TourStepItem(stepNumber: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * F4 — Convite de Cuidador e Permissões (RF-FAM-02, 03)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyManagementDialog(
    onDismiss: () -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Cuidador") } // "Admin", "Cuidador", "Leitor"
    var showSuccessInvite by remember { mutableStateOf(false) }
    var generatedTokenLink by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Família & Cuidadores (Sync)")
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Convide mães, pais, avós ou babás para sincronizar registros em tempo real.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("E-mail do Cuidador") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_invite_email")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Papel do Cuidador:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedRole == "Cuidador",
                        onClick = { selectedRole = "Cuidador" },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) { Text("Cuidador") }
                    SegmentedButton(
                        selected = selectedRole == "Admin",
                        onClick = { selectedRole = "Admin" },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) { Text("Admin") }
                    SegmentedButton(
                        selected = selectedRole == "Leitor",
                        onClick = { selectedRole = "Leitor" },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) { Text("Leitor") }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (emailInput.isNotEmpty()) {
                            generatedTokenLink = "https://nanei.app/invite?token=NANEI_${System.currentTimeMillis()}"
                            showSuccessInvite = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("btn_send_invite"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gerar Convite Único (48h)", fontWeight = FontWeight.Bold)
                }

                if (showSuccessInvite) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "✨ Convite criado com sucesso!",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = generatedTokenLink,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Membros Ativos na Família:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                CaregiverMemberItem("Você (Dispositivo Principal)", "Admin", "🟢 Conectado")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}

@Composable
private fun CaregiverMemberItem(name: String, role: String, status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text(text = role, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
        Text(text = status, style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981))
    }
}

/**
 * F8 — Modo Escuta e Detector de Choro (RF-SND-05)
 */
@Composable
fun SoundListenModeDialog(
    onDismiss: () -> Unit
) {
    var isListeningActive by remember { mutableStateOf(false) }
    var sensitivity by remember { mutableFloatStateOf(0.7f) } // 0.0 to 1.0
    var deviceMode by remember { mutableStateOf("PAIS") } // "BABY_MONITOR" or "PAIS"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Modo Escuta & Detector de Choro")
            }
        },
        text = {
            Column {
                Text(
                    text = "Processamento 100% local no microfone. Nenhum áudio é gravado ou enviado para servidores.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Modo do Dispositivo:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = deviceMode == "PAIS",
                        onClick = { deviceMode = "PAIS" },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text("Dispositivo dos Pais") }
                    SegmentedButton(
                        selected = deviceMode == "BABY_MONITOR",
                        onClick = { deviceMode = "BABY_MONITOR" },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text("Junto ao Bebê") }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Sensibilidade do Detector: ${(sensitivity * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = sensitivity,
                    onValueChange = { sensitivity = it },
                    valueRange = 0.1f..1.0f
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isListeningActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isListeningActive) Icons.Default.GraphicEq else Icons.Default.MicOff,
                                contentDescription = null,
                                tint = if (isListeningActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isListeningActive) "Escuta Ativa 🟢" else "Monitoramento Desativado",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (isListeningActive) "Aguardando ruídos de choro..." else "Toque para ligar a escuta",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Switch(
                            checked = isListeningActive,
                            onCheckedChange = { isListeningActive = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Concluído") }
        }
    )
}
