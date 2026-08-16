package com.amairatech.zubeenfm.data.provider

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SongCatalogueSyncManager {

    private const val SYNC_WORK_NAME = "SongCatalogueSyncWork"

    fun schedulePeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SongCatalogueSyncWorker>(
            12, TimeUnit.HOURS // Sync twice a day
        )
            .setConstraints(constraints)
            .setInitialDelay(30, TimeUnit.MINUTES) // Start first sync after some time
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing if scheduled
            syncRequest
        )
    }

    fun startOneTimeSync(context: Context) {
        // Implementation for immediate sync if needed
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = androidx.work.OneTimeWorkRequestBuilder<SongCatalogueSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(syncRequest)
    }
}
