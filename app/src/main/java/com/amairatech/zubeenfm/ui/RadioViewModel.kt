package com.amairatech.zubeenfm.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amairatech.zubeenfm.data.model.Album
import com.amairatech.zubeenfm.data.model.Artist
import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.YouTubeMusicProvider
import com.amairatech.zubeenfm.data.repository.NormalCatalogueRepository
import com.amairatech.zubeenfm.data.repository.ZubeenRadioCatalogueRepository
import com.amairatech.zubeenfm.playback.PlaybackEventListener
import com.amairatech.zubeenfm.playback.ZubeenMediaPlaybackService
import com.amairatech.zubeenfm.radio.RadioAudioEngine
import com.amairatech.zubeenfm.radio.RadioSongEligibilityChecker
import com.amairatech.zubeenfm.radio.RadioStationClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Main ViewModel for ZUBEEN FM.
 * Strictly separates:
 * 1. Radio Mode: Universal Synchronized Zubeen Broadcast (RadioStationClock, Play/Pause only, independent queue & history, instant pause->play fast resume).
 * 2. Normal Mode: Assamese Music Player (Songs, Albums, Artists, authoritative seekbar, background preloading).
 * Battery-optimized: No high-frequency station clock loops, UI-gated visualizer, decoupled foreground service.
 * MediaSession & Notification integrated with PlaybackEventListener bridge.
 */
class RadioViewModel(
    application: Application,
    private val audioEngine: RadioAudioEngine = RadioAudioEngine(application.applicationContext, CoroutineScopeHolder.scope)
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        audioEngine = RadioAudioEngine(application.applicationContext, CoroutineScopeHolder.scope)
    )

    private object CoroutineScopeHolder {
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    }

    private val _uiState = MutableStateFlow(
        RadioUiState(
            radioCurrentSong = ZubeenRadioCatalogueRepository.playlist.first(),
            normalCurrentSong = NormalCatalogueRepository.playlist.first(),
            isRadioPlaying = false,
            radioPlaybackState = RadioPlaybackState.IDLE,
            userRequestedRadioPlayback = false,
            activePlaybackMode = PlaybackMode.RADIO
        )
    )
    val uiState: StateFlow<RadioUiState> = _uiState.asStateFlow()
    
    val waveformAmplitudes: StateFlow<List<Float>> = audioEngine.waveformAmplitudes

    private var radioProgressJob: Job? = null
    private var normalProgressJob: Job? = null
    private var radioPlaybackFailureAttempts = 0
    private var lastRadioRecoveryPositionMs = 0
    private var radioPauseTimestampMs = 0L

    init {
        Log.d("ZubeenPlayback", "RadioViewModel initialized. Wiring MediaSession...")

        // Attach MediaSession / Lock Screen / Notification transport listener
        ZubeenMediaPlaybackService.setEventListener(object : PlaybackEventListener {
            override fun onPlayPauseRequested() {
                viewModelScope.launch(Dispatchers.Main) {
                    togglePlayPause()
                }
            }

            override fun onNextRequested() {
                viewModelScope.launch(Dispatchers.Main) {
                    if (_uiState.value.activePlaybackMode == PlaybackMode.NORMAL) {
                        playNextSong()
                    }
                }
            }

            override fun onPreviousRequested() {
                viewModelScope.launch(Dispatchers.Main) {
                    if (_uiState.value.activePlaybackMode == PlaybackMode.NORMAL) {
                        playPreviousSong()
                    }
                }
            }

            override fun onSeekRequested(positionMs: Long) {
                viewModelScope.launch(Dispatchers.Main) {
                    if (_uiState.value.activePlaybackMode == PlaybackMode.NORMAL) {
                        seekTo((positionMs / 1000).toInt())
                    }
                }
            }

            override fun onStopRequested() {
                viewModelScope.launch(Dispatchers.Main) {
                    if (_uiState.value.activePlaybackMode == PlaybackMode.RADIO) {
                        pauseRadioBroadcast()
                    } else {
                        if (_uiState.value.isNormalPlaying) {
                            togglePlayPauseNormal()
                        }
                    }
                }
            }
        })

        // Initial setup without auto-play
        val initialSlot = ZubeenRadioCatalogueRepository.getCurrentStationBroadcastSlot()
        _uiState.update { it.copy(radioCurrentSong = initialSlot.song) }

        // Observe waveform visualizer
        viewModelScope.launch {
            audioEngine.waveformAmplitudes.collect { amps ->
                _uiState.update { it.copy(waveformAmplitudes = amps) }
            }
        }

        // Observe Normal favorites and recently played
        viewModelScope.launch {
            NormalCatalogueRepository.favoritesFlow.collect { favs ->
                _uiState.update { it.copy(favoriteSongIds = favs) }
            }
        }
        viewModelScope.launch {
            NormalCatalogueRepository.recentlyPlayedFlow.collect { recents ->
                _uiState.update { it.copy(recentlyPlayedSongs = recents) }
            }
        }
    }

    fun onAppForegroundChanged(isForeground: Boolean) {
        Log.i("ZubeenRadioDebug", "RADIO_LIFECYCLE_CHANGED isForeground=$isForeground")
        audioEngine.setUiVisibility(isForeground)
    }

    // ==========================================
    // 1. RADIO MODE (SYNCHRONIZED BROADCAST)
    // ==========================================

    fun togglePlayPauseRadio() {
        val currentlyPlaying = _uiState.value.isRadioPlaying && _uiState.value.activePlaybackMode == PlaybackMode.RADIO
        Log.d("ZubeenPlayback", "togglePlayPauseRadio: currentlyPlaying=$currentlyPlaying")

        if (currentlyPlaying) {
            _uiState.update { it.copy(userRequestedRadioPlayback = false) }
            pauseRadioBroadcast()
        } else {
            _uiState.update { it.copy(userRequestedRadioPlayback = true) }
            resumeOrSyncRadioBroadcast()
        }
    }

    fun togglePlayPause() {
        if (_uiState.value.activePlaybackMode == PlaybackMode.RADIO) {
            togglePlayPauseRadio()
        } else {
            togglePlayPauseNormal()
        }
    }

    private fun pauseRadioBroadcast() {
        val tPauseReq = System.currentTimeMillis()
        Log.i("ZubeenRadioDebug", "RADIO_PAUSED item=[${_uiState.value.radioCurrentSong.titleEnglish}]")
        radioPauseTimestampMs = tPauseReq

        _uiState.update { it.copy(isRadioPlaying = false, radioPlaybackState = RadioPlaybackState.PAUSED) }
        radioProgressJob?.cancel()
        radioProgressJob = null
        audioEngine.pauseAudio()

        val tPaused = System.currentTimeMillis()
        Log.d("ZubeenPlayback", "[Timing] Radio paused: $tPaused (Delta: ${tPaused - tPauseReq}ms)")

        ZubeenMediaPlaybackService.updatePlayback(
            context = getApplication(),
            song = _uiState.value.radioCurrentSong,
            isPlaying = false,
            mode = PlaybackMode.RADIO,
            elapsedSeconds = _uiState.value.radioElapsedSeconds
        )
    }

    private fun resumeOrSyncRadioBroadcast() {
        if (!_uiState.value.userRequestedRadioPlayback) {
            Log.i("ZubeenRadioDebug", "RADIO_AUTOPLAY_SUPPRESSED: userRequestedRadioPlayback=false. Resume aborted.")
            return
        }

        val tPlayReq = System.currentTimeMillis()
        Log.i("ZubeenRadioDebug", "RADIO_PLAY_REQUEST timestamp=$tPlayReq")

        val currentSlot = ZubeenRadioCatalogueRepository.getCurrentStationBroadcastSlot(tPlayReq)
        val scheduledSong = currentSlot.song
        val targetOffsetMs = currentSlot.intraSongOffsetMs
        val targetOffsetSec = (targetOffsetMs / 1000).toInt()

        // 1. Mandatory Radio Pre-Playback Safety Gate Check
        if (!RadioSongEligibilityChecker.isEligibleForRadio(scheduledSong)) {
            Log.e("ZubeenPlayback", "Radio Fast Resume REJECTED non-Zubeen recording: [${scheduledSong.titleEnglish}] by [${scheduledSong.artistEnglish}]. Skipping...")
            handleRadioPlaybackFailure(scheduledSong)
            return
        }

        // CASE A: Fast Resume — player is prepared with valid stream and song is still current
        if (_uiState.value.radioCurrentSong.id == scheduledSong.id && audioEngine.isPrepared() && audioEngine.hasValidStream()) {
            Log.d("ZubeenPlayback", "[Timing] Fast resume: Existing player and stream valid for [${scheduledSong.titleEnglish}]. Seeking to offset ${targetOffsetSec}s and resuming immediately.")
            _uiState.update {
                it.copy(
                    activePlaybackMode = PlaybackMode.RADIO,
                    radioCurrentSong = scheduledSong,
                    isRadioPlaying = true,
                    radioPlaybackState = RadioPlaybackState.PLAYING,
                    isNormalPlaying = false,
                    radioElapsedSeconds = targetOffsetSec,
                    radioSongProgress = (targetOffsetSec.toFloat() / scheduledSong.durationSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f),
                    isRadioBuffering = false,
                    errorMessage = null
                )
            }

            audioEngine.seekToPosition(targetOffsetMs.toInt())
            val resumed = audioEngine.resumeAudio()
            if (resumed) {
                Log.i("ZubeenRadioDebug", "RADIO_PLAY_STARTED mode=FAST_RESUME item=[${scheduledSong.titleEnglish}] offset=${targetOffsetSec}s")
                startRadioProgressTicker(scheduledSong, targetOffsetSec)
                ZubeenMediaPlaybackService.updatePlayback(
                    context = getApplication(),
                    song = scheduledSong,
                    isPlaying = true,
                    mode = PlaybackMode.RADIO,
                    elapsedSeconds = targetOffsetSec
                )
                return
            }
        }

        // CASE B: Track changed or stream expired — full station sync
        Log.d("ZubeenPlayback", "[Timing] Full station sync required for [${scheduledSong.titleEnglish}] at offset ${targetOffsetSec}s")
        syncAndPlayRadioBroadcast()
    }

    fun syncAndPlayRadioBroadcast() {
        if (!_uiState.value.userRequestedRadioPlayback) {
            Log.i("ZubeenRadioDebug", "RADIO_AUTOPLAY_SUPPRESSED: syncAndPlayRadioBroadcast aborted because userRequestedRadioPlayback=false")
            return
        }

        radioProgressJob?.cancel()
        normalProgressJob?.cancel()

        // Calculate current station broadcast slot once from universal station clock
        val slot = ZubeenRadioCatalogueRepository.getCurrentStationBroadcastSlot()
        val song = slot.song

        // 1. Mandatory Pre-Playback Safety Gate: Radio MUST NEVER play non-Zubeen songs
        if (!RadioSongEligibilityChecker.isEligibleForRadio(song)) {
            Log.e("ZubeenPlayback", "Radio Station Clock REJECTED ineligible recording: [${song.titleEnglish}] by [${song.artistEnglish}]. Skipping to next verified song...")
            handleRadioPlaybackFailure(song)
            return
        }

        // Avoid premature track-end cutoff: If offset is within 10s of duration, start at beginning
        val rawOffsetSeconds = (slot.intraSongOffsetMs / 1000).toInt()
        val offsetSeconds = if (rawOffsetSeconds >= (song.durationSeconds - 10)) 0 else rawOffsetSeconds
        val progress = (offsetSeconds.toFloat() / song.durationSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

        Log.i("ZubeenRadioDebug", "RADIO_TRACK_CHANGED item=[${song.titleEnglish}] offset=${offsetSeconds}s duration=${song.durationSeconds}s")

        _uiState.update {
            it.copy(
                activePlaybackMode = PlaybackMode.RADIO,
                radioCurrentSong = song,
                isRadioPlaying = true,
                radioPlaybackState = RadioPlaybackState.BUFFERING,
                isNormalPlaying = false,
                radioElapsedSeconds = offsetSeconds,
                radioSongProgress = progress,
                errorMessage = null,
                isRadioUnavailable = false
            )
        }

        ZubeenRadioCatalogueRepository.recordRadioHistory(song)
        ZubeenMediaPlaybackService.updatePlayback(
            context = getApplication(),
            song = song,
            isPlaying = true,
            mode = PlaybackMode.RADIO,
            elapsedSeconds = offsetSeconds
        )

        viewModelScope.launch {
            try {
                if (!_uiState.value.userRequestedRadioPlayback) return@launch
                Log.i("ZubeenRadioDebug", "RADIO_BUFFERING item=[${song.titleEnglish}]")
                _uiState.update { it.copy(isRadioBuffering = true, radioPlaybackState = RadioPlaybackState.BUFFERING) }
                Log.d("ZubeenAudioDebug", "SOURCE_RESOLUTION_START mode=RADIO id=${song.id}")
                val streamUrl = if (song.streamUrl?.startsWith("http") == true) {
                    song.streamUrl
                } else {
                    ZubeenRadioCatalogueRepository.providerManager.resolveStreamUrl(song) ?: song.streamUrl
                }
                Log.d("ZubeenAudioDebug", "SOURCE_RESOLUTION_END mode=RADIO id=${song.id} success=${streamUrl != null}")

                if (!_uiState.value.userRequestedRadioPlayback) return@launch

                if (!RadioSongEligibilityChecker.verifyPlaybackSource(song, streamUrl)) {
                    Log.w("ZubeenPlayback", "Radio stream verification failed for: ${song.titleEnglish}")
                    handleRadioPlaybackFailure(song)
                    return@launch
                }

                Log.i("ZubeenRadioDebug", "RADIO_SOURCE_CHANGED item=[${song.titleEnglish}]")
                
                // Diagnostic logging
                val sourceId = if (song.playbackSources.isNotEmpty()) song.playbackSources.first().sourceId else "none"
                Log.i("ZubeenRadioSync", "START contentId=${song.id} title=[${song.titleEnglish}] artist=[${song.artistEnglish}] duration=${song.durationSeconds}s sourceId=$sourceId resolvedSource=${streamUrl?.take(20)}... playerMediaItemId=${song.id}")

                val activeIntentId = _uiState.value.radioCurrentSong.id
                if (song.id != activeIntentId) {
                    Log.e("ZubeenRadioSync", "MISMATCH: current coroutine song (${song.id}) does not match UI intent ($activeIntentId). Aborting playback for stale request.")
                    return@launch
                }

                audioEngine.playSongStream(
                    streamUrl = streamUrl,
                    initialSeekPositionMs = (offsetSeconds * 1000),
                    mode = "RADIO",
                    contentId = song.id,
                    onPrepared = { realDurationMs ->
                        if (!_uiState.value.userRequestedRadioPlayback) {
                            audioEngine.pauseAudio()
                            return@playSongStream
                        }
                        
                        val trueDurationSecs = if (realDurationMs > 0) realDurationMs / 1000 else song.durationSeconds
                        _uiState.update { it.copy(
                            isRadioBuffering = false, 
                            radioPlaybackState = RadioPlaybackState.PLAYING,
                            radioCurrentSong = song.copy(durationSeconds = trueDurationSecs)
                        ) }
                        
                        Log.i("ZubeenRadioDebug", "RADIO_READY & RADIO_PLAY_STARTED item=[${song.titleEnglish}] realDuration=${trueDurationSecs}s")
                        startRadioProgressTicker(song.copy(durationSeconds = trueDurationSecs), offsetSeconds)
                        preloadNextRadioSlot()
                    },
                    onCompleted = {
                        Log.i("ZubeenRadioDebug", "RADIO_ENDED item=[${song.titleEnglish}]")
                        if (_uiState.value.userRequestedRadioPlayback) {
                            syncAndPlayRadioBroadcast()
                        }
                    },
                    onError = { err ->
                        Log.w("ZubeenRadioDebug", "RADIO_ERROR err=$err for [${song.titleEnglish}]")
                        if (_uiState.value.userRequestedRadioPlayback) {
                            attemptSeamlessRadioRecovery(song)
                        }
                    }
                )
            } catch (t: Throwable) {
                Log.e("ZubeenPlayback", "Error in syncAndPlayRadioBroadcast: ${t.message}", t)
                if (_uiState.value.userRequestedRadioPlayback) {
                    attemptSeamlessRadioRecovery(song)
                }
            }
        }
    }

    private fun preloadNextRadioSlot() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentSlot = ZubeenRadioCatalogueRepository.getCurrentStationBroadcastSlot()
                val playlist = ZubeenRadioCatalogueRepository.playlist
                val nextIndex = (currentSlot.sequenceIndex + 1) % playlist.size.coerceAtLeast(1)
                val nextSong = playlist[nextIndex]
                if (nextSong.playbackSources.isNotEmpty()) {
                    val ytm = ZubeenRadioCatalogueRepository.providerManager.discoveryEngine.providers.filterIsInstance<YouTubeMusicProvider>().firstOrNull()
                    ytm?.preloadStreamUrl(nextSong.playbackSources.first())
                }
            } catch (e: Exception) {
                // Background preloading ignore
            }
        }
    }

    private fun attemptSeamlessRadioRecovery(song: Song) {
        if (!_uiState.value.userRequestedRadioPlayback) {
            Log.i("ZubeenRadioDebug", "RADIO_AUTOPLAY_SUPPRESSED: recovery skipped because userRequestedRadioPlayback=false")
            return
        }

        val currentPos = audioEngine.getCurrentPositionMs()
        lastRadioRecoveryPositionMs = if (currentPos > 0) currentPos else lastRadioRecoveryPositionMs
        Log.i("ZubeenBufferDebug", "SOURCE_SWITCH (recovery) timestamp=${System.currentTimeMillis()} mode=RADIO id=${song.id} recoveryPosMs=$lastRadioRecoveryPositionMs")

        // Invalidate stale stream from provider cache
        if (song.playbackSources.isNotEmpty()) {
            val ytm = ZubeenRadioCatalogueRepository.providerManager.discoveryEngine.providers.filterIsInstance<YouTubeMusicProvider>().firstOrNull()
            ytm?.invalidateStream(song.playbackSources.first().sourceId)
        }

        viewModelScope.launch {
            try {
                if (!_uiState.value.userRequestedRadioPlayback) return@launch
                val freshStreamUrl = ZubeenRadioCatalogueRepository.providerManager.resolveStreamUrl(song) ?: song.streamUrl
                if (RadioSongEligibilityChecker.verifyPlaybackSource(song, freshStreamUrl)) {
                    audioEngine.playSongStream(
                        streamUrl = freshStreamUrl,
                        initialSeekPositionMs = lastRadioRecoveryPositionMs,
                        mode = "RADIO",
                        contentId = song.id,
                        onPrepared = { realDurationMs ->
                            if (!_uiState.value.userRequestedRadioPlayback) {
                                audioEngine.pauseAudio()
                                return@playSongStream
                            }
                            val trueDurationSecs = if (realDurationMs > 0) realDurationMs / 1000 else song.durationSeconds
                            _uiState.update { it.copy(
                                isRadioBuffering = false, 
                                radioPlaybackState = RadioPlaybackState.PLAYING,
                                radioCurrentSong = song.copy(durationSeconds = trueDurationSecs)
                            ) }
                            startRadioProgressTicker(song.copy(durationSeconds = trueDurationSecs), lastRadioRecoveryPositionMs / 1000)
                        },
                        onCompleted = {
                            if (_uiState.value.userRequestedRadioPlayback) {
                                syncAndPlayRadioBroadcast()
                            }
                        },
                        onError = {
                            if (_uiState.value.userRequestedRadioPlayback) {
                                handleRadioPlaybackFailure(song)
                            }
                        }
                    )
                } else {
                    if (_uiState.value.userRequestedRadioPlayback) {
                        handleRadioPlaybackFailure(song)
                    }
                }
            } catch (e: Exception) {
                if (_uiState.value.userRequestedRadioPlayback) {
                    handleRadioPlaybackFailure(song)
                }
            }
        }
    }

    private fun handleRadioPlaybackFailure(failedSong: Song) {
        if (!_uiState.value.userRequestedRadioPlayback) return
        radioPlaybackFailureAttempts++
        val verifiedCatalogue = ZubeenRadioCatalogueRepository.playlist.filter { RadioSongEligibilityChecker.isEligibleForRadio(it) }
        val totalSongs = verifiedCatalogue.size
        Log.w("ZubeenPlayback", "Radio song skipped/failed: ${failedSong.titleEnglish} ($radioPlaybackFailureAttempts / $totalSongs attempts)")

        if (radioPlaybackFailureAttempts < totalSongs && totalSongs > 1) {
            val currentIndex = verifiedCatalogue.indexOfFirst { it.id == failedSong.id }
            val nextSong = if (currentIndex != -1 && currentIndex + 1 < totalSongs) {
                verifiedCatalogue[currentIndex + 1]
            } else {
                verifiedCatalogue.first()
            }
            _uiState.update { it.copy(radioCurrentSong = nextSong) }
            syncAndPlayRadioBroadcast()
        } else {
            _uiState.update {
                it.copy(
                    errorMessage = "Radio unavailable",
                    isRadioUnavailable = true,
                    isRadioPlaying = false,
                    isRadioBuffering = false,
                    radioPlaybackState = RadioPlaybackState.ERROR
                )
            }
            ZubeenMediaPlaybackService.stopPlaybackService(getApplication())
        }
    }

    private fun startRadioProgressTicker(song: Song, initialSeconds: Int) {
        radioProgressJob?.cancel()
        radioProgressJob = viewModelScope.launch {
            val totalSeconds = song.durationSeconds
            var elapsed = initialSeconds

            while (isActive && _uiState.value.isRadioPlaying) {
                delay(1000L)
                val enginePosMs = audioEngine.getCurrentPositionMs()
                elapsed = if (enginePosMs > 0) (enginePosMs / 1000) else elapsed + 1
                lastRadioRecoveryPositionMs = elapsed * 1000

                val prog = (elapsed.toFloat() / totalSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                _uiState.update {
                    it.copy(
                        radioElapsedSeconds = elapsed,
                        radioSongProgress = prog
                    )
                }
            }
        }
    }

    fun selectAndPlayNormalSong(song: Song) {
        Log.d("ZubeenAudioDebug", "PLAY_REQUEST mode=NORMAL id=${song.id} title=[${song.titleEnglish}]")
        radioProgressJob?.cancel()
        normalProgressJob?.cancel()

        NormalCatalogueRepository.queueManager.onSongSelected(song)

        _uiState.update {
            it.copy(
                activePlaybackMode = PlaybackMode.NORMAL,
                normalCurrentSong = song,
                isNormalPlaying = true,
                isRadioPlaying = false,
                normalSongProgress = 0f,
                normalElapsedSeconds = 0,
                errorMessage = null
            )
        }

        NormalCatalogueRepository.recordRecentlyPlayed(song)
        ZubeenMediaPlaybackService.updatePlayback(
            context = getApplication(),
            song = song,
            isPlaying = true,
            mode = PlaybackMode.NORMAL,
            elapsedSeconds = 0
        )

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isNormalBuffering = true) }
                Log.d("ZubeenAudioDebug", "SOURCE_RESOLUTION_START mode=NORMAL id=${song.id}")
                val streamUrl = if (song.streamUrl?.startsWith("http") == true) {
                    song.streamUrl
                } else {
                    NormalCatalogueRepository.providerManager.resolveStreamUrl(song) ?: song.streamUrl
                }
                Log.d("ZubeenAudioDebug", "SOURCE_RESOLUTION_END mode=NORMAL id=${song.id} success=${streamUrl != null}")

                if (streamUrl.isNullOrBlank()) {
                    Log.w("ZubeenPlayback", "Normal stream resolution returned NULL for: ${song.titleEnglish}")
                    _uiState.update {
                        it.copy(
                            isNormalBuffering = false,
                            errorMessage = "Playback unavailable"
                        )
                    }
                    ZubeenMediaPlaybackService.updatePlayback(
                        context = getApplication(),
                        song = song,
                        isPlaying = false,
                        mode = PlaybackMode.NORMAL,
                        elapsedSeconds = 0
                    )
                    return@launch
                }

                audioEngine.playSongStream(
                    streamUrl = streamUrl,
                    initialSeekPositionMs = 0,
                    mode = "NORMAL",
                    contentId = song.id,
                    onPrepared = { realDurationMs ->
                        _uiState.update { it.copy(isNormalBuffering = false) }
                        val trueDurationSecs = if (realDurationMs > 0) (realDurationMs / 1000) else song.durationSeconds
                        val updatedSong = song.copy(durationSeconds = trueDurationSecs)
                        _uiState.update { it.copy(normalCurrentSong = updatedSong) }
                        startNormalProgressTicker(updatedSong)
                        preloadNextNormalSong()
                    },
                    onCompleted = {
                        Log.d("ZubeenPlayback", "Normal song completed. Evaluating repeat and shuffle logic...")
                        onNormalTrackCompleted()
                    },
                    onError = { err ->
                        Log.w("ZubeenBufferDebug", "PLAYER_ERROR mode=NORMAL id=${song.id} err=$err")
                        attemptSeamlessNormalRecovery(song)
                    }
                )
            } catch (t: Throwable) {
                Log.e("ZubeenPlayback", "Error in selectAndPlayNormalSong: ${t.message}", t)
                _uiState.update { it.copy(isNormalBuffering = false, errorMessage = "Playback error") }
            }
        }
    }

    private fun attemptSeamlessNormalRecovery(song: Song) {
        val currentPos = audioEngine.getCurrentPositionMs()
        Log.i("ZubeenBufferDebug", "SOURCE_SWITCH (normal recovery) timestamp=${System.currentTimeMillis()} mode=NORMAL id=${song.id} posMs=$currentPos")

        if (song.playbackSources.isNotEmpty()) {
            val ytm = NormalCatalogueRepository.providerManager.discoveryEngine.providers.filterIsInstance<YouTubeMusicProvider>().firstOrNull()
            ytm?.invalidateStream(song.playbackSources.first().sourceId)
        }

        viewModelScope.launch {
            try {
                val freshStreamUrl = NormalCatalogueRepository.providerManager.resolveStreamUrl(song) ?: song.streamUrl
                if (!freshStreamUrl.isNullOrBlank()) {
                    audioEngine.playSongStream(
                        streamUrl = freshStreamUrl,
                        initialSeekPositionMs = currentPos,
                        mode = "NORMAL",
                        contentId = song.id,
                        onPrepared = { realDurationMs ->
                            _uiState.update { it.copy(isNormalBuffering = false) }
                            val trueDurationSecs = if (realDurationMs > 0) (realDurationMs / 1000) else song.durationSeconds
                            val updatedSong = song.copy(durationSeconds = trueDurationSecs)
                            _uiState.update { it.copy(normalCurrentSong = updatedSong) }
                            startNormalProgressTicker(updatedSong)
                        },
                        onCompleted = {
                            onNormalTrackCompleted()
                        },
                        onError = { err ->
                            _uiState.update { it.copy(isNormalBuffering = false, errorMessage = "Playback error: $err") }
                            ZubeenMediaPlaybackService.updatePlayback(
                                context = getApplication(),
                                song = song,
                                isPlaying = false,
                                mode = PlaybackMode.NORMAL,
                                elapsedSeconds = 0
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isNormalBuffering = false, errorMessage = "Playback error") }
            }
        }
    }

    private fun preloadNextNormalSong() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val nextSong = NormalCatalogueRepository.getNextSong(
                    _uiState.value.normalCurrentSong,
                    _uiState.value.isShuffleEnabled,
                    _uiState.value.repeatMode
                )
                if (nextSong.playbackSources.isNotEmpty()) {
                    val ytm = NormalCatalogueRepository.providerManager.discoveryEngine.providers.filterIsInstance<YouTubeMusicProvider>().firstOrNull()
                    ytm?.preloadStreamUrl(nextSong.playbackSources.first())
                }
            } catch (e: Exception) {
                // Ignore preloading exceptions
            }
        }
    }

    fun playAlbum(album: Album, shuffle: Boolean = false) {
        if (album.songs.isEmpty()) return
        NormalCatalogueRepository.queueManager.updatePlaylist(album.songs, null)
        NormalCatalogueRepository.queueManager.setShuffleEnabled(shuffle, null)
        val startSong = if (shuffle) {
            NormalCatalogueRepository.queueManager.getNextSong(null) ?: album.songs.first()
        } else {
            album.songs.first()
        }
        _uiState.update { it.copy(isShuffleEnabled = shuffle) }
        selectAndPlayNormalSong(startSong)
        setNormalModeFullPlayerVisible(true)
    }

    fun playArtist(artist: Artist) {
        if (artist.allSongs.isEmpty()) return
        NormalCatalogueRepository.queueManager.updatePlaylist(artist.allSongs, null)
        val startSong = artist.allSongs.first()
        selectAndPlayNormalSong(startSong)
        setNormalModeFullPlayerVisible(true)
    }

    fun togglePlayPauseNormal() {
        val currentlyPlaying = _uiState.value.isNormalPlaying && _uiState.value.activePlaybackMode == PlaybackMode.NORMAL
        Log.d("ZubeenPlayback", "togglePlayPauseNormal: currentlyPlaying=$currentlyPlaying")

        if (currentlyPlaying) {
            _uiState.update { it.copy(isNormalPlaying = false) }
            normalProgressJob?.cancel()
            normalProgressJob = null
            audioEngine.pauseAudio()
            ZubeenMediaPlaybackService.updatePlayback(
                context = getApplication(),
                song = _uiState.value.normalCurrentSong,
                isPlaying = false,
                mode = PlaybackMode.NORMAL,
                elapsedSeconds = _uiState.value.normalElapsedSeconds
            )
        } else {
            _uiState.update { it.copy(activePlaybackMode = PlaybackMode.NORMAL, isNormalPlaying = true, isRadioPlaying = false) }
            ZubeenMediaPlaybackService.updatePlayback(
                context = getApplication(),
                song = _uiState.value.normalCurrentSong,
                isPlaying = true,
                mode = PlaybackMode.NORMAL,
                elapsedSeconds = _uiState.value.normalElapsedSeconds
            )
            if (audioEngine.isPrepared()) {
                audioEngine.resumeAudio()
                startNormalProgressTicker(_uiState.value.normalCurrentSong)
            } else {
                selectAndPlayNormalSong(_uiState.value.normalCurrentSong)
            }
        }
    }

    fun playNextSong() {
        val next = NormalCatalogueRepository.getNextSong(
            _uiState.value.normalCurrentSong,
            _uiState.value.isShuffleEnabled,
            _uiState.value.repeatMode
        )
        selectAndPlayNormalSong(next)
    }

    fun playPreviousSong() {
        if (_uiState.value.normalElapsedSeconds > 3) {
            seekToPosition(0)
            return
        }
        val prev = NormalCatalogueRepository.getPreviousSong(_uiState.value.normalCurrentSong)
        selectAndPlayNormalSong(prev)
    }

    fun seekToPosition(seconds: Int) {
        val song = _uiState.value.normalCurrentSong
        val totalSecs = song.durationSeconds.coerceAtLeast(1)
        val clamped = seconds.coerceIn(0, totalSecs)
        val prog = (clamped.toFloat() / totalSecs.toFloat()).coerceIn(0f, 1f)

        Log.d("ZubeenPlayback", "[Seek] Normal seek requested to ${clamped}s / ${totalSecs}s (prog=$prog)")
        audioEngine.seekToPosition(clamped * 1000)

        _uiState.update {
            it.copy(
                normalElapsedSeconds = clamped,
                normalSongProgress = prog
            )
        }

        ZubeenMediaPlaybackService.updatePlayback(
            context = getApplication(),
            song = song,
            isPlaying = _uiState.value.isNormalPlaying,
            mode = PlaybackMode.NORMAL,
            elapsedSeconds = clamped
        )
    }

    fun seekTo(seconds: Int) = seekToPosition(seconds)

    fun toggleShuffle() {
        val newShuffle = !_uiState.value.isShuffleEnabled
        NormalCatalogueRepository.queueManager.setShuffleEnabled(newShuffle, _uiState.value.normalCurrentSong)
        _uiState.update { it.copy(isShuffleEnabled = newShuffle) }
        Log.d("ZubeenPlayback", "Normal Mode Shuffle toggled -> $newShuffle")
    }

    fun toggleRepeat() {
        val newMode = when (_uiState.value.repeatMode) {
            NormalRepeatMode.OFF -> NormalRepeatMode.ALL
            NormalRepeatMode.ALL -> NormalRepeatMode.ONE
            NormalRepeatMode.ONE -> NormalRepeatMode.OFF
        }
        NormalCatalogueRepository.queueManager.setRepeatMode(newMode)
        _uiState.update {
            it.copy(
                repeatMode = newMode,
                isRepeatEnabled = (newMode != NormalRepeatMode.OFF)
            )
        }
        Log.d("ZubeenPlayback", "Normal Mode Repeat toggled -> $newMode")
    }

    fun toggleFavorite(songId: String) {
        NormalCatalogueRepository.toggleFavorite(songId)
    }

    fun setNormalModeFullPlayerVisible(visible: Boolean) {
        _uiState.update { it.copy(isNormalModeFullPlayerVisible = visible) }
    }

    private fun startNormalProgressTicker(song: Song) {
        normalProgressJob?.cancel()
        normalProgressJob = viewModelScope.launch {
            val totalSeconds = song.durationSeconds.coerceAtLeast(1)
            var elapsed = _uiState.value.normalElapsedSeconds

            while (isActive && _uiState.value.isNormalPlaying) {
                delay(1000L)
                val enginePosMs = audioEngine.getCurrentPositionMs()
                elapsed = if (enginePosMs > 0) (enginePosMs / 1000) else elapsed + 1
                val prog = (elapsed.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
                _uiState.update {
                    it.copy(
                        normalElapsedSeconds = elapsed,
                        normalSongProgress = prog
                    )
                }
            }
        }
    }

    private fun onNormalTrackCompleted() {
        val currentSong = _uiState.value.normalCurrentSong
        val repeatMode = _uiState.value.repeatMode
        val isShuffle = _uiState.value.isShuffleEnabled

        if (repeatMode == NormalRepeatMode.ONE) {
            Log.d("ZubeenPlayback", "Track completed in Repeat ONE mode. Replaying [${currentSong.titleEnglish}]...")
            selectAndPlayNormalSong(currentSong)
            return
        }

        val nextSong = NormalCatalogueRepository.getNextSongOrNull(currentSong)
        if (nextSong != null) {
            Log.d("ZubeenPlayback", "Track completed. Advancing to next: [${nextSong.titleEnglish}] (Shuffle=$isShuffle, Repeat=$repeatMode)...")
            selectAndPlayNormalSong(nextSong)
        } else {
            Log.d("ZubeenPlayback", "End of queue reached in Repeat OFF mode. Stopping playback.")
            _uiState.update {
                it.copy(
                    isNormalPlaying = false,
                    normalElapsedSeconds = 0,
                    normalSongProgress = 0f
                )
            }
            normalProgressJob?.cancel()
            normalProgressJob = null
            audioEngine.seekToPosition(0)
            audioEngine.pauseAudio()
            ZubeenMediaPlaybackService.updatePlayback(
                context = getApplication(),
                song = currentSong,
                isPlaying = false,
                mode = PlaybackMode.NORMAL,
                elapsedSeconds = 0
            )
        }
    }

    fun retryPlayback() {
        if (_uiState.value.activePlaybackMode == PlaybackMode.RADIO) {
            radioPlaybackFailureAttempts = 0
            syncAndPlayRadioBroadcast()
        } else {
            selectAndPlayNormalSong(_uiState.value.normalCurrentSong)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ZubeenMediaPlaybackService.setEventListener(null)
        radioProgressJob?.cancel()
        normalProgressJob?.cancel()
        audioEngine.release()
        ZubeenMediaPlaybackService.stopPlaybackService(getApplication())
    }
}
