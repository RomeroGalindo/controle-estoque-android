package com.example.controleestoque.data.repository

import com.example.controleestoque.data.local.dao.MovimentacaoDao
import com.example.controleestoque.data.local.dao.ProdutoDao
import com.example.controleestoque.data.local.entity.Movimentacao
import com.example.controleestoque.data.local.entity.TipoMovimentacao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositório para acesso e manipulação de dados de Movimentacao.
 * Gerencia a lógica de entrada e saída, atualizando a quantidade do produto.
 */
@Singleton
class MovimentacaoRepository @Inject constructor(
    private val movimentacaoDao: MovimentacaoDao,
    private val produtoDao: ProdutoDao
) {

    fun listarTodas(): Flow<List<Movimentacao>> = movimentacaoDao.listarTodas()

    fun listarPorProduto(produtoId: Long): Flow<List<Movimentacao>> =
        movimentacaoDao.listarPorProduto(produtoId)

    fun filtrarPorPeriodo(inicio: Long, fim: Long): Flow<List<Movimentacao>> =
        movimentacaoDao.filtrarPorPeriodo(inicio, fim)

    fun filtrarPorProdutoEPeriodo(produtoId: Long, inicio: Long, fim: Long): Flow<List<Movimentacao>> =
        movimentacaoDao.filtrarPorProdutoEPeriodo(produtoId, inicio, fim)

    /**
     * Registra uma movimentação (entrada ou saída) e atualiza a quantidade do produto.
     *
     * Para SAIDA, valida que o estoque não ficará negativo.
     *
     * @return Result.success com o ID da movimentação inserida,
     *         ou Result.failure com mensagem de erro.
     */
    suspend fun registrarMovimentacao(movimentacao: Movimentacao): Result<Long> {
        val produto = produtoDao.buscarPorId(movimentacao.produtoId).firstOrNull()
            ?: return Result.failure(Exception("Produto não encontrado"))

        val novaQuantidade = when (movimentacao.tipo) {
            TipoMovimentacao.ENTRADA -> produto.quantidadeAtual + movimentacao.quantidade
            TipoMovimentacao.SAIDA -> {
                if (produto.quantidadeAtual < movimentacao.quantidade) {
                    return Result.failure(
                        Exception("Estoque insuficiente: disponível ${produto.quantidadeAtual}")
                    )
                }
                produto.quantidadeAtual - movimentacao.quantidade
            }
        }

        val id = movimentacaoDao.inserir(movimentacao)
        produtoDao.atualizarQuantidade(produto.id, novaQuantidade)
        return Result.success(id)
    }

    suspend fun deletar(movimentacao: Movimentacao) = movimentacaoDao.deletar(movimentacao)
}
