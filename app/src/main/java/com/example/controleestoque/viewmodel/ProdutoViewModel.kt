package com.example.controleestoque.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controleestoque.data.local.entity.Produto
import com.example.controleestoque.data.repository.ProdutoRepository
import com.example.controleestoque.utils.ConfiguracoesManager
import com.example.controleestoque.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Enum para ordenação da lista de produtos */
enum class OrdemProduto { NOME, VALIDADE }

/** Filtro para exibição de produtos */
enum class FiltroProduto { TODOS, PROXIMOS_VENCIMENTO, VENCIDOS, ESTOQUE_BAIXO }

/**
 * ViewModel para a tela de listagem e detalhes de produtos.
 * Gerencia o estado da UI e as operações de CRUD de produtos.
 */
@HiltViewModel
class ProdutoViewModel @Inject constructor(
    private val produtoRepository: ProdutoRepository,
    private val configuracoesManager: ConfiguracoesManager
) : ViewModel() {

    // Estado da busca por nome
    private val _busca = MutableStateFlow("")
    val busca: StateFlow<String> = _busca.asStateFlow()

    // Ordem de exibição
    private val _ordem = MutableStateFlow(OrdemProduto.NOME)
    val ordem: StateFlow<OrdemProduto> = _ordem.asStateFlow()

    // Filtro ativo
    private val _filtro = MutableStateFlow(FiltroProduto.TODOS)
    val filtro: StateFlow<FiltroProduto> = _filtro.asStateFlow()

    // Categoria selecionada para filtro
    private val _categoriaFiltro = MutableStateFlow<String?>(null)
    val categoriaFiltro: StateFlow<String?> = _categoriaFiltro.asStateFlow()

    // Mensagem de feedback para a UI
    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem: StateFlow<String?> = _mensagem.asStateFlow()

    // Todos os produtos (dados brutos do banco)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val todosProdutos: StateFlow<List<Produto>> = _ordem.flatMapLatest { ordem ->
        when (ordem) {
            OrdemProduto.NOME -> produtoRepository.listarTodos()
            OrdemProduto.VALIDADE -> produtoRepository.listarPorValidade()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Configurações para uso nos filtros
    val diasAntesVencimento: StateFlow<Int> = configuracoesManager.diasAntesVencimento
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConfiguracoesManager.DEFAULT_DIAS_ANTES_VENCIMENTO)

    val quantidadeMinimaGlobal: StateFlow<Int> = configuracoesManager.quantidadeMinimaGlobal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConfiguracoesManager.DEFAULT_QUANTIDADE_MINIMA_GLOBAL)

    /** Lista de produtos filtrada e ordenada conforme configurações da UI */
    @OptIn(ExperimentalCoroutinesApi::class)
    val produtos: StateFlow<List<Produto>> = combine(
        // Agrupa primeiros 3 estados
        combine(todosProdutos, _busca, _filtro) { lista, busca, filtro ->
            Triple(lista, busca, filtro)
        },
        // Agrupa os outros 3 estados
        combine(_categoriaFiltro, diasAntesVencimento, quantidadeMinimaGlobal) { cat, dias, qtdMin ->
            Triple(cat, dias, qtdMin)
        }
    ) { (lista, busca, filtro), (categoria, dias, qtdMin) ->
        lista
            .filter { p -> busca.isBlank() || p.nome.contains(busca, ignoreCase = true) }
            .filter { p -> categoria == null || p.categoria == categoria }
            .filter { p ->
                when (filtro) {
                    FiltroProduto.TODOS -> true
                    FiltroProduto.PROXIMOS_VENCIMENTO ->
                        DateUtils.estaProximoDoVencimento(p.dataValidade, dias)
                    FiltroProduto.VENCIDOS -> DateUtils.estaVencido(p.dataValidade)
                    FiltroProduto.ESTOQUE_BAIXO -> {
                        val min = if (p.quantidadeMinima > 0) p.quantidadeMinima else qtdMin
                        p.quantidadeAtual <= min
                    }
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Categorias disponíveis para filtro */
    val categorias: StateFlow<List<String>> = produtoRepository.listarCategorias()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Contagem total de produtos */
    val totalProdutos: StateFlow<Int> = produtoRepository.contarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setBusca(busca: String) { _busca.value = busca }
    fun setOrdem(ordem: OrdemProduto) { _ordem.value = ordem }
    fun setFiltro(filtro: FiltroProduto) { _filtro.value = filtro }
    fun setCategoriaFiltro(categoria: String?) { _categoriaFiltro.value = categoria }

    /** Limpa a mensagem de feedback após exibição */
    fun limparMensagem() { _mensagem.value = null }

    /** Salva um produto (insert ou update) com validação dos campos obrigatórios */
    fun salvarProduto(produto: Produto) {
        if (produto.nome.isBlank()) {
            _mensagem.value = "Nome do produto é obrigatório"
            return
        }
        if (produto.dataValidade == 0L) {
            _mensagem.value = "Data de validade é obrigatória"
            return
        }
        viewModelScope.launch {
            try {
                if (produto.id == 0L) {
                    produtoRepository.inserir(produto)
                    _mensagem.value = "Produto cadastrado com sucesso"
                } else {
                    produtoRepository.atualizar(produto)
                    _mensagem.value = "Produto atualizado com sucesso"
                }
            } catch (e: Exception) {
                _mensagem.value = "Erro ao salvar produto: ${e.message}"
            }
        }
    }

    /** Remove um produto pelo ID */
    fun deletarProduto(id: Long) {
        viewModelScope.launch {
            try {
                produtoRepository.deletarPorId(id)
                _mensagem.value = "Produto removido"
            } catch (e: Exception) {
                _mensagem.value = "Erro ao remover produto: ${e.message}"
            }
        }
    }

    /** Retorna um Flow com o produto pelo ID */
    fun buscarPorId(id: Long) = produtoRepository.buscarPorId(id)

    /** Retorna Flow de produtos vencidos (para a tela de relatórios) */
    fun observarVencidos(agoraMs: Long) = produtoRepository.observarVencidos(agoraMs)

    /**
     * Retorna Flow de produtos próximos do vencimento, excluindo os já vencidos.
     * [limiteMs] é o timestamp máximo de validade, [agoraMs] é o timestamp atual.
     */
    fun observarProximosVencimento(limiteMs: Long, agoraMs: Long) =
        produtoRepository.observarProximosDoVencimento(limiteMs)
            .map { lista -> lista.filter { it.dataValidade >= agoraMs } }
}
