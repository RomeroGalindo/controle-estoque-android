package com.example.controleestoque.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controleestoque.utils.ConfiguracoesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para a tela de Configurações.
 * Gerencia a leitura e escrita das preferências do usuário.
 */
@HiltViewModel
class ConfiguracoesViewModel @Inject constructor(
    private val configuracoesManager: ConfiguracoesManager
) : ViewModel() {

    /** Número de dias antes do vencimento para alertar (padrão: 7) */
    val diasAntesVencimento: StateFlow<Int> = configuracoesManager.diasAntesVencimento
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ConfiguracoesManager.DEFAULT_DIAS_ANTES_VENCIMENTO
        )

    /** Quantidade mínima global para alerta de estoque baixo (padrão: 5) */
    val quantidadeMinimaGlobal: StateFlow<Int> = configuracoesManager.quantidadeMinimaGlobal
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ConfiguracoesManager.DEFAULT_QUANTIDADE_MINIMA_GLOBAL
        )

    /** Salva a configuração de dias antes do vencimento */
    fun setDiasAntesVencimento(dias: Int) {
        viewModelScope.launch {
            configuracoesManager.setDiasAntesVencimento(dias)
        }
    }

    /** Salva a configuração de quantidade mínima global */
    fun setQuantidadeMinimaGlobal(quantidade: Int) {
        viewModelScope.launch {
            configuracoesManager.setQuantidadeMinimaGlobal(quantidade)
        }
    }
}
