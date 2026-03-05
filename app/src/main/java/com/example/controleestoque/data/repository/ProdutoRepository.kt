package com.example.controleestoque.data.repository

import com.example.controleestoque.data.local.dao.ProdutoDao
import com.example.controleestoque.data.local.entity.Produto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositório para acesso e manipulação de dados de Produto.
 * Abstrai a fonte de dados (Room) dos ViewModels.
 */
@Singleton
class ProdutoRepository @Inject constructor(
    private val produtoDao: ProdutoDao
) {

    fun listarTodos(): Flow<List<Produto>> = produtoDao.listarTodos()

    fun listarPorValidade(): Flow<List<Produto>> = produtoDao.listarPorValidade()

    fun buscarPorNome(busca: String): Flow<List<Produto>> = produtoDao.buscarPorNome(busca)

    fun filtrarPorCategoria(categoria: String): Flow<List<Produto>> =
        produtoDao.filtrarPorCategoria(categoria)

    fun buscarPorId(id: Long): Flow<Produto?> = produtoDao.buscarPorId(id)

    fun listarCategorias(): Flow<List<String>> = produtoDao.listarCategorias()

    fun contarTodos(): Flow<Int> = produtoDao.contarTodos()

    fun observarProximosDoVencimento(limiteMs: Long): Flow<List<Produto>> =
        produtoDao.observarProximosDoVencimento(limiteMs)

    fun observarVencidos(agoraMs: Long): Flow<List<Produto>> =
        produtoDao.observarVencidos(agoraMs)

    suspend fun inserir(produto: Produto): Long = produtoDao.inserir(produto)

    suspend fun atualizar(produto: Produto) = produtoDao.atualizar(produto)

    suspend fun deletar(produto: Produto) = produtoDao.deletar(produto)

    suspend fun deletarPorId(id: Long) = produtoDao.deletarPorId(id)

    suspend fun atualizarQuantidade(id: Long, novaQuantidade: Int) =
        produtoDao.atualizarQuantidade(id, novaQuantidade)

    suspend fun atualizarDetalhesMovimentacao(id: Long, unidade: String, dataValidade: Long, localizacao: String) =
        produtoDao.atualizarDetalhesMovimentacao(id, unidade, dataValidade, localizacao)

    /**
     * Busca produtos próximos do vencimento para uso no worker de notificações.
     * [limiteMs] é o timestamp máximo de validade a considerar.
     */
    suspend fun buscarProximosDoVencimento(limiteMs: Long): List<Produto> =
        produtoDao.buscarProximosDoVencimento(limiteMs)

    /**
     * Busca produtos já vencidos para uso no worker de notificações.
     */
    suspend fun buscarVencidos(agoraMs: Long): List<Produto> =
        produtoDao.buscarVencidos(agoraMs)
}
