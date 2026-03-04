package com.example.controleestoque.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.controleestoque.data.local.dao.MovimentacaoDao
import com.example.controleestoque.data.local.dao.ProdutoDao
import com.example.controleestoque.data.local.entity.Movimentacao
import com.example.controleestoque.data.local.entity.Produto

/**
 * Banco de dados principal do aplicativo usando Room.
 * Contém as entidades Produto e Movimentacao.
 * A instância singleton é gerenciada pelo Hilt via DatabaseModule.
 */
@Database(
    entities = [Produto::class, Movimentacao::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** Fornece acesso ao DAO de Produto */
    abstract fun produtoDao(): ProdutoDao

    /** Fornece acesso ao DAO de Movimentação */
    abstract fun movimentacaoDao(): MovimentacaoDao

    companion object {
        const val DATABASE_NAME = "controle_estoque.db"
    }
}
