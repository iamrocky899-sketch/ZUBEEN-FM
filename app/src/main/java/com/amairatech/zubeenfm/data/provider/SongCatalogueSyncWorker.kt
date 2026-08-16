package com.amairatech.zubeenfm.data.provider

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.amairatech.zubeenfm.data.repository.NormalCatalogueRepository

class SongCatalogueSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.i("ZubeenSync", "Background catalogue sync started...")
        
        return try {
            val normalSuccess = NormalCatalogueRepository.providerManager.loadCompleteNormalCatalogue(isRefresh = true)
            
            // Also refresh Radio catalogue pool incrementally
            val radioSuccess = com.amairatech.zubeenfm.data.repository.ZubeenRadioCatalogueRepository.providerManager.loadNextRadioPage()
            
            if (normalSuccess || radioSuccess) {
                Log.i("ZubeenSync", "Background catalogue sync COMPLETED. Normal success=$normalSuccess, Radio pool updated=$radioSuccess")
                Result.success()
            } else {
                Log.w("ZubeenSync", "Background catalogue sync reported NO NEW DATA or partial failure.")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("ZubeenSync", "Background catalogue sync CRASHED: ${e.message}", e)
            Result.failure()
        }
    }
}
