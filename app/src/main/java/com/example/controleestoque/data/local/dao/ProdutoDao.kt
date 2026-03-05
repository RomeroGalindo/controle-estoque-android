package com.example.controleestoque.data.local.dao

import androidx.room.*
import com.example.controleestoque.data.local.entity.Produto
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para operações de banco de dados da entidade Produto.
 */
@Dao
interface ProdutoDao {

    /** Insere um novo produto e retorna o ID gerado */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(produto: Produto): Long

    /** Atualiza um produto existente */
    @Update
    suspend fun atualizar(produto: Produto)

    /** Remove um produto pelo objeto */
    @Delete
    suspend fun deletar(produto: Produto)

    /** Remove um produto pelo ID */
    @Query("DELETE FROM produtos WHERE id = :id")
    suspend fun deletarPorId(id: Long)

    /** Retorna todos os produtos ordenados por nome */
    @Query("SELECT * FROM produtos ORDER BY nome ASC")
    fun listarTodos(): Flow<List<Produto>>

    /** Retorna todos os produtos ordenados por data de validade */
    @Query("SELECT * FROM produtos ORDER BY dataValidade ASC")
    fun listarPorValidade(): Flow<List<Produto>>

    /** Busca produtos pelo nome (busca parcial, ignorando maiúsculas/minúsculas) */
    @Query("SELECT * FROM produtos WHERE nome LIKE '%' || :busca || '%' ORDER BY nome ASC")
    fun buscarPorNome(busca: String): Flow<List<Produto>>

    /** Filtra produtos por categoria */
    @Query("SELECT * FROM produtos WHERE categoria = :categoria ORDER BY nome ASC")
    fun filtrarPorCategoria(categoria: String): Flow<List<Produto>>

    /** Retorna produto por ID */
    @Query("SELECT * FROM produtos WHERE id = :id")
    fun buscarPorId(id: Long): Flow<Produto?>

    /**
     * Retorna produtos que estão próximos do vencimento ou já vencidos.
     * [limiteMs] é o timestamp limite: produtos com dataValidade <= limiteMs são retornados.
     */
    @Query("SELECT * FROM produtos WHERE dataValidade <= :limiteMs ORDER BY dataValidade ASC")
    suspend fun buscarProximosDoVencimento(limiteMs: Long): List<Produto>

    /**
     * Retorna produtos já vencidos (dataValidade antes de agora).
     */
    @Query("SELECT * FROM produtos WHERE dataValidade < :agoraMs ORDER BY dataValidade ASC")
    suspend fun buscarVencidos(agoraMs: Long): List<Produto>

    /**
     * Retorna produtos próximos do vencimento como Flow (para UI reativa).
     */
    @Query("SELECT * FROM produtos WHERE dataValidade <= :limiteMs ORDER BY dataValidade ASC")
    fun observarProximosDoVencimento(limiteMs: Long): Flow<List<Produto>>

    /**
     * Retorna produtos vencidos como Flow.
     */
    @Query("SELECT * FROM produtos WHERE dataValidade < :agoraMs ORDER BY dataValidade ASC")
    fun observarVencidos(agoraMs: Long): Flow<List<Produto>>

    /** Atualiza apenas a quantidade atual de um produto */
    @Query("UPDATE produtos SET quantidadeAtual = :novaQuantidade WHERE id = :id")
    suspend fun atualizarQuantidade(id: Long, novaQuantidade: Int)

    /** Atualiza unidade, data de validade e localização de um produto (dados vindos de movimentação) */
    @Query("UPDATE produtos SET unidade = :unidade, dataValidade = :dataValidade, localizacao = :localizacao WHERE id = :id")
    suspend fun atualizarDetalhesMovimentacao(id: Long, unidade: String, dataValidade: Long, localizacao: String)

    /** Retorna todas as categorias distintas */
    @Query("SELECT DISTINCT categoria FROM produtos WHERE categoria != '' ORDER BY categoria ASC")
    fun listarCategorias(): Flow<List<String>>

    /** Retorna contagem total de produtos */
    @Query("SELECT COUNT(*) FROM produtos")
    fun contarTodos(): Flow<Int>
}
