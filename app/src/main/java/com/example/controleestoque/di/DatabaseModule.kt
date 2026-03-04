package com.example.controleestoque.di

import android.content.Context
import androidx.room.Room
import com.example.controleestoque.data.local.AppDatabase
import com.example.controleestoque.data.local.dao.MovimentacaoDao
import com.example.controleestoque.data.local.dao.ProdutoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para injeção de dependência da camada de dados.
 * Fornece instâncias do banco de dados e dos DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideProdutoDao(db: AppDatabase): ProdutoDao = db.produtoDao()

    @Provides
    fun provideMovimentacaoDao(db: AppDatabase): MovimentacaoDao = db.movimentacaoDao()
}
