package com.example.controleestoque.data.local.dao

import androidx.room.*
import com.example.controleestoque.data.local.entity.Movimentacao
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para operações de banco de dados da entidade Movimentacao.
 */
@Dao
interface MovimentacaoDao {

    /** Insere uma nova movimentação e retorna o ID gerado */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(movimentacao: Movimentacao): Long

    /** Remove uma movimentação pelo objeto */
    @Delete
    suspend fun deletar(movimentacao: Movimentacao)

    /** Retorna todas as movimentações ordenadas por data (mais recente primeiro) */
    @Query("SELECT * FROM movimentacoes ORDER BY dataHora DESC")
    fun listarTodas(): Flow<List<Movimentacao>>

    /** Retorna movimentações de um produto específico */
    @Query("SELECT * FROM movimentacoes WHERE produtoId = :produtoId ORDER BY dataHora DESC")
    fun listarPorProduto(produtoId: Long): Flow<List<Movimentacao>>

    /**
     * Retorna movimentações filtradas por período.
     * [inicio] e [fim] são timestamps em milissegundos.
     */
    @Query("""
        SELECT * FROM movimentacoes 
        WHERE dataHora BETWEEN :inicio AND :fim 
        ORDER BY dataHora DESC
    """)
    fun filtrarPorPeriodo(inicio: Long, fim: Long): Flow<List<Movimentacao>>

    /**
     * Retorna movimentações de um produto em um período específico.
     */
    @Query("""
        SELECT * FROM movimentacoes 
        WHERE produtoId = :produtoId AND dataHora BETWEEN :inicio AND :fim 
        ORDER BY dataHora DESC
    """)
    fun filtrarPorProdutoEPeriodo(produtoId: Long, inicio: Long, fim: Long): Flow<List<Movimentacao>>

    /** Remove todas as movimentações de um produto */
    @Query("DELETE FROM movimentacoes WHERE produtoId = :produtoId")
    suspend fun deletarPorProduto(produtoId: Long)
}
