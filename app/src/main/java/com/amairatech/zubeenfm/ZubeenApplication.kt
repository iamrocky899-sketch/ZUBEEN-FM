package com.amairatech.zubeenfm

import android.app.Application
import android.util.Log
import com.amairatech.zubeenfm.data.provider.SongCatalogueSyncManager

class ZubeenApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d("ZubeenApp", "ZubeenApplication created")
        
        // Schedule automatic background catalogue synchronization
        SongCatalogueSyncManager.schedulePeriodicSync(this)
    }

    companion object {
        lateinit var instance: ZubeenApplication
            private set
    }
}
