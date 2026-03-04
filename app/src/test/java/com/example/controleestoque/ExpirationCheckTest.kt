package com.example.controleestoque

import com.example.controleestoque.utils.DateUtils
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Testes unitários para a lógica de verificação de validade de produtos.
 * Cobre os cenários de produtos vencidos, próximos do vencimento e normais.
 */
class ExpirationCheckTest {

    /** Cria um timestamp a N dias a partir do início de hoje */
    private fun timestampDaqui(dias: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.DAY_OF_YEAR, dias)
        return cal.timeInMillis
    }

    // ──────────────────────────────────────────────────────────────
    // DateUtils.estaVencido
    // ──────────────────────────────────────────────────────────────

    @Test
    fun `produto vencido a 1 dia deve ser detectado como vencido`() {
        val dataValidade = timestampDaqui(-1)
        assertTrue(DateUtils.estaVencido(dataValidade))
    }

    @Test
    fun `produto vencido a 30 dias deve ser detectado como vencido`() {
        val dataValidade = timestampDaqui(-30)
        assertTrue(DateUtils.estaVencido(dataValidade))
    }

    @Test
    fun `produto com validade hoje nao deve ser detectado como vencido`() {
        val dataValidade = timestampDaqui(0)
        assertFalse(DateUtils.estaVencido(dataValidade))
    }

    @Test
    fun `produto com validade amanha nao deve ser detectado como vencido`() {
        val dataValidade = timestampDaqui(1)
        assertFalse(DateUtils.estaVencido(dataValidade))
    }

    // ──────────────────────────────────────────────────────────────
    // DateUtils.estaProximoDoVencimento
    // ──────────────────────────────────────────────────────────────

    @Test
    fun `produto vencendo hoje deve estar proximo do vencimento com limite 7 dias`() {
        val dataValidade = timestampDaqui(0)
        assertTrue(DateUtils.estaProximoDoVencimento(dataValidade, 7))
    }

    @Test
    fun `produto vencendo em 7 dias deve estar proximo do vencimento com limite 7`() {
        val dataValidade = timestampDaqui(7)
        assertTrue(DateUtils.estaProximoDoVencimento(dataValidade, 7))
    }

    @Test
    fun `produto vencendo em 8 dias nao deve estar proximo com limite 7`() {
        val dataValidade = timestampDaqui(8)
        assertFalse(DateUtils.estaProximoDoVencimento(dataValidade, 7))
    }

    @Test
    fun `produto ja vencido nao deve ser considerado proximo do vencimento`() {
        val dataValidade = timestampDaqui(-1)
        assertFalse(DateUtils.estaProximoDoVencimento(dataValidade, 7))
    }

    @Test
    fun `produto com validade longa nao deve estar proximo do vencimento`() {
        val dataValidade = timestampDaqui(365)
        assertFalse(DateUtils.estaProximoDoVencimento(dataValidade, 7))
    }

    // ──────────────────────────────────────────────────────────────
    // DateUtils.diasParaVencer
    // ──────────────────────────────────────────────────────────────

    @Test
    fun `dias para vencer hoje deve ser 0`() {
        val dataValidade = timestampDaqui(0)
        assertEquals(0L, DateUtils.diasParaVencer(dataValidade))
    }

    @Test
    fun `dias para vencer amanha deve ser 1`() {
        val dataValidade = timestampDaqui(1)
        assertEquals(1L, DateUtils.diasParaVencer(dataValidade))
    }

    @Test
    fun `dias para vencer para produto vencido ha 5 dias deve ser negativo`() {
        val dataValidade = timestampDaqui(-5)
        assertTrue(DateUtils.diasParaVencer(dataValidade) < 0)
    }

    // ──────────────────────────────────────────────────────────────
    // DateUtils.formatarData e parsearData
    // ──────────────────────────────────────────────────────────────

    @Test
    fun `formatarData deve retornar formato dd-MM-yyyy`() {
        val cal = Calendar.getInstance()
        cal.set(2025, Calendar.DECEMBER, 25, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val result = DateUtils.formatarData(cal.timeInMillis)
        assertEquals("25/12/2025", result)
    }

    @Test
    fun `parsearData deve converter string valida para timestamp`() {
        val timestamp = DateUtils.parsearData("25/12/2025")
        assertNotNull(timestamp)
        val formatted = DateUtils.formatarData(timestamp!!)
        assertEquals("25/12/2025", formatted)
    }

    @Test
    fun `parsearData deve retornar null para string invalida`() {
        val timestamp = DateUtils.parsearData("data_invalida")
        assertNull(timestamp)
    }

    @Test
    fun `parsearData deve retornar null para string vazia`() {
        val timestamp = DateUtils.parsearData("")
        assertNull(timestamp)
    }
}
