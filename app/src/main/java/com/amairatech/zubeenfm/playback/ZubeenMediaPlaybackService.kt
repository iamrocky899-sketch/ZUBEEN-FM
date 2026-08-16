package com.amairatech.zubeenfm.playback

import android.content.Context
import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.annotation.OptIn
import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.ui.PlaybackMode

/**
 * Modernized MediaSession Service for ZUBEEN FM.
 * Uses Media3 Session to manage playback state and notifications efficiently.
 */
class ZubeenMediaPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        Log.d("ZubeenAudioDebug", "MEDIA_SERVICE_CREATED")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession ?: getActiveSession()
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
        Log.d("ZubeenAudioDebug", "MEDIA_SERVICE_DESTROYED")
    }

    /**
     * Set the active MediaSession. 
     * In this architecture, the player is managed by RadioAudioEngine.
     */
    fun setSession(session: MediaSession) {
        this.mediaSession = session
    }

    companion object {
        private var eventListener: PlaybackEventListener? = null

        fun setEventListener(listener: PlaybackEventListener?) {
            this.eventListener = listener
        }

        fun setSession(session: MediaSession) {
            // Store session for the service instance
            // In a real app, we'd use a more robust way to pass this, 
            // but here we can use a temporary static holder that the service reads in onGetSession
            activeSession = session
        }

        private var activeSession: MediaSession? = null
        
        fun getActiveSession(): MediaSession? = activeSession


        fun getEventListener(): PlaybackEventListener? = eventListener

        /**
         * Bridge method to update the foreground service state from the ViewModel.
         */
        fun updatePlayback(
            context: Context,
            song: Song,
            isPlaying: Boolean,
            mode: PlaybackMode,
            elapsedSeconds: Int
        ) {
            Log.d("ZubeenPlayback", "Service updatePlayback: ${song.titleEnglish} isPlaying=$isPlaying")
            // In a full implementation, this would update the notification via MediaSession or Intent
        }

        /**
         * Bridge method to stop the foreground service.
         */
        fun stopPlaybackService(context: Context) {
            Log.d("ZubeenPlayback", "Service stopPlaybackService")
            context.stopService(Intent(context, ZubeenMediaPlaybackService::class.java))
        }
    }
}
