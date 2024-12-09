package com.quanticheart.workmanager

import android.app.Application
import androidx.lifecycle.Observer
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.quanticheart.workmanager.koinWorkers.NotificationWorker
import com.quanticheart.workmanager.koinWorkers.workerModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named

class ProjectApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@ProjectApplication)
            modules(listOf(workerModule))
            workManagerFactory()
        }

        val worker = get<PeriodicWorkRequest>(named("notificationWorker"))
        scheduleNotification(worker)
        observeNotificationWorker(worker)
    }

    private fun scheduleNotification(worker: PeriodicWorkRequest) {
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            NotificationWorker.TAG,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            worker
        )
    }

    private fun observeNotificationWorker(worker: PeriodicWorkRequest) {
        val workerInfo = WorkManager.getInstance(this).getWorkInfoByIdLiveData(worker.id)
        var workObserver: Observer<WorkInfo?>? = null
        workerInfo.observeForever(Observer<WorkInfo?> { workInfo ->
            when (workInfo?.state) {
                WorkInfo.State.ENQUEUED -> {
                }

                WorkInfo.State.RUNNING -> {
                }

                WorkInfo.State.SUCCEEDED -> {
                    workObserver?.let {
                        workerInfo.removeObserver(it)
                    }
                }

                WorkInfo.State.FAILED -> {
                    workObserver?.let { workerInfo.removeObserver(it) }

                }

                WorkInfo.State.BLOCKED -> {
                    workObserver?.let { workerInfo.removeObserver(it) }

                }

                WorkInfo.State.CANCELLED -> {
                    workObserver?.let { workerInfo.removeObserver(it) }
                }

                null -> {}
            }
        }.also {
            workObserver = it
        })
    }

    override val workManagerConfiguration: Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}