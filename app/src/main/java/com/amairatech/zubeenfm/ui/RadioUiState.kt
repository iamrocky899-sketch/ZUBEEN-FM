package com.amairatech.zubeenfm.ui

import com.amairatech.zubeenfm.data.model.Song

enum class PlaybackMode {
    RADIO,
    NORMAL
}

enum class NormalRepeatMode {
    OFF,
    ALL,
    ONE
}

enum class RadioPlaybackState {
    IDLE,
    READY,
    PLAYING,
    PAUSED,
    BUFFERING,
    ERROR
}

/**
 * State representing ZUBEEN FM application.
 * Fully isolates Radio Mode (Synchronized Zubeen Broadcast) from Normal Mode (Assamese Music Player).
 */
data class RadioUiState(
    // Active playback mode
    val activePlaybackMode: PlaybackMode = PlaybackMode.RADIO,

    // 1. Radio Mode Independent State (100% Zubeen Garg Synchronized Station)
    val radioCurrentSong: Song,
    val isRadioPlaying: Boolean = false,
    val isRadioBuffering: Boolean = false,
    val radioPlaybackState: RadioPlaybackState = RadioPlaybackState.IDLE,
    val userRequestedRadioPlayback: Boolean = false,
    val radioSongProgress: Float = 0f,
    val radioElapsedSeconds: Int = 0,
    val isRadioUnavailable: Boolean = false,
    val stationFrequency: String = "98.6 FM",
    val stationNameAssamese: String = "ZUBEEN FM",
    val stationTaglineAssamese: String = "অসমৰ প্ৰাণৰ সুৰ • অল-টাইম জুবিন গাৰ্গ",
    val broadcastLocationAssamese: String = "Guwahati, Assam",

    // 2. Normal Mode Independent State (All Assamese Music Library)
    val normalCurrentSong: Song,
    val isNormalPlaying: Boolean = false,
    val isNormalBuffering: Boolean = false,
    val normalSongProgress: Float = 0f,
    val normalElapsedSeconds: Int = 0,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: NormalRepeatMode = NormalRepeatMode.OFF,
    val isRepeatEnabled: Boolean = false,
    val isNormalModeFullPlayerVisible: Boolean = false,
    val favoriteSongIds: Set<String> = emptySet(),
    val recentlyPlayedSongs: List<Song> = emptyList(),

    // Shared visualizer & error status
    val waveformAmplitudes: List<Float> = emptyList(),
    val errorMessage: String? = null
) {
    // Backwards-compatible convenience getters for UI components
    val currentSong: Song
        get() = if (activePlaybackMode == PlaybackMode.RADIO) radioCurrentSong else normalCurrentSong

    val isPlaying: Boolean
        get() = if (activePlaybackMode == PlaybackMode.RADIO) isRadioPlaying else isNormalPlaying

    val isBuffering: Boolean
        get() = if (activePlaybackMode == PlaybackMode.RADIO) isRadioBuffering else isNormalBuffering

    val songProgress: Float
        get() = if (activePlaybackMode == PlaybackMode.RADIO) radioSongProgress else normalSongProgress

    val elapsedSongSeconds: Int
        get() = if (activePlaybackMode == PlaybackMode.RADIO) radioElapsedSeconds else normalElapsedSeconds
}
