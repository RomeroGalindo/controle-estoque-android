package com.example.controleestoque

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.example.controleestoque.worker.ExpirationCheckWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Classe Application do aplicativo.
 * Inicializa o Hilt (injeção de dependência) e agenda o WorkManager
 * para verificação diária de validades.
 */
@HiltAndroidApp
class ControleEstoqueApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        agendarVerificacaoDiaria()
    }

    /**
     * Agenda o worker de verificação de validade para executar diariamente.
     * Usa ExistingPeriodicWorkPolicy.KEEP para não reagendar se já existir.
     *
     * O worker roda uma vez por dia e verifica produtos próximos do vencimento/vencidos,
     * disparando notificações locais conforme necessário.
     */
    private fun agendarVerificacaoDiaria() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ExpirationCheckWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ExpirationCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
