package com.example.controleestoque.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade que representa um produto no banco de dados.
 * Contém todas as informações sobre o produto incluindo estoque e validade.
 */
@Entity(tableName = "produtos")
data class Produto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Nome do produto (obrigatório) */
    val nome: String,

    /** Código de barras do produto (opcional) */
    val codigoBarras: String? = null,

    /** Categoria do produto (ex.: alimentos, bebidas, limpeza) */
    val categoria: String = "",

    /** Quantidade atual em estoque */
    val quantidadeAtual: Int = 0,

    /** Unidade de medida (ex.: unidade, caixa, kg, litro) */
    val unidade: String = "unidade",

    /** Data de validade em milissegundos desde epoch (0 = não informada, atualizada por movimentações) */
    val dataValidade: Long = 0L,

    /** Localização física do produto (ex.: prateleira 1, depósito, geladeira) */
    val localizacao: String = "",

    /** Observações adicionais */
    val observacoes: String = "",

    /** Quantidade mínima para alertar estoque baixo (0 = usa configuração global) */
    val quantidadeMinima: Int = 0
)
