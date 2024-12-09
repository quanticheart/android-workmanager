package com.quanticheart.workmanager

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit

object WorkerHelper {

    inline fun <reified T : ListenableWorker> singleWorker(
        context: Context,
        tag: String? = null,
        constraints: Constraints.Builder? = null,
        data: Data.Builder? = null,
        startExactHour: Int? = null,
    ): Pair<String, OneTimeWorkRequest> {

        val builder = OneTimeWorkRequestBuilder<T>()

        if (constraints != null)
            builder.setConstraints(constraints.build())

        if (data != null)
            builder.setInputData(data.build())

        if (startExactHour != null)
            builder.setInitialDelay(calculateDelay(startExactHour), TimeUnit.MINUTES)

        val tagToWorker = tag ?: generateUUID()
        val worker = builder
            .addTag(tagToWorker)
            .build()

        WorkManager.getInstance(context).enqueue(worker)
        return Pair(tagToWorker, worker)
    }

    inline fun <reified T : ListenableWorker> periodicWorker(
        context: Context,
        repeatInterval: Long = 1,
        repeatIntervalTimeUnit: TimeUnit = TimeUnit.MICROSECONDS,
        tag: String? = null,
        constraints: Constraints.Builder? = null,
        data: Data.Builder? = null,
        startExactHour: Int? = null,
    ): Pair<String, PeriodicWorkRequest> {
        val builder = PeriodicWorkRequestBuilder<T>(
            repeatInterval, repeatIntervalTimeUnit
        )

        if (constraints != null)
            builder.setConstraints(constraints.build())

        if (data != null)
            builder.setInputData(data.build())

        if (startExactHour != null)
            builder.setInitialDelay(calculateDelay(startExactHour), TimeUnit.MINUTES)

        val tagToWorker = tag ?: generateUUID()
        val worker = builder
            .addTag(tagToWorker)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            generateUUID(),
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            worker
        )
        return Pair(tagToWorker, worker)
    }

    fun cancelWorkerByTag(context: Context, tag: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag(tag)
    }

    fun cancelAllWorkers(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
    }

    fun cancelWorkerById(context: Context, worker: WorkRequest) {
        WorkManager.getInstance(context).cancelWorkById(worker.id)
    }

    fun getObserver(context: Context, workerID: UUID) {
        WorkManager.getInstance(context).getWorkInfoByIdLiveData(workerID)
            .observeForever {
                when (it?.state) {
                    WorkInfo.State.ENQUEUED -> {
                    }

                    WorkInfo.State.RUNNING -> {
                    }

                    WorkInfo.State.SUCCEEDED -> {

                    }

                    WorkInfo.State.FAILED -> {

                    }

                    WorkInfo.State.BLOCKED -> {
                    }

                    WorkInfo.State.CANCELLED -> {
                    }

                    null -> {

                    }
                }
            }
    }

    fun generateUUID(): String {
        val uuid = UUID.randomUUID()
        val bytes = uuid.toString().toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun calculateDelay(hourOrDay: Int): Long {

//        This is my new defaultval currentDate = Calendar.getInstance()
//        val dueDate = Calendar.getInstance()
//// Set Execution around 05:00:00 AM
//        dueDate.set(Calendar.HOUR_OF_DAY, 5)
//        dueDate.set(Calendar.MINUTE, 0)
//        dueDate.set(Calendar.SECOND, 0)
//        if (dueDate.before(currentDate)) {
//            dueDate.add(Calendar.HOUR_OF_DAY, 24)
//        }
//        val timeDiff = dueDate.timeInMillis — currentDate.timeInMillis

        val now = java.util.Calendar.getInstance()
        val currentHour = now.get(java.util.Calendar.HOUR_OF_DAY)

        return if (currentHour < hourOrDay) {
            val reminderCalendar = java.util.Calendar.getInstance()
            reminderCalendar.set(java.util.Calendar.HOUR_OF_DAY, hourOrDay)
            reminderCalendar.set(java.util.Calendar.MINUTE, 0)
            reminderCalendar.set(java.util.Calendar.SECOND, 0)
            (reminderCalendar.timeInMillis - now.timeInMillis) / (60 * 1000) // Delay in minutes
        } else {
            val reminderCalendar = java.util.Calendar.getInstance()
            reminderCalendar.add(java.util.Calendar.DAY_OF_YEAR, 1) // Add one day
            reminderCalendar.set(java.util.Calendar.HOUR_OF_DAY, hourOrDay)
            reminderCalendar.set(java.util.Calendar.MINUTE, 0)
            reminderCalendar.set(java.util.Calendar.SECOND, 0)
            (reminderCalendar.timeInMillis - now.timeInMillis) / (60 * 1000) // Delay in minutes
        }
    }
}