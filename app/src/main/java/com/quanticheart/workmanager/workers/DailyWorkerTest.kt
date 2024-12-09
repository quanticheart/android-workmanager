package com.quanticheart.workmanager.workers

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.quanticheart.custonnotifications.debugNotification
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyWorkerTest(private val ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        ctx.debugNotification("DailyWorkerTest 1")
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        // Set Execution around 05:00:00 AM
        dueDate.set(Calendar.HOUR_OF_DAY, 1)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        applicationContext.scheduleDailyWorkerTest(timeDiff)
        return Result.success()
    }
}

fun Context.scheduleDailyWorkerTest(timeDiff: Long = 1) {
    val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyWorkerTest>().setInitialDelay(
        timeDiff,
        TimeUnit.MILLISECONDS
    ).addTag("DailyWorker").build()
    WorkManager.getInstance(applicationContext).enqueue(dailyWorkRequest)
}