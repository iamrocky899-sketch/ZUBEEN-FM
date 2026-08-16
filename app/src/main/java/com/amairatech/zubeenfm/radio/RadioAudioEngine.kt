package com.amairatech.zubeenfm.radio

import android.content.Context
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

import androidx.media3.common.util.UnstableApi
import androidx.annotation.OptIn

/**
 * Modernized, battery-optimized audio playback engine for ZUBEEN FM.
 * Uses Media3 ExoPlayer for high stability and smooth streaming.
 */
import androidx.media3.session.MediaSession

@OptIn(UnstableApi::class)
class RadioAudioEngine(
    private val context: Context,
    private val scope: CoroutineScope
) : Player.Listener {

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var streamPreparedTimestamp: Long = 0L
    private var onPreparedCallback: ((Int) -> Unit)? = null
    private var onCompletedCallback: (() -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    private val _waveformAmplitudes = MutableStateFlow(List(16) { 0.1f })
    val waveformAmplitudes: StateFlow<List<Float>> = _waveformAmplitudes.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlayingFlow: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    private var visualizerJob: Job? = null
    private var isUiVisible = true

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        if (exoPlayer != null) return

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                40_000, // minBuffer: 40s
                80_000, // maxBuffer: 80s
                2500,   // bufferForPlayback: 2.5s
                5000    // bufferForPlaybackAfterRebuffer: 5s
            )
            .setBackBuffer(10000, true)
            .build()

        val player = ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true // handleAudioFocus
            )
            .build().apply {
                addListener(this@RadioAudioEngine)
            }
        
        exoPlayer = player
        val session = MediaSession.Builder(context, player)
            .setId("ZubeenFMRadioSession_${System.currentTimeMillis()}")
            .build()
        mediaSession = session
        
        // Connect to the Foreground Service
        com.amairatech.zubeenfm.playback.ZubeenMediaPlaybackService.setSession(session)
        
        Log.d("ZubeenAudioDebug", "PLAYER_RECREATED = 1 (Initial with MediaSession)")
    }

    fun isPrepared(): Boolean = exoPlayer?.playbackState != Player.STATE_IDLE

    fun hasValidStream(): Boolean {
        val player = exoPlayer ?: return false
        if (player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED) return false
        val elapsed = System.currentTimeMillis() - streamPreparedTimestamp
        return elapsed < (3.5 * 60 * 60 * 1000L)
    }

    fun playSongStream(
        streamUrl: String?,
        initialSeekPositionMs: Int = 0,
        mode: String = "RADIO",
        contentId: String = "unknown",
        onPrepared: ((durationMs: Int) -> Unit)? = null,
        onCompleted: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        _playbackError.value = null
        onPreparedCallback = onPrepared
        onCompletedCallback = onCompleted
        onErrorCallback = onError
        
        Log.d("ZubeenAudioDebug", "PLAY_REQUEST mode=$mode id=$contentId url=${streamUrl?.take(50)}...")

        if (streamUrl.isNullOrEmpty()) {
            _playbackError.value = "Playback unavailable"
            onError?.invoke("Playback unavailable")
            return
        }

        val player = exoPlayer ?: return
        
        scope.launch(Dispatchers.Main) {
            val mediaItem = MediaItem.Builder()
                .setUri(streamUrl)
                .setMediaId(contentId)
                .build()

            player.setMediaItem(mediaItem)
            player.seekTo(initialSeekPositionMs.toLong()) // Explicitly seek to avoid stale position
            player.prepare()
            player.playWhenReady = true
            
            Log.d("ZubeenAudioDebug", "PLAYER_PREPARING mode=$mode id=$contentId seek=$initialSeekPositionMs")
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        val mode = "RADIO" // Simplified for logic
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                _isBuffering.value = true
                Log.d("ZubeenAudioDebug", "BUFFERING_START mode=$mode pos=${exoPlayer?.currentPosition}")
            }
            Player.STATE_READY -> {
                _isBuffering.value = false
                streamPreparedTimestamp = System.currentTimeMillis()
                val duration = exoPlayer?.duration?.toInt() ?: 0
                Log.d("ZubeenAudioDebug", "PLAYER_READY mode=$mode pos=${exoPlayer?.currentPosition} buffered=${exoPlayer?.bufferedPosition} duration=$duration")
                onPreparedCallback?.invoke(duration)
            }
            Player.STATE_ENDED -> {
                _isBuffering.value = false
                stopVisualizer()
                Log.d("ZubeenAudioDebug", "TRACK_CHANGED mode=$mode (Ended)")
                onCompletedCallback?.invoke()
            }
            Player.STATE_IDLE -> {
                _isBuffering.value = false
                stopVisualizer()
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
        if (isPlaying && isUiVisible) {
            startVisualizer()
        } else if (!isPlaying) {
            stopVisualizer()
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        Log.e("ZubeenAudioDebug", "PLAYER_ERROR code=${error.errorCode} message=${error.message}")
        _playbackError.value = "Playback error: ${error.errorCodeName}"
        onErrorCallback?.invoke(error.message ?: "Unknown player error")
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        if (reason == Player.DISCONTINUITY_REASON_SEEK) {
            Log.d("ZubeenAudioDebug", "SEEK_COMPLETED from=${oldPosition.positionMs} to=${newPosition.positionMs}")
        }
    }

    fun pauseAudio() {
        exoPlayer?.playWhenReady = false
        stopVisualizer()
        Log.d("ZubeenAudioDebug", "PLAYER_PAUSED pos=${exoPlayer?.currentPosition}")
    }

    fun resumeAudio(): Boolean {
        val player = exoPlayer ?: return false
        if (player.playbackState != Player.STATE_IDLE) {
            player.playWhenReady = true
            if (isUiVisible) startVisualizer()
            Log.d("ZubeenAudioDebug", "PLAYER_RESUMED pos=${player.currentPosition}")
            return true
        }
        return false
    }

    fun seekToPosition(positionMs: Int) {
        exoPlayer?.seekTo(positionMs.toLong())
    }

    fun getCurrentPositionMs(): Int = exoPlayer?.currentPosition?.toInt() ?: 0

    fun isPlaying(): Boolean = _isPlaying.value

    fun setUiVisibility(visible: Boolean) {
        isUiVisible = visible
        if (visible && _isPlaying.value) {
            startVisualizer()
        } else {
            stopVisualizer()
        }
    }

    private fun startVisualizer() {
        visualizerJob?.cancel()
        visualizerJob = scope.launch(Dispatchers.Default) {
            var phase = 0f
            while (isActive && isUiVisible) {
                if (_isBuffering.value || !_isPlaying.value) {
                    _waveformAmplitudes.value = List(16) { 0.1f }
                    delay(500)
                    continue
                }

                val factor = 0.65f
                val newAmplitudes = (0 until 16).map { i ->
                    val wave = sin(phase + i * 0.4f)
                    val normalized = (wave + 1f) / 2f
                    (normalized * factor + (0.1f * Math.random().toFloat())).coerceIn(0.08f, 1.0f)
                }
                _waveformAmplitudes.value = newAmplitudes
                phase += 0.35f
                delay(66) // ~15 FPS optimized
            }
        }
    }

    private fun stopVisualizer() {
        visualizerJob?.cancel()
        visualizerJob = null
        _waveformAmplitudes.value = List(16) { 0.1f }
    }

    fun release() {
        exoPlayer?.removeListener(this)
        mediaSession?.release()
        mediaSession = null
        exoPlayer?.release()
        exoPlayer = null
        stopVisualizer()
    }
}
