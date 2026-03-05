package com.example.controleestoque.ui.screens.relatorios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.controleestoque.data.local.entity.Produto
import com.example.controleestoque.ui.theme.ColorProximoVencer
import com.example.controleestoque.ui.theme.ColorVencido
import com.example.controleestoque.utils.DateUtils
import com.example.controleestoque.viewmodel.ConfiguracoesViewModel
import com.example.controleestoque.viewmodel.ProdutoViewModel

/**
 * Tela de relatórios e visão geral do estoque.
 * Exibe totais, produtos vencidos e produtos próximos do vencimento.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatoriosScreen(
    onVoltar: () -> Unit,
    produtoViewModel: ProdutoViewModel = hiltViewModel(),
    configViewModel: ConfiguracoesViewModel = hiltViewModel()
) {
    val totalProdutos by produtoViewModel.totalProdutos.collectAsState()
    val diasAntesVencimento by configViewModel.diasAntesVencimento.collectAsState()
    val quantidadeMinimaGlobal by configViewModel.quantidadeMinimaGlobal.collectAsState()

    // Produtos vencidos
    val agora = remember { DateUtils.inicioDeHoje() }
    val limiteVencimento = remember(diasAntesVencimento) { DateUtils.agoraMaisDias(diasAntesVencimento) }

    val vencidos by produtoViewModel.observarVencidos(agora).collectAsState(initial = emptyList())
    val proximosVencer by produtoViewModel.observarProximosVencimento(limiteVencimento, agora).collectAsState(initial = emptyList())
    val estoqueTotal by produtoViewModel.produtos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relatórios") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cards de resumo
            item {
                Text(
                    "Visão Geral",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryCard(
                        title = "Total de Produtos",
                        value = totalProdutos.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Vencidos",
                        value = vencidos.size.toString(),
                        valueColor = ColorVencido,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Próx. Vencer",
                        value = proximosVencer.size.toString(),
                        valueColor = ColorProximoVencer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Produtos vencidos
            if (vencidos.isNotEmpty()) {
                item {
                    Text(
                        "⚠️ Produtos Vencidos (${vencidos.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = ColorVencido,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(vencidos) { produto ->
                    ProdutoRelatorioItem(
                        produto = produto,
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                }
            }

            // Produtos próximos do vencimento
            if (proximosVencer.isNotEmpty()) {
                item {
                    Text(
                        "🕐 Próximos do Vencimento - ${diasAntesVencimento} dias (${proximosVencer.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = ColorProximoVencer,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(proximosVencer) { produto ->
                    ProdutoRelatorioItem(
                        produto = produto,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }
            }

            // Todos em estoque baixo
            val estoqueBaixo = estoqueTotal.filter { p ->
                val min = if (p.quantidadeMinima > 0) p.quantidadeMinima else quantidadeMinimaGlobal
                p.quantidadeAtual <= min
            }
            if (estoqueBaixo.isNotEmpty()) {
                item {
                    Text(
                        "📦 Estoque Baixo (${estoqueBaixo.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(estoqueBaixo) { produto ->
                    ProdutoRelatorioItem(produto = produto)
                }
            }

            if (vencidos.isEmpty() && proximosVencer.isEmpty() && estoqueBaixo.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Tudo em ordem! Nenhum alerta no momento.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Card de resumo numérico */
@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = valueColor, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Item de produto no relatório */
@Composable
private fun ProdutoRelatorioItem(
    produto: Produto,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(produto.nome, fontWeight = FontWeight.Medium)
                if (produto.dataValidade != 0L) {
                    Text(
                        "Validade: ${DateUtils.formatarData(produto.dataValidade)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Text(
                "${produto.quantidadeAtual} ${produto.unidade}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
