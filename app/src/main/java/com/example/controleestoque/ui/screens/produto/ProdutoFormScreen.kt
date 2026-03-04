package com.example.controleestoque.ui.screens.produto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.controleestoque.data.local.entity.Produto
import com.example.controleestoque.utils.DateUtils
import com.example.controleestoque.viewmodel.ProdutoViewModel
import java.util.Calendar

/**
 * Tela de cadastro e edição de produto.
 * Se [produtoId] == 0, trata-se de um novo produto; caso contrário, edição.
 * Inclui validação de campos obrigatórios e DatePicker para data de validade.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdutoFormScreen(
    produtoId: Long,
    onVoltar: () -> Unit,
    viewModel: ProdutoViewModel = hiltViewModel()
) {
    val isEdicao = produtoId != 0L
    val produtoExistente by viewModel.buscarPorId(produtoId).collectAsState(initial = null)

    // Campos do formulário
    var nome by remember { mutableStateOf("") }
    var codigoBarras by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("") }
    var unidade by remember { mutableStateOf("unidade") }
    var dataValidadeMs by remember { mutableStateOf(0L) }
    var dataValidadeStr by remember { mutableStateOf("") }
    var localizacao by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }
    var quantidadeMinima by remember { mutableStateOf("0") }

    // Preenche o formulário ao carregar produto existente
    LaunchedEffect(produtoExistente) {
        produtoExistente?.let { p ->
            nome = p.nome
            codigoBarras = p.codigoBarras ?: ""
            categoria = p.categoria
            quantidade = p.quantidadeAtual.toString()
            unidade = p.unidade
            dataValidadeMs = p.dataValidade
            dataValidadeStr = DateUtils.formatarData(p.dataValidade)
            localizacao = p.localizacao
            observacoes = p.observacoes
            quantidadeMinima = p.quantidadeMinima.toString()
        }
    }

    // Erros de validação
    var erroNome by remember { mutableStateOf("") }
    var erroQuantidade by remember { mutableStateOf("") }
    var erroData by remember { mutableStateOf("") }

    // DatePicker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (dataValidadeMs != 0L) dataValidadeMs else null
    )

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val mensagem by viewModel.mensagem.collectAsState()
    LaunchedEffect(mensagem) {
        mensagem?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparMensagem()
            if (it.contains("sucesso")) onVoltar()
        }
    }

    // Opções de unidade
    val unidades = listOf("unidade", "caixa", "kg", "g", "litro", "ml", "pacote", "dúzia", "par")
    var unidadeExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdicao) "Editar Produto" else "Novo Produto") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Nome (obrigatório)
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it; erroNome = "" },
                label = { Text("Nome do Produto *") },
                isError = erroNome.isNotBlank(),
                supportingText = { if (erroNome.isNotBlank()) Text(erroNome) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Código de barras (opcional)
            OutlinedTextField(
                value = codigoBarras,
                onValueChange = { codigoBarras = it },
                label = { Text("Código de Barras (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Categoria
            OutlinedTextField(
                value = categoria,
                onValueChange = { categoria = it },
                label = { Text("Categoria") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Quantidade e Unidade em linha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = quantidade,
                    onValueChange = { quantidade = it; erroQuantidade = "" },
                    label = { Text("Quantidade *") },
                    isError = erroQuantidade.isNotBlank(),
                    supportingText = { if (erroQuantidade.isNotBlank()) Text(erroQuantidade) },
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
                                onClick = { unidade = u; unidadeExpanded = false }
                            )
                        }
                    }
                }
            }

            // Data de validade (obrigatória)
            OutlinedTextField(
                value = dataValidadeStr,
                onValueChange = {},
                label = { Text("Data de Validade *") },
                isError = erroData.isNotBlank(),
                supportingText = { if (erroData.isNotBlank()) Text(erroData) },
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
                onValueChange = { localizacao = it },
                label = { Text("Localização") },
                placeholder = { Text("Ex.: Prateleira 1, Depósito, Geladeira") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Quantidade mínima
            OutlinedTextField(
                value = quantidadeMinima,
                onValueChange = { quantidadeMinima = it },
                label = { Text("Quantidade Mínima para Alerta (0 = global)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Observações
            OutlinedTextField(
                value = observacoes,
                onValueChange = { observacoes = it },
                label = { Text("Observações") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botão Salvar
            Button(
                onClick = {
                    // Validação
                    var valido = true
                    if (nome.isBlank()) { erroNome = "Nome é obrigatório"; valido = false }
                    val qtd = quantidade.toIntOrNull()
                    if (qtd == null || qtd < 0) { erroQuantidade = "Quantidade inválida"; valido = false }
                    if (dataValidadeMs == 0L) { erroData = "Data de validade é obrigatória"; valido = false }
                    if (!valido) return@Button

                    val produto = Produto(
                        id = if (isEdicao) produtoId else 0L,
                        nome = nome.trim(),
                        codigoBarras = codigoBarras.ifBlank { null },
                        categoria = categoria.trim(),
                        quantidadeAtual = qtd!!,
                        unidade = unidade,
                        dataValidade = dataValidadeMs,
                        localizacao = localizacao.trim(),
                        observacoes = observacoes.trim(),
                        quantidadeMinima = quantidadeMinima.toIntOrNull() ?: 0
                    )
                    viewModel.salvarProduto(produto)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEdicao) "Atualizar" else "Cadastrar")
            }
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        dataValidadeMs = ms
                        dataValidadeStr = DateUtils.formatarData(ms)
                        erroData = ""
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
