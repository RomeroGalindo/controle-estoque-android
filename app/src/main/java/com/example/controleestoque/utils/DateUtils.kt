package com.example.controleestoque.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Utilitários para manipulação e formatação de datas.
 */
object DateUtils {

    private val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    /** Formata um timestamp (milissegundos) para string no formato "dd/MM/yyyy" */
    fun formatarData(timestamp: Long): String {
        return DATE_FORMAT.format(Date(timestamp))
    }

    /** Converte uma string no formato "dd/MM/yyyy" para timestamp em milissegundos */
    fun parsearData(dataStr: String): Long? {
        return try {
            DATE_FORMAT.parse(dataStr)?.time
        } catch (e: Exception) {
            null
        }
    }

    /** Retorna o timestamp do início do dia de hoje (meia-noite) */
    fun inicioDeHoje(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** Retorna o timestamp correspondente a N dias a partir de agora */
    fun agoraMailsDias(dias: Int): Long {
        return System.currentTimeMillis() + TimeUnit.DAYS.toMillis(dias.toLong())
    }

    /**
     * Calcula a diferença em dias entre a data de validade e hoje.
     * Valores negativos indicam produto vencido.
     * @param dataValidade timestamp da data de validade
     */
    fun diasParaVencer(dataValidade: Long): Long {
        val agora = inicioDeHoje()
        val diff = dataValidade - agora
        return TimeUnit.MILLISECONDS.toDays(diff)
    }

    /**
     * Verifica se um produto está vencido.
     * @param dataValidade timestamp da data de validade
     */
    fun estaVencido(dataValidade: Long): Boolean {
        return dataValidade < inicioDeHoje()
    }

    /**
     * Verifica se um produto está próximo do vencimento.
     * @param dataValidade timestamp da data de validade
     * @param diasLimite número de dias de antecedência para considerar "próximo"
     */
    fun estaProximoDoVencimento(dataValidade: Long, diasLimite: Int): Boolean {
        val dias = diasParaVencer(dataValidade)
        return dias in 0..diasLimite.toLong()
    }
}
