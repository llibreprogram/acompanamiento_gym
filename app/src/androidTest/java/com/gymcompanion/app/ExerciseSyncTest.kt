package com.gymcompanion.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.gymcompanion.app.worker.ExerciseSyncWorker
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

/**
 * Test de instrumentación para verificar la sincronización de ejercicios
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class ExerciseSyncTest {

    @Inject
    lateinit var workManager: WorkManager

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Configurar WorkManager para testing
        val config = Configuration.Builder()
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun testExerciseSyncWorker() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Crear request para el worker
        val request = androidx.work.OneTimeWorkRequestBuilder<ExerciseSyncWorker>()
            .build()

        // Enqueue el trabajo
        workManager.enqueue(request)

        // Esperar a que termine (con timeout)
        val workInfo = withTimeout(30000) { // 30 segundos timeout
            var info: WorkInfo?
            do {
                info = workManager.getWorkInfoById(request.id).get()
                kotlinx.coroutines.delay(1000) // Esperar 1 segundo
            } while (info?.state == WorkInfo.State.RUNNING || info?.state == WorkInfo.State.ENQUEUED)
            info
        }

        // Verificar que el trabajo terminó exitosamente
        assertEquals("El trabajo debería terminar exitosamente", WorkInfo.State.SUCCEEDED, workInfo?.state)

        // Verificar que se sincronizaron ejercicios
        val outputData = workInfo?.outputData
        val totalSynced = outputData?.getInt(ExerciseSyncWorker.TOTAL_KEY, 0) ?: 0
        assertTrue("Deberían sincronizarse al menos algunos ejercicios", totalSynced > 0)

        println("✅ Test exitoso: Se sincronizaron $totalSynced ejercicios")
    }
}