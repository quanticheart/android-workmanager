package com.quanticheart.workmanager.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.quanticheart.custonnotifications.debugNotification

class SingleWorker(private val context: Context, params: WorkerParameters) :
    Worker(context, params) {
    override fun doWork(): Result {
        context.debugNotification("SingleWorker")
        return Result.success()
    }
}