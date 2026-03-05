package com.example.controleestoque.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Enum que representa o tipo de movimentação de estoque.
 * ENTRADA: aumenta a quantidade em estoque.
 * SAIDA: diminui a quantidade em estoque.
 */
enum class TipoMovimentacao {
    ENTRADA,
    SAIDA
}

/**
 * Entidade que representa uma movimentação de estoque (entrada ou saída).
 * Possui chave estrangeira para Produto com deleção em cascata.
 */
@Entity(
    tableName = "movimentacoes",
    foreignKeys = [
        ForeignKey(
            entity = Produto::class,
            parentColumns = ["id"],
            childColumns = ["produtoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("produtoId")]
)
data class Movimentacao(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** ID do produto associado */
    val produtoId: Long,

    /** Tipo da movimentação: ENTRADA ou SAIDA */
    val tipo: TipoMovimentacao,

    /** Quantidade movimentada */
    val quantidade: Int,

    /** Unidade de medida (ex.: unidade, caixa, kg, litro) */
    val unidade: String = "unidade",

    /** Data de validade em milissegundos desde epoch (0 = não informada) */
    val dataValidade: Long = 0L,

    /** Localização física do produto (ex.: prateleira 1, depósito, geladeira) */
    val localizacao: String = "",

    /** Data e hora da movimentação em milissegundos desde epoch */
    val dataHora: Long = System.currentTimeMillis(),

    /** Observações sobre a movimentação */
    val observacoes: String = ""
)
