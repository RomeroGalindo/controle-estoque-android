package com.example.controleestoque.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.controleestoque.ui.components.ProdutoCard
import com.example.controleestoque.viewmodel.FiltroProduto
import com.example.controleestoque.viewmodel.OrdemProduto
import com.example.controleestoque.viewmodel.ProdutoViewModel

/**
 * Tela inicial que exibe a lista de produtos com busca, filtros e ordenação.
 * Cada produto exibe indicadores visuais de validade e estoque.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNovoProduto: () -> Unit,
    onEditarProduto: (Long) -> Unit,
    onMovimentacoes: () -> Unit,
    onRelatorios: () -> Unit,
    onConfiguracoes: () -> Unit,
    viewModel: ProdutoViewModel = hiltViewModel()
) {
    val produtos by viewModel.produtos.collectAsState()
    val busca by viewModel.busca.collectAsState()
    val filtro by viewModel.filtro.collectAsState()
    val ordem by viewModel.ordem.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val categoriaFiltro by viewModel.categoriaFiltro.collectAsState()
    val totalProdutos by viewModel.totalProdutos.collectAsState()
    val mensagem by viewModel.mensagem.collectAsState()
    val diasAntesVencimento by viewModel.diasAntesVencimento.collectAsState()
    val quantidadeMinimaGlobal by viewModel.quantidadeMinimaGlobal.collectAsState()

    var showFiltroMenu by remember { mutableStateOf(false) }
    var showOrdemMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }

    // Snackbar host
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(mensagem) {
        mensagem?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparMensagem()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Controle de Estoque") },
                actions = {
                    // Botão de ordenação
                    Box {
                        IconButton(onClick = { showOrdemMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Ordenar")
                        }
                        DropdownMenu(
                            expanded = showOrdemMenu,
                            onDismissRequest = { showOrdemMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ordenar por Nome") },
                                onClick = { viewModel.setOrdem(OrdemProduto.NOME); showOrdemMenu = false },
                                leadingIcon = {
                                    if (ordem == OrdemProduto.NOME)
                                        Icon(Icons.Default.Check, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Ordenar por Validade") },
                                onClick = { viewModel.setOrdem(OrdemProduto.VALIDADE); showOrdemMenu = false },
                                leadingIcon = {
                                    if (ordem == OrdemProduto.VALIDADE)
                                        Icon(Icons.Default.Check, contentDescription = null)
                                }
                            )
                        }
                    }
                    // Botão de filtro
                    Box {
                        IconButton(onClick = { showFiltroMenu = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                        }
                        DropdownMenu(
                            expanded = showFiltroMenu,
                            onDismissRequest = { showFiltroMenu = false }
                        ) {
                            FiltroProduto.entries.forEach { f ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            when (f) {
                                                FiltroProduto.TODOS -> "Todos"
                                                FiltroProduto.PROXIMOS_VENCIMENTO -> "Próximos do Vencimento"
                                                FiltroProduto.VENCIDOS -> "Vencidos"
                                                FiltroProduto.ESTOQUE_BAIXO -> "Estoque Baixo"
                                            }
                                        )
                                    },
                                    onClick = { viewModel.setFiltro(f); showFiltroMenu = false },
                                    leadingIcon = {
                                        if (filtro == f) Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                )
                            }
                            if (categorias.isNotEmpty()) {
                                HorizontalDivider()
                                Text(
                                    "Categorias",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                                DropdownMenuItem(
                                    text = { Text("Todas as categorias") },
                                    onClick = { viewModel.setCategoriaFiltro(null); showFiltroMenu = false },
                                    leadingIcon = {
                                        if (categoriaFiltro == null) Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                )
                                categorias.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = { viewModel.setCategoriaFiltro(cat); showFiltroMenu = false },
                                        leadingIcon = {
                                            if (categoriaFiltro == cat) Icon(Icons.Default.Check, contentDescription = null)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    // Menu de mais opções
                    var showMoreMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Movimentações") },
                                onClick = { onMovimentacoes(); showMoreMenu = false },
                                leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Relatórios") },
                                onClick = { onRelatorios(); showMoreMenu = false },
                                leadingIcon = { Icon(Icons.Default.BarChart, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Configurações") },
                                onClick = { onConfiguracoes(); showMoreMenu = false },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNovoProduto) {
                Icon(Icons.Default.Add, contentDescription = "Novo Produto")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                    label = { Text("Produtos") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNovoProduto,
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Cadastrar novo produto") },
                    label = { Text("Cadastro") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onMovimentacoes,
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                    label = { Text("Movimentações") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onRelatorios,
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("Relatórios") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onConfiguracoes,
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Config") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de busca
            OutlinedTextField(
                value = busca,
                onValueChange = viewModel::setBusca,
                placeholder = { Text("Buscar produto...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    AnimatedVisibility(busca.isNotBlank()) {
                        IconButton(onClick = { viewModel.setBusca("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            // Contagem de resultados
            Text(
                text = "${produtos.size} de $totalProdutos produto(s)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (produtos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Nenhum produto encontrado",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Toque no + para adicionar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(produtos, key = { it.id }) { produto ->
                        ProdutoCard(
                            produto = produto,
                            diasAntesVencimento = diasAntesVencimento,
                            quantidadeMinimaGlobal = quantidadeMinimaGlobal,
                            onEditar = { onEditarProduto(produto.id) },
                            onDeletar = { showDeleteDialog = produto.id }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmação de exclusão
    showDeleteDialog?.let { id ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Confirmar exclusão") },
            text = { Text("Deseja remover este produto? As movimentações associadas também serão removidas.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletarProduto(id)
                    showDeleteDialog = null
                }) { Text("Remover", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }
}
