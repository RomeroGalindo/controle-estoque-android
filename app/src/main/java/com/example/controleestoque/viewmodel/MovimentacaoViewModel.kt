package com.example.controleestoque.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controleestoque.data.local.entity.Movimentacao
import com.example.controleestoque.data.local.entity.Produto
import com.example.controleestoque.data.local.entity.TipoMovimentacao
import com.example.controleestoque.data.repository.MovimentacaoRepository
import com.example.controleestoque.data.repository.ProdutoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para a tela de movimentações (entrada/saída de estoque).
 * Gerencia o estado da UI de movimentações e o histórico.
 */
@HiltViewModel
class MovimentacaoViewModel @Inject constructor(
    private val movimentacaoRepository: MovimentacaoRepository,
    private val produtoRepository: ProdutoRepository
) : ViewModel() {

    // Produto selecionado para movimentação
    private val _produtoSelecionadoId = MutableStateFlow<Long?>(null)
    val produtoSelecionadoId: StateFlow<Long?> = _produtoSelecionadoId.asStateFlow()

    // Tipo de movimentação selecionado
    private val _tipoMovimentacao = MutableStateFlow(TipoMovimentacao.ENTRADA)
    val tipoMovimentacao: StateFlow<TipoMovimentacao> = _tipoMovimentacao.asStateFlow()

    // Quantidade da movimentação
    private val _quantidade = MutableStateFlow("")
    val quantidade: StateFlow<String> = _quantidade.asStateFlow()

    // Observações da movimentação
    private val _observacoes = MutableStateFlow("")
    val observacoes: StateFlow<String> = _observacoes.asStateFlow()

    // Mensagem de feedback
    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem: StateFlow<String?> = _mensagem.asStateFlow()

    // Carregando?
    private val _carregando = MutableStateFlow(false)
    val carregando: StateFlow<Boolean> = _carregando.asStateFlow()

    /** Lista de todas as movimentações (histórico completo) */
    val historico: StateFlow<List<Movimentacao>> = movimentacaoRepository.listarTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Todos os produtos disponíveis (para seleção) */
    val produtos: StateFlow<List<Produto>> = produtoRepository.listarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setProdutoSelecionado(id: Long?) { _produtoSelecionadoId.value = id }
    fun setTipoMovimentacao(tipo: TipoMovimentacao) { _tipoMovimentacao.value = tipo }
    fun setQuantidade(qty: String) { _quantidade.value = qty }
    fun setObservacoes(obs: String) { _observacoes.value = obs }
    fun limparMensagem() { _mensagem.value = null }

    /** Retorna movimentações filtradas por produto */
    fun listarPorProduto(produtoId: Long): Flow<List<Movimentacao>> =
        movimentacaoRepository.listarPorProduto(produtoId)

    /**
     * Registra a movimentação com os dados do formulário atual.
     * Valida campos obrigatórios antes de prosseguir.
     */
    fun registrarMovimentacao() {
        val produtoId = _produtoSelecionadoId.value
            ?: run { _mensagem.value = "Selecione um produto"; return }

        val qtd = _quantidade.value.toIntOrNull()
            ?: run { _mensagem.value = "Quantidade inválida"; return }

        if (qtd <= 0) {
            _mensagem.value = "Quantidade deve ser maior que zero"
            return
        }

        viewModelScope.launch {
            _carregando.value = true
            val movimentacao = Movimentacao(
                produtoId = produtoId,
                tipo = _tipoMovimentacao.value,
                quantidade = qtd,
                dataHora = System.currentTimeMillis(),
                observacoes = _observacoes.value
            )
            val resultado = movimentacaoRepository.registrarMovimentacao(movimentacao)
            resultado.fold(
                onSuccess = {
                    _mensagem.value = "Movimentação registrada com sucesso"
                    // Limpa o formulário após sucesso
                    _quantidade.value = ""
                    _observacoes.value = ""
                },
                onFailure = { e ->
                    _mensagem.value = e.message ?: "Erro ao registrar movimentação"
                }
            )
            _carregando.value = false
        }
    }
}
