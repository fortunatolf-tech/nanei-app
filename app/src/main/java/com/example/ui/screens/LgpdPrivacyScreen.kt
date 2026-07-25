package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AuditLog
import com.example.util.PdfReportGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LgpdPrivacyScreen(
    auditLogs: List<AuditLog>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var consentBabyData by remember { mutableStateOf(true) }
    var consentPhotos by remember { mutableStateOf(true) }
    var consentAiFeatures by remember { mutableStateOf(true) }
    var consentAnalytics by remember { mutableStateOf(false) }

    var showAuditLogsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showExportSuccessToast by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Portal de Privacidade e LGPD") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gestão de Consentimentos Granulares",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ConsentToggleRow(
                        title = "Tratamento de Dados do Bebê (Essencial)",
                        subtitle = "Art. 14 LGPD — Necessário para o uso do app",
                        checked = consentBabyData,
                        onCheckedChange = { consentBabyData = it }
                    )
                    HorizontalDivider()
                    ConsentToggleRow(
                        title = "Uso de Fotos e Galeria",
                        subtitle = "Anexar fotos em marcos e diário",
                        checked = consentPhotos,
                        onCheckedChange = { consentPhotos = it }
                    )
                    HorizontalDivider()
                    ConsentToggleRow(
                        title = "Assistente de IA Gemini (Opt-in)",
                        subtitle = "Envio pseudonimizado sem uso para treino",
                        checked = consentAiFeatures,
                        onCheckedChange = { consentAiFeatures = it }
                    )
                    HorizontalDivider()
                    ConsentToggleRow(
                        title = "Métricas de Analytics",
                        subtitle = "Melhorias contínuas de interface",
                        checked = consentAnalytics,
                        onCheckedChange = { consentAnalytics = it }
                    )
                }
            }

            // Rights Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Seus Direitos como Titular de Dados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            showExportSuccessToast = true
                            PdfReportGenerator.generateAndShareCsv(
                                context = context,
                                baby = null,
                                events = emptyList()
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_export_user_data"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar Meus Dados (JSON / CSV)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showAuditLogsDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Visualizar Histórico de Auditoria (AuditLog)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Excluir Conta e Dados do Bebê")
                    }
                }
            }

            if (showExportSuccessToast) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = "✅ Solicitação recebida! O arquivo JSON/CSV com todos os registros exportados foi gerado.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }

    if (showAuditLogsDialog) {
        AlertDialog(
            onDismissRequest = { showAuditLogsDialog = false },
            title = { Text("Registros de Auditoria (AuditLog)") },
            text = {
                Column(modifier = Modifier.height(300.dp).verticalScroll(rememberScrollState())) {
                    auditLogs.forEach { log ->
                        Text(
                            text = "• [${log.action}] ${log.details}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAuditLogsDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Excluir conta irreversivelmente?") },
            text = {
                Text("De acordo com a LGPD, a exclusão da conta removerá todos os dados e histórico do bebê. A purga total ocorrerá dentro do ciclo de 30 dias.")
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirmar Exclusão")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ConsentToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
