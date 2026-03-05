package com.example.controleestoque.ui.screens.movimentacao

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.controleestoque.data.local.entity.Movimentacao
import com.example.controleestoque.data.local.entity.TipoMovimentacao
import com.example.controleestoque.utils.DateUtils
import com.example.controleestoque.viewmodel.MovimentacaoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tela de movimentações de estoque.
 * Permite registrar entradas e saídas e exibe o histórico completo.
 * Inclui campos de quantidade, unidade, data de validade e localização.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimentacaoScreen(
    onVoltar: () -> Unit,
    viewModel: MovimentacaoViewModel = hiltViewModel()
) {
    val produtos by viewModel.produtos.collectAsState()
    val historico by viewModel.historico.collectAsState()
    val produtoSelecionadoId by viewModel.produtoSelecionadoId.collectAsState()
    val tipoMovimentacao by viewModel.tipoMovimentacao.collectAsState()
    val quantidade by viewModel.quantidade.collectAsState()
    val unidade by viewModel.unidade.collectAsState()
    val dataValidadeMs by viewModel.dataValidadeMs.collectAsState()
    val localizacao by viewModel.localizacao.collectAsState()
    val observacoes by viewModel.observacoes.collectAsState()
    val carregando by viewModel.carregando.collectAsState()
    val mensagem by viewModel.mensagem.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(mensagem) {
        mensagem?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparMensagem()
        }
    }

    var showForm by remember { mutableStateOf(false) }
    var produtoDropdownExpanded by remember { mutableStateOf(false) }

    // Unidade dropdown
    val unidades = listOf("unidade", "caixa", "kg", "g", "litro", "ml", "pacote", "dúzia", "par")
    var unidadeExpanded by remember { mutableStateOf(false) }

    // DatePicker
    var showDatePicker by remember { mutableStateOf(false) }
    val dataValidadeStr = if (dataValidadeMs != 0L) DateUtils.formatarData(dataValidadeMs) else ""

    val produtoSelecionado = produtos.find { it.id == produtoSelecionadoId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movimentações") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showForm = !showForm }) {
                Icon(
                    if (showForm) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (showForm) "Fechar" else "Nova movimentação"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Formulário de nova movimentação (colapsável)
            if (showForm) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Nova Movimentação",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Seleção de produto
                        ExposedDropdownMenuBox(
                            expanded = produtoDropdownExpanded,
                            onExpandedChange = { produtoDropdownExpanded = !produtoDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = produtoSelecionado?.nome ?: "Selecione um produto",
                                onValueChange = {},
                                label = { Text("Produto") },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(produtoDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = produtoDropdownExpanded,
                                onDismissRequest = { produtoDropdownExpanded = false }
                            ) {
                                produtos.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text("${p.nome} (Estoque: ${p.quantidadeAtual} ${p.unidade})") },
                                        onClick = {
                                            viewModel.setProdutoSelecionado(p.id)
                                            produtoDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Tipo: Entrada ou Saída
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TipoMovimentacao.entries.forEach { tipo ->
                                FilterChip(
                                    selected = tipoMovimentacao == tipo,
                                    onClick = { viewModel.setTipoMovimentacao(tipo) },
                                    label = { Text(tipo.name) },
                                    leadingIcon = {
                                        Icon(
                                            if (tipo == TipoMovimentacao.ENTRADA) Icons.Default.Add
                                            else Icons.Default.Remove,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }

                        // Quantidade e Unidade em linha
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = quantidade,
                                onValueChange = viewModel::setQuantidade,
                                label = { Text("Quantidade") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            // Dropdown de unidade
                            ExposedDropdownMenuBox(
                                expanded = unidadeExpanded,
                                onExpandedChange = { unidadeExpanded = !unidadeExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = unidade,
                                    onValueChange = {},
                                    label = { Text("Unidade") },
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unidadeExpanded) },
                                    modifier = Modifier.menuAnchor(),
                                    singleLine = true
                                )
                                ExposedDropdownMenu(
                                    expanded = unidadeExpanded,
                                    onDismissRequest = { unidadeExpanded = false }
                                ) {
                                    unidades.forEach { u ->
                                        DropdownMenuItem(
                                            text = { Text(u) },
                                            onClick = { viewModel.setUnidade(u); unidadeExpanded = false }
                                        )
                                    }
                                }
                            }
                        }

                        // Data de validade
                        OutlinedTextField(
                            value = dataValidadeStr,
                            onValueChange = {},
                            label = { Text("Data de Validade") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "Selecionar data")
                                }
                            }
                        )

                        // Localização
                        OutlinedTextField(
                            value = localizacao,
                            onValueChange = viewModel::setLocalizacao,
                            label = { Text("Localização") },
                            placeholder = { Text("Ex.: Prateleira 1, Depósito, Geladeira") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Observações
                        OutlinedTextField(
                            value = observacoes,
                            onValueChange = viewModel::setObservacoes,
                            label = { Text("Observações") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )

                        // Botão confirmar
                        Button(
                            onClick = { viewModel.registrarMovimentacao() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !carregando
                        ) {
                            if (carregando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Confirmar Movimentação")
                            }
                        }
                    }
                }
            }

            // Histórico de movimentações
            Text(
                "Histórico",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (historico.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nenhuma movimentação registrada",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(historico, key = { it.id }) { mov ->
                        MovimentacaoItem(
                            movimentacao = mov,
                            nomeProduto = produtos.find { it.id == mov.produtoId }?.nome ?: "Produto #${mov.produtoId}"
                        )
                    }
                }
            }
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (dataValidadeMs != 0L) dataValidadeMs else null
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        viewModel.setDataValidadeMs(ms)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/** Item da lista de histórico de movimentações */
@Composable
private fun MovimentacaoItem(movimentacao: Movimentacao, nomeProduto: String) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    val isEntrada = movimentacao.tipo == TipoMovimentacao.ENTRADA

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isEntrada) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = movimentacao.tipo.name,
                tint = if (isEntrada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(nomeProduto, fontWeight = FontWeight.Medium)
                Text(
                    "${movimentacao.tipo.name}: ${movimentacao.quantidade}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isEntrada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(
                    dateFormat.format(Date(movimentacao.dataHora)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (movimentacao.observacoes.isNotBlank()) {
                    Text(
                        movimentacao.observacoes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
