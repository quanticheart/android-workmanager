package com.quanticheart.workmanager.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.quanticheart.custonnotifications.debugNotification

class PeriodicExactWorker(private val context: Context, params: WorkerParameters) :
    Worker(context, params) {
    override fun doWork(): Result {
        context.debugNotification("PeriodicExactWorker")
        return Result.success()
    }
}