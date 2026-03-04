package com.example.controleestoque.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Extensão para criar DataStore no Context */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "configuracoes")

/**
 * Gerenciador de configurações do aplicativo usando DataStore Preferences.
 * Armazena preferências como dias antes do vencimento e quantidade mínima de estoque.
 */
@Singleton
class ConfiguracoesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        val DIAS_ANTES_VENCIMENTO = intPreferencesKey("dias_antes_vencimento")
        val QUANTIDADE_MINIMA_GLOBAL = intPreferencesKey("quantidade_minima_global")

        const val DEFAULT_DIAS_ANTES_VENCIMENTO = 7
        const val DEFAULT_QUANTIDADE_MINIMA_GLOBAL = 5
    }

    /** Flow com o número de dias antes do vencimento para alertar */
    val diasAntesVencimento: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DIAS_ANTES_VENCIMENTO] ?: DEFAULT_DIAS_ANTES_VENCIMENTO
    }

    /** Flow com a quantidade mínima global para alerta de estoque baixo */
    val quantidadeMinimaGlobal: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[QUANTIDADE_MINIMA_GLOBAL] ?: DEFAULT_QUANTIDADE_MINIMA_GLOBAL
    }

    /** Atualiza o número de dias antes do vencimento */
    suspend fun setDiasAntesVencimento(dias: Int) {
        context.dataStore.edit { prefs ->
            prefs[DIAS_ANTES_VENCIMENTO] = dias
        }
    }

    /** Atualiza a quantidade mínima global */
    suspend fun setQuantidadeMinimaGlobal(quantidade: Int) {
        context.dataStore.edit { prefs ->
            prefs[QUANTIDADE_MINIMA_GLOBAL] = quantidade
        }
    }
}
