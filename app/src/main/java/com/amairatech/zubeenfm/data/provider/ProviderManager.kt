package com.amairatech.zubeenfm.data.provider

import android.util.Log
import com.amairatech.zubeenfm.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

enum class SyncState {
    IDLE,
    RUNNING,
    COMPLETED,
    FAILED
}

/**
 * Provider Manager orchestrating song discovery across all configured music providers
 * for both Normal Mode (Assamese Music Library) and Radio Mode (Zubeen Garg Catalogue).
 */
class ProviderManager(
    val discoveryEngine: ZubeenDiscoveryEngine = ZubeenDiscoveryEngine()
) {
    private val mutex = Mutex()

    // Normal Mode Pagination & State
    private var normalCurrentPage = 1
    private var normalIsEndReached = false
    private var normalIsCurrentlyLoading = false
    private var lastNormalSyncTimestamp = 0L
    
    private val _normalSyncState = MutableStateFlow(SyncState.IDLE)
    val normalSyncState: StateFlow<SyncState> = _normalSyncState.asStateFlow()

    private val _normalCatalogue = MutableStateFlow<List<Song>>(emptyList())
    val normalCatalogue: StateFlow<List<Song>> = _normalCatalogue.asStateFlow()

    private val _isNormalLoading = MutableStateFlow(false)
    val isNormalLoading: StateFlow<Boolean> = _isNormalLoading.asStateFlow()

    private val _normalLoadingStatus = MutableStateFlow("")
    val normalLoadingStatus: StateFlow<String> = _normalLoadingStatus.asStateFlow()

    private val _newReleases = MutableStateFlow<List<Song>>(emptyList())
    val newReleases: StateFlow<List<Song>> = _newReleases.asStateFlow()

    private val _isNewReleasesLoading = MutableStateFlow(false)
    val isNewReleasesLoading: StateFlow<Boolean> = _isNewReleasesLoading.asStateFlow()

    // Radio Mode Pagination & State
    private var radioCurrentPage = 1
    private var radioIsEndReached = false
    private var radioIsCurrentlyLoading = false

    private val _radioCatalogue = MutableStateFlow<List<Song>>(emptyList())
    val radioCatalogue: StateFlow<List<Song>> = _radioCatalogue.asStateFlow()

    private val _isRadioLoading = MutableStateFlow(false)
    val isRadioLoading: StateFlow<Boolean> = _isRadioLoading.asStateFlow()

    // Legacy alias for compatibility
    val unifiedCatalogue: StateFlow<List<Song>> = _normalCatalogue
    val isLoading: StateFlow<Boolean> = _isNormalLoading

    suspend fun loadCompleteNormalCatalogue(isRefresh: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        mutex.withLock {
            if (normalIsCurrentlyLoading) {
                Log.d("ZubeenPlayback", "Catalogue already loading. Waiting for completion...")
                return@withContext false
            }
            // Avoid redundant background syncs if recently updated (within 30 mins)
            if (normalIsEndReached && !isRefresh && (now - lastNormalSyncTimestamp < 30 * 60 * 1000L)) {
                Log.d("ZubeenPlayback", "Complete catalogue recently updated. Skipping.")
                return@withContext true
            }
            normalIsCurrentlyLoading = true
            _isNormalLoading.value = true
            _normalSyncState.value = SyncState.RUNNING
        }

        try {
            Log.d("ZubeenPlayback", "Initiating incremental catalogue discovery...")
            _normalLoadingStatus.value = "Discovering new songs..."
            
            val discoveredList = discoveryEngine.discoverCompleteAssameseCatalogue()
            
            mutex.withLock {
                val currentList = _normalCatalogue.value
                val merged = currentList + discoveredList
                val resolved = DuplicateResolver.deduplicateSongs(merged)
                
                val newSongsCount = resolved.size - currentList.size
                
                _normalCatalogue.value = resolved
                normalIsEndReached = true
                lastNormalSyncTimestamp = System.currentTimeMillis()
                _normalSyncState.value = SyncState.COMPLETED
                
                _normalLoadingStatus.value = if (newSongsCount > 0) {
                    "$newSongsCount new songs added to your library"
                } else {
                    "Your library is up to date"
                }
                
                Log.i("ZubeenPlayback", "Incremental Catalogue Sync Complete: ${resolved.size} songs total ($newSongsCount new discovered).")
            }
            
            // Also refresh new releases in background
            loadNewReleases()
            
            return@withContext true
        } catch (e: Exception) {
            Log.e("ZubeenPlayback", "Catalogue Sync Failed: ${e.message}", e)
            _normalSyncState.value = SyncState.FAILED
            _normalLoadingStatus.value = "Sync failed: ${e.message}"
            return@withContext false
        } finally {
            mutex.withLock {
                normalIsCurrentlyLoading = false
                _isNormalLoading.value = false
            }
        }
    }

    suspend fun loadNewReleases(): Boolean = withContext(Dispatchers.IO) {
        _isNewReleasesLoading.value = true
        try {
            val result = discoveryEngine.discoverNewAssameseReleases(page = 1, pageSize = 20)
            _newReleases.value = result.songs
            return@withContext true
        } catch (e: Exception) {
            return@withContext false
        } finally {
            _isNewReleasesLoading.value = false
        }
    }

    /**
     * Legacy pagination method - now redirected to complete load for safety.
     */
    suspend fun loadNextNormalPage(query: String? = null): Boolean {
        if (!query.isNullOrBlank()) {
            // Search still uses its own logic
            return performNormalSearch(query)
        }
        return loadCompleteNormalCatalogue()
    }

    private suspend fun performNormalSearch(query: String): Boolean = withContext(Dispatchers.IO) {
        // Search should not replace the main catalogue if we want "load once" persistence,
        // but it can update a search-specific flow. For now, keep it simple.
        val searchResults = discoveryEngine.searchAssameseSongs(query)
        // Note: The user said search should operate on COMPLETE loaded catalogue.
        // So we shouldn't necessarily update _normalCatalogue here.
        return@withContext true
    }

    /**
     * Loads the next page of 100% Zubeen songs for Radio Mode.
     */
    suspend fun loadNextRadioPage(): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (radioIsCurrentlyLoading || radioIsEndReached) {
                return@withContext false
            }
            radioIsCurrentlyLoading = true
            _isRadioLoading.value = true
        }

        try {
            val pageResult = discoveryEngine.discoverZubeenCatalogue(page = radioCurrentPage, pageSize = 12)

            mutex.withLock {
                val currentList = _radioCatalogue.value
                val combined = currentList + pageResult.songs
                val resolved = DuplicateResolver.deduplicateSongs(combined)
                _radioCatalogue.value = resolved

                radioCurrentPage++
                radioIsEndReached = !pageResult.hasMorePages || pageResult.songs.isEmpty()
            }
            return@withContext true
        } catch (e: Exception) {
            return@withContext false
        } finally {
            mutex.withLock {
                radioIsCurrentlyLoading = false
                _isRadioLoading.value = false
            }
        }
    }

    /**
     * Searches Normal Mode Assamese songs.
     */
    suspend fun searchNormalSongs(
        query: String,
        selectedGenre: String = "All Genres",
        selectedLanguage: String = "All Languages",
        selectedAlbum: String = "All Albums"
    ): List<Song> = withContext(Dispatchers.IO) {
        val trimmed = query.trim().lowercase()
        val allCurrent = _normalCatalogue.value

        allCurrent.filter { song ->
            val matchesGenre = selectedGenre == "All Genres" || song.genreAssamese == selectedGenre
            val matchesLanguage = selectedLanguage == "All Languages" || song.languageAssamese == selectedLanguage
            val matchesAlbum = selectedAlbum == "All Albums" || song.albumAssamese == selectedAlbum
            val matchesQuery = trimmed.isEmpty() ||
                song.titleAssamese.lowercase().contains(trimmed) ||
                song.titleEnglish.lowercase().contains(trimmed) ||
                song.albumAssamese.lowercase().contains(trimmed) ||
                song.albumEnglish.lowercase().contains(trimmed) ||
                song.artistAssamese.lowercase().contains(trimmed) ||
                song.artistEnglish.lowercase().contains(trimmed) ||
                song.languageAssamese.lowercase().contains(trimmed) ||
                song.genreAssamese.lowercase().contains(trimmed)
            matchesGenre && matchesLanguage && matchesAlbum && matchesQuery
        }
    }

    /**
     * Resolves a playable stream URL with provider fallback.
     */
    suspend fun resolveStreamUrl(song: Song): String? {
        return discoveryEngine.resolvePlayableStreamWithFallback(song)
    }

    fun hasMoreNormalPages(): Boolean = !normalIsEndReached
    fun hasMoreRadioPages(): Boolean = !radioIsEndReached

    // Compatibility aliases
    suspend fun initializeCatalogue() {
        loadNextNormalPage()
    }
    suspend fun loadNextPage(): Boolean = loadNextNormalPage()
    suspend fun searchSongs(query: String, selectedGenre: String, selectedLanguage: String, selectedAlbum: String) =
        searchNormalSongs(query, selectedGenre, selectedLanguage, selectedAlbum)
}
