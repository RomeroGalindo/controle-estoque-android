package com.example.controleestoque.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.controleestoque.data.repository.ProdutoRepository
import com.example.controleestoque.utils.ConfiguracoesManager
import com.example.controleestoque.utils.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

/**
 * Worker do WorkManager que verifica diariamente os produtos próximos do vencimento ou vencidos.
 * É agendado para executar uma vez por dia, mesmo que o app não esteja aberto.
 *
 * Para cada produto problemático, uma notificação local é enviada ao usuário com:
 * - Nome do produto
 * - Data de validade
 * - Quantidade em estoque
 */
@HiltWorker
class ExpirationCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val produtoRepository: ProdutoRepository,
    private val configuracoesManager: ConfiguracoesManager
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "expiration_check_worker"
        const val CHANNEL_ID = "validade_channel"
        const val CHANNEL_NAME = "Alertas de Validade"
        const val CHANNEL_DESC = "Notificações sobre produtos próximos do vencimento ou vencidos"
    }

    override suspend fun doWork(): Result {
        // Cria o canal de notificações (necessário no Android 8+)
        createNotificationChannel()

        // Lê a configuração de dias antes do vencimento
        val diasAntesVencimento = configuracoesManager.diasAntesVencimento.firstOrNull()
            ?: ConfiguracoesManager.DEFAULT_DIAS_ANTES_VENCIMENTO

        val agora = System.currentTimeMillis()

        // Timestamp limite: produtos com validade até (agora + diasAntesVencimento dias)
        val limiteMs = DateUtils.agoraMaisDias(diasAntesVencimento)

        // Busca produtos próximos do vencimento (incluindo os já vencidos)
        val proximosVencimento = produtoRepository.buscarProximosDoVencimento(limiteMs)

        // Busca produtos já vencidos (dataValidade < agora)
        val vencidos = produtoRepository.buscarVencidos(agora)
        val idVencidos = vencidos.map { it.id }.toSet()

        var notificationId = 1000

        // Envia notificação para produtos já vencidos
        for (produto in vencidos) {
            if (produto.dataValidade == 0L) continue // Pula produtos sem data de validade
            val dataFormatada = DateUtils.formatarData(produto.dataValidade)
            sendNotification(
                id = notificationId++,
                title = "⚠️ Produto Vencido",
                message = "${produto.nome} venceu em $dataFormatada — Estoque: ${produto.quantidadeAtual} ${produto.unidade}"
            )
        }

        // Envia notificação para produtos próximos do vencimento (mas não vencidos)
        for (produto in proximosVencimento) {
            if (produto.id in idVencidos) continue // Já notificado como vencido
            if (produto.dataValidade == 0L) continue // Pula produtos sem data de validade
            val diasRestantes = DateUtils.diasParaVencer(produto.dataValidade)
            val dataFormatada = DateUtils.formatarData(produto.dataValidade)
            sendNotification(
                id = notificationId++,
                title = "🕐 Produto Próximo do Vencimento",
                message = "${produto.nome} vence em $diasRestantes dia(s) ($dataFormatada) — Estoque: ${produto.quantidadeAtual} ${produto.unidade}"
            )
        }

        return Result.success()
    }

    /** Cria o canal de notificação (obrigatório no Android 8.0+) */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESC
        }
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /** Envia uma notificação local com o título e mensagem informados */
    private fun sendNotification(id: Int, title: String, message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}
