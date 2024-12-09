package com.quanticheart.workmanager

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Data
import com.quanticheart.workmanager.workers.PeriodicExactWorker
import com.quanticheart.workmanager.workers.PeriodicWorker
import com.quanticheart.workmanager.workers.SingleWorker
import com.quanticheart.workmanager.workers.scheduleDailyWorkerTest

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // In your activity or service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    10101
                )
            }
        }

        scheduleDailyWorkerTest()

        val data = Data.Builder()
            .putString("DATA_USER_FIRSTNAME", "John")

        WorkerHelper.singleWorker<SingleWorker>(applicationContext)
        WorkerHelper.periodicWorker<PeriodicWorker>(applicationContext, data = data)
        WorkerHelper.periodicWorker<PeriodicExactWorker>(applicationContext, startExactHour = 10)
    }
}