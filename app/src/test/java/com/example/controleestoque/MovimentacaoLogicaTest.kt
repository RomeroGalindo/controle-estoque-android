package com.example.controleestoque

import com.example.controleestoque.data.local.entity.TipoMovimentacao
import org.junit.Assert.*
import org.junit.Test

/**
 * Testes unitários para a lógica de movimentações de estoque.
 * Testa os cálculos de entrada e saída sem dependência de banco de dados.
 */
class MovimentacaoLogicaTest {

    /**
     * Simula a lógica de cálculo de nova quantidade após uma movimentação.
     * Reflete a lógica implementada em MovimentacaoRepository.registrarMovimentacao().
     */
    private fun calcularNovaQuantidade(
        quantidadeAtual: Int,
        tipo: TipoMovimentacao,
        quantidade: Int
    ): Result<Int> {
        return when (tipo) {
            TipoMovimentacao.ENTRADA -> Result.success(quantidadeAtual + quantidade)
            TipoMovimentacao.SAIDA -> {
                if (quantidadeAtual < quantidade) {
                    Result.failure(Exception("Estoque insuficiente: disponível $quantidadeAtual"))
                } else {
                    Result.success(quantidadeAtual - quantidade)
                }
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // ENTRADA
    // ──────────────────────────────────────────────────────────────

    @Test
    fun `entrada deve aumentar a quantidade corretamente`() {
        val resultado = calcularNovaQuantidade(10, TipoMovimentacao.ENTRADA, 5)
        assertTrue(resultado.isSuccess)
        assertEquals(15, resultado.getOrNull())
    }

    @Test
    fun `entrada com estoque zero deve resultar na quantidade entrada`() {
        val resultado = calcularNovaQuantidade(0, TipoMovimentacao.ENTRADA, 20)
        assertTrue(resultado.isSuccess)
        assertEquals(20, resultado.getOrNull())
    }

    @Test
    fun `entrada de grande quantidade deve funcionar`() {
        val resultado = calcularNovaQuantidade(100, TipoMovimentacao.ENTRADA, 1000)
        assertTrue(resultado.isSuccess)
        assertEquals(1100, resultado.getOrNull())
    }

    // ──────────────────────────────────────────────────────────────
    // SAÍDA
    // ──────────────────────────────────────────────────────────────

    @Test
    fun `saida deve diminuir a quantidade corretamente`() {
        val resultado = calcularNovaQuantidade(10, TipoMovimentacao.SAIDA, 3)
        assertTrue(resultado.isSuccess)
        assertEquals(7, resultado.getOrNull())
    }

    @Test
    fun `saida com quantidade igual ao estoque deve zerar o estoque`() {
        val resultado = calcularNovaQuantidade(10, TipoMovimentacao.SAIDA, 10)
        assertTrue(resultado.isSuccess)
        assertEquals(0, resultado.getOrNull())
    }

    @Test
    fun `saida com quantidade maior que estoque deve falhar`() {
        val resultado = calcularNovaQuantidade(5, TipoMovimentacao.SAIDA, 10)
        assertTrue(resultado.isFailure)
        assertNotNull(resultado.exceptionOrNull())
        assertTrue(resultado.exceptionOrNull()!!.message!!.contains("insuficiente"))
    }

    @Test
    fun `saida com estoque zero deve falhar`() {
        val resultado = calcularNovaQuantidade(0, TipoMovimentacao.SAIDA, 1)
        assertTrue(resultado.isFailure)
    }

    @Test
    fun `saida nao deve resultar em quantidade negativa`() {
        val resultado = calcularNovaQuantidade(3, TipoMovimentacao.SAIDA, 100)
        assertTrue(resultado.isFailure)
        // Nunca retorna quantidade negativa
        assertNull(resultado.getOrNull())
    }
}
