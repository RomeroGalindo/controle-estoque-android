package com.example.controleestoque.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** Fornece acesso ao DAO de Produto */
    abstract fun produtoDao(): ProdutoDao

    /** Fornece acesso ao DAO de Movimentação */
    abstract fun movimentacaoDao(): MovimentacaoDao

    companion object {
        const val DATABASE_NAME = "controle_estoque.db"

        /** Migração da versão 1 para 2: adiciona campos unidade, dataValidade, localizacao na tabela movimentacoes */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE movimentacoes ADD COLUMN unidade TEXT NOT NULL DEFAULT 'unidade'")
                db.execSQL("ALTER TABLE movimentacoes ADD COLUMN dataValidade INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE movimentacoes ADD COLUMN localizacao TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
