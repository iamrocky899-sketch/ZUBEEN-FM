package com.amairatech.zubeenfm.data.repository

import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.PlaybackSource
import com.amairatech.zubeenfm.data.provider.ProviderManager
import com.amairatech.zubeenfm.radio.RadioStationClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Repository for Radio Mode — ZUBEEN FM 100% Zubeen Garg Catalogue.
 * Strictly verifies that all songs are genuine Zubeen Garg recordings (ZubeenArtistFilter),
 * managed via RadioStationClock for universal synchronized broadcast,
 * with independent radio history and radio queue.
 */
object ZubeenRadioCatalogueRepository {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    val providerManager = ProviderManager()

    private val _radioSongsFlow = MutableStateFlow<List<Song>>(emptyList())
    val radioSongsFlow: StateFlow<List<Song>> = _radioSongsFlow.asStateFlow()

    private val _radioHistoryFlow = MutableStateFlow<List<Song>>(emptyList())
    val radioHistoryFlow: StateFlow<List<Song>> = _radioHistoryFlow.asStateFlow()

    private val initialZubeenSeedSongs: List<Song> = listOf(
        Song(
            id = "yt_6QW7CHoLpos",
            titleAssamese = "মায়াবিনী",
            titleEnglish = "Mayabini",
            albumAssamese = "মায়া",
            albumEnglish = "Maya",
            artistAssamese = "জুবিন গাৰ্গ",
            artistEnglish = "Zubeen Garg",
            durationSeconds = 322,
            genreAssamese = "আধুনিক সুৰীয়া",
            languageAssamese = "অসমীয়া",
            originalLanguage = "ASSAMESE",
            releaseYear = "2001",
            accentColorHex = 0xFFD84315,
            streamUrl = null,
            isPlayable = true,
            playbackSources = listOf(
                PlaybackSource(
                    providerId = "youtube_music_provider",
                    sourceId = "6QW7CHoLpos",
                    streamUrl = null,
                    qualityLabel = "YouTube Music High"
                )
            )
        ),
        Song(
            id = "yt__3BIc99Tbcw",
            titleAssamese = "চুইট লাভ",
            titleEnglish = "Sweet Love",
            albumAssamese = "চুইট লাভ",
            albumEnglish = "Sweet Love",
            artistAssamese = "জুবিন গাৰ্গ",
            artistEnglish = "Zubeen Garg",
            durationSeconds = 295,
            genreAssamese = "ৰ'মাণ্টিক সুৰ",
            languageAssamese = "অসমীয়া",
            originalLanguage = "ASSAMESE",
            releaseYear = "2007",
            accentColorHex = 0xFFC2185B,
            streamUrl = null,
            isPlayable = true,
            playbackSources = listOf(
                PlaybackSource(
                    providerId = "youtube_music_provider",
                    sourceId = "_3BIc99Tbcw",
                    streamUrl = null,
                    qualityLabel = "YouTube Music High"
                )
            )
        ),
        Song(
            id = "yt_E8nFRftKow8",
            titleAssamese = "কঁকাল খামুচীয়া",
            titleEnglish = "Kokal Khamusia",
            albumAssamese = "জুবিনৰ বিহু",
            albumEnglish = "Zubeen Bihu Hits",
            artistAssamese = "জুবিন গাৰ্গ",
            artistEnglish = "Zubeen Garg",
            durationSeconds = 278,
            genreAssamese = "বিহু আৰু লোকসংগীত",
            languageAssamese = "অসমীয়া",
            originalLanguage = "ASSAMESE",
            releaseYear = "2010",
            accentColorHex = 0xFFF57C00,
            streamUrl = null,
            isPlayable = true,
            playbackSources = listOf(
                PlaybackSource(
                    providerId = "youtube_music_provider",
                    sourceId = "E8nFRftKow8",
                    streamUrl = null,
                    qualityLabel = "YouTube Music High"
                )
            )
        )
    )

    private val manifestSongs: List<Song>
        get() = com.amairatech.zubeenfm.radio.RadioStationManifest.getEligibleRadioSongs()

    val playlist: List<Song>
        get() {
            val dynamicList = _radioSongsFlow.value.filter {
                com.amairatech.zubeenfm.radio.RadioStationManifest.isEligibleSongForRadio(it)
            }
            val combined = manifestSongs + dynamicList
            return com.amairatech.zubeenfm.data.provider.DuplicateResolver.deduplicateSongs(combined)
                .ifEmpty { manifestSongs }
        }

    init {
        scope.launch {
            providerManager.radioCatalogue.collect { list ->
                val verifiedOnly = list.filter {
                    com.amairatech.zubeenfm.radio.RadioStationManifest.isEligibleSongForRadio(it)
                }
                if (verifiedOnly.isNotEmpty()) {
                    _radioSongsFlow.value = verifiedOnly
                }
            }
        }
        scope.launch {
            providerManager.loadNextRadioPage()
        }
    }

    /**
     * Gets the currently scheduled universal radio station broadcast slot.
     */
    fun getCurrentStationBroadcastSlot(timestampMs: Long = System.currentTimeMillis()): RadioStationClock.StationBroadcastSlot {
        val currentCatalogue = playlist
        return RadioStationClock.calculateCurrentBroadcastSlot(currentCatalogue, timestampMs)
            ?: RadioStationClock.StationBroadcastSlot(
                song = currentCatalogue.first(),
                sequenceIndex = 0,
                intraSongOffsetMs = 0L,
                songDurationMs = (currentCatalogue.first().durationSeconds * 1000L),
                cycleTotalDurationMs = 300000L,
                stationTimeMs = timestampMs
            )
    }

    fun recordRadioHistory(song: Song) {
        _radioHistoryFlow.update { current ->
            val updated = current.filter { it.id != song.id }.toMutableList()
            updated.add(0, song)
            if (updated.size > 20) updated.take(20) else updated
        }
    }

    fun loadMoreRadioSongs() {
        scope.launch {
            providerManager.loadNextRadioPage()
        }
    }
}
