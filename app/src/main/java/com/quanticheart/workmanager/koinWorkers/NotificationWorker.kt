package com.quanticheart.workmanager.koinWorkers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quanticheart.custonnotifications.debugNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationWorker(
    private val appContext: Context,
    params: WorkerParameters,
    useCaseWorker: UseCaseWorker
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        try {
            withContext(Dispatchers.IO) {
                appContext.debugNotification("Koin NotificationWorker")
                //make call and return successful result
            }
        } catch (exception: Exception) {
            // return failure
            return Result.failure()
        }
        return Result.success()
    }

    companion object {
        const val TAG = "fetch_worker"
        const val HEADLINE_TITLE = "headline_title"
        const val HEADLINE_DATE = "headline_date"
    }
}