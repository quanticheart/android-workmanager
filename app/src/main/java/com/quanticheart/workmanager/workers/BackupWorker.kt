package com.quanticheart.workmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.net.SocketException

class BackupWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) {
    override fun doWork(): Result {

        val context = applicationContext
        val name = inputData.getString("NAME")
        Log.d("doWork", "=$name")

        // Perform your long-running backup operation here.
        // Return SUCCESS if the task was successful.
        // Return FAILURE if the task failed and you don’t want to retry it.
        // Return RETRY if the task failed and you want to retry it.
        try {
            backupData(context)
            val data = workDataOf("RESULT" to "Timer 01")
            return Result.success(data)
        } catch (e: Exception) {
            // only retry 3 times
            if (runAttemptCount > 3) {
                Log.d("runAttemptCount=$runAttemptCount", " stop retry")
                return Result.success()
            }
            // retry if network failure, else considered failed
            when (e.cause) {
                is SocketException -> {
                    Log.e(e.toString(), e.toString())
                    return Result.retry()
                }

                else -> {
                    Log.e(e.toString(), e.toString())
                    return Result.failure()
                }
            }
        }
    }

    private fun backupData(context: Context) {

    }
}