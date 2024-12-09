package com.quanticheart.workmanager.koinWorkers

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.UUID
import java.util.concurrent.TimeUnit

val workerModule = module {
    factory { RepositoryWorker(androidContext()) }
    factory { UseCaseWorker(get()) }
    worker { NotificationWorker(get(), get(), get()) }

    factory<PeriodicWorkRequest>(named("notificationWorker")) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        return@factory PeriodicWorkRequest.Builder(
            NotificationWorker::class.java,
            15,
            TimeUnit.MINUTES
        )
            .setId(UUID.randomUUID())
            .setConstraints(constraints)
            .addTag(NotificationWorker.TAG)
            .build()
    }

}