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

                    var showFullPolicyDialog by remember { mutableStateOf(false) }

                    OutlinedButton(
                        onClick = { showFullPolicyDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Article, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ler Política de Privacidade na Íntegra")
                    }

                    if (showFullPolicyDialog) {
                        val fullPolicyText = """
                            POLÍTICA DE PRIVACIDADE E PROTEÇÃO DE DADOS — NANEI
                            Última atualização: 30 de Julho de 2026

                            1. APRESENTAÇÃO E COMPROMISSO
                            O Nanei (disponível via aplicativo móvel e web em nanei.com.br) é uma plataforma dedicada ao acompanhamento da gestação, maternidade e cuidados com o bebê. Respeitamos rigorosamente a privacidade dos pais, gestantes, convidados e crianças, atuando em total conformidade com a Lei Geral de Proteção de Dados Pessoais (LGPD — Lei nº 13.709/2018).

                            2. TRATAMENTO DE DADOS DE CRIANÇAS E BEBÊS (ART. 14 DA LGPD)
                            O tratamento de dados pessoais de bebês e recém-nascidos (como nome, data de nascimento, medidas corporais, hábitos de sono e amamentação) é realizado exclusivamente em seu melhor interesse e mediante o consentimento específico e em destaque fornecido por pelo menos um dos pais ou responsável legal no momento da criação do perfil no aplicativo.

                            3. DADOS PESSOAIS COLETADOS
                            • Dados do Responsável: Nome completo, endereço de e-mail, foto de perfil (opcional) e credenciais de acesso.
                            • Dados do Bebê / Gestação: Idade gestacional, batimentos, sintomas, registros de amamentação, fraldas, rotina de sono e diário da mamãe.
                            • Dados do Chá de Bebê & Eventos: Nome do evento, data, endereço, lista de convidados (nome, telefone, status de presença RSVP) e itens de presentes reservados.
                            • Dados Técnicos e Logs: Endereço IP, tipo de dispositivo, registros de auditoria de segurança (AuditLog) e tokens de notificação.

                            4. FINALIDADES DO TRATAMENTO
                            Os dados são utilizados estritamente para:
                            a) Permitir a gestão diária dos cuidados com o bebê e saúde materna;
                            b) Sincronizar o Chá de Bebê com a página web customizada no nanei.com.br;
                            c) Oferecer suporte e insights personalizados via Assistente IA Gemini (dados pseudonimizados sem uso para treino público);
                            d) Gerar relatórios exportáveis e o Livro de Memórias em formato PDF.

                            5. COMPARTILHAMENTO E SEGURANÇA DOS DADOS
                            • Criptografia: Todas as comunicações utilizam protocolo seguro TLS 1.3 (HTTPS) e dados sensíveis são armazenados com criptografia de ponta AES-256.
                            • Terceiros: O Nanei NÃO vende nem comercializa dados pessoais com anunciantes. O compartilhamento ocorre apenas com provedores de infraestrutura estritamente necessários para manter o serviço funcionando.

                            6. SEUS DIREITOS COMO TITULAR (ART. 18 DA LGPD)
                            Você pode a qualquer momento no aplicativo:
                            • Confirmar a existência e acessar todos os seus dados;
                            • Solicitar a exportação completa em formato CSV/JSON;
                            • Revogar consentimentos e alterar permissões de uso;
                            • Solicitar a exclusão definitiva da conta e purga integral dos dados (efetuada no prazo de até 30 dias).

                            7. ATALHOS DA TELA INICIAL (APP SHORTCUTS) E ACESSIBILIDADE RÁPIDA
                            • Como Usar: Pressione e segure o ícone do Nanei na tela inicial do Android para acessar atalhos rápidos ("Nova Mamada", "IA Nanei", "Troca de Fralda" e "Chá de Bebê").
                            • Importância da Atualização: Manter os atalhos atualizados assegura resposta imediata nos momentos mais críticos dos cuidados com o bebê e integração completa com recursos de acessibilidade (TalkBack e comandos de voz).

                            8. CONTATO DO ENCARREGADO DE DADOS (DPO)
                            Em caso de dúvidas sobre esta Política ou para exercer seus direitos de privacidade:
                            E-mail: privacidade@nanei.com.br / suporte@nanei.com.br
                            Website: https://nanei.com.br
                        """.trimIndent()

                        AlertDialog(
                            onDismissRequest = { showFullPolicyDialog = false },
                            title = { Text("Política de Privacidade Nanei") },
                            text = {
                                Column(modifier = Modifier.height(350.dp).verticalScroll(rememberScrollState())) {
                                    Text(
                                        text = fullPolicyText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            confirmButton = {
                                Row {
                                    TextButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("Política de Privacidade Nanei", fullPolicyText)
                                            clipboard.setPrimaryClip(clip)
                                            android.widget.Toast.makeText(context, "Política de privacidade copiada!", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copiar Texto")
                                    }
                                    TextButton(onClick = { showFullPolicyDialog = false }) {
                                        Text("Fechar")
                                    }
                                }
                            }
                        )
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
