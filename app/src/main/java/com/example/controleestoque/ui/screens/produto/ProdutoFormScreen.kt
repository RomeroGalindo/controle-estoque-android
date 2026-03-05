package com.example.controleestoque.ui.screens.produto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.controleestoque.data.local.entity.Produto
import com.example.controleestoque.viewmodel.ProdutoViewModel

/**
 * Tela de cadastro e edição de produto.
 * Se [produtoId] == 0, trata-se de um novo produto; caso contrário, edição.
 * Contém apenas informações do produto. Quantidade, unidade, data de validade
 * e localização são gerenciados pela tela de movimentações.
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

    // Campos do formulário (apenas informações do produto)
    var nome by remember { mutableStateOf("") }
    var codigoBarras by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }
    var quantidadeMinima by remember { mutableStateOf("0") }

    // Preenche o formulário ao carregar produto existente
    LaunchedEffect(produtoExistente) {
        produtoExistente?.let { p ->
            nome = p.nome
            codigoBarras = p.codigoBarras ?: ""
            categoria = p.categoria
            observacoes = p.observacoes
            quantidadeMinima = p.quantidadeMinima.toString()
        }
    }

    // Erros de validação
    var erroNome by remember { mutableStateOf("") }

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
                    if (!valido) return@Button

                    val produto = Produto(
                        id = if (isEdicao) produtoId else 0L,
                        nome = nome.trim(),
                        codigoBarras = codigoBarras.ifBlank { null },
                        categoria = categoria.trim(),
                        quantidadeAtual = produtoExistente?.quantidadeAtual ?: 0,
                        unidade = produtoExistente?.unidade ?: "unidade",
                        dataValidade = produtoExistente?.dataValidade ?: 0L,
                        localizacao = produtoExistente?.localizacao ?: "",
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
}
