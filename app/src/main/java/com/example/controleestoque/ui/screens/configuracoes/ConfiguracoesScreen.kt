package com.example.controleestoque.ui.screens.configuracoes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.controleestoque.viewmodel.ConfiguracoesViewModel

/**
 * Tela de Configurações do aplicativo.
 * Permite ajustar o número de dias para alerta de vencimento
 * e a quantidade mínima de estoque global.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesScreen(
    onVoltar: () -> Unit,
    viewModel: ConfiguracoesViewModel = hiltViewModel()
) {
    val diasAntesVencimento by viewModel.diasAntesVencimento.collectAsState()
    val quantidadeMinimaGlobal by viewModel.quantidadeMinimaGlobal.collectAsState()

    // Sliders trabalham com Float
    var diasSlider by remember(diasAntesVencimento) { mutableFloatStateOf(diasAntesVencimento.toFloat()) }
    var qtdSlider by remember(quantidadeMinimaGlobal) { mutableFloatStateOf(quantidadeMinimaGlobal.toFloat()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Alertas de Validade",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Configuração de dias antes do vencimento
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Dias antes do vencimento para alertar",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "${diasSlider.toInt()} dias",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = diasSlider,
                        onValueChange = { diasSlider = it },
                        onValueChangeFinished = {
                            viewModel.setDiasAntesVencimento(diasSlider.toInt())
                        },
                        valueRange = 1f..30f,
                        steps = 28,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Produtos vencendo nos próximos ${diasSlider.toInt()} dias aparecerão em laranja e gerarão notificações.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Configuração de estoque mínimo global
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Quantidade mínima global para alerta",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "${qtdSlider.toInt()} unid.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = qtdSlider,
                        onValueChange = { qtdSlider = it },
                        onValueChangeFinished = {
                            viewModel.setQuantidadeMinimaGlobal(qtdSlider.toInt())
                        },
                        valueRange = 1f..50f,
                        steps = 48,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Produtos com estoque abaixo de ${qtdSlider.toInt()} serão marcados como estoque baixo. " +
                                "Configurações por produto têm prioridade.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // Informações sobre notificações
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Sobre as Notificações",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "O aplicativo verifica diariamente os produtos e envia notificações locais para produtos vencidos ou próximos do vencimento. " +
                                "As verificações ocorrem automaticamente pelo WorkManager, mesmo com o app fechado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
