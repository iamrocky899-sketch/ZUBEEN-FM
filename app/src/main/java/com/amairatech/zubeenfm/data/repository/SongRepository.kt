package com.amairatech.zubeenfm.data.repository

import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.ProviderManager
import kotlinx.coroutines.flow.StateFlow

/**
 * Unified Repository Facade coordinating:
 * - Normal Mode -> NormalCatalogueRepository (Wide Assamese Music Library 1980s -> current)
 * - Radio Mode -> ZubeenRadioCatalogueRepository (100% Zubeen Garg Synchronized Radio)
 */
object SongRepository {

    val providerManager: ProviderManager
        get() = NormalCatalogueRepository.providerManager

    val songsFlow: StateFlow<List<Song>>
        get() = NormalCatalogueRepository.songsFlow

    val playlist: List<Song>
        get() = NormalCatalogueRepository.playlist

    val radioPlaylist: List<Song>
        get() = ZubeenRadioCatalogueRepository.playlist

    val genres: List<String>
        get() = NormalCatalogueRepository.genres

    val languages: List<String>
        get() = NormalCatalogueRepository.languages

    val albums: List<String>
        get() = NormalCatalogueRepository.albums

    val artists: List<String>
        get() = NormalCatalogueRepository.artists

    val recentlyPlayedFlow: StateFlow<List<Song>>
        get() = NormalCatalogueRepository.recentlyPlayedFlow

    val favoritesFlow: StateFlow<Set<String>>
        get() = NormalCatalogueRepository.favoritesFlow

    fun loadMoreSongs() {
        NormalCatalogueRepository.loadMoreSongs()
    }

    fun loadMoreRadioSongs() {
        ZubeenRadioCatalogueRepository.loadMoreRadioSongs()
    }

    fun getSongByIndex(index: Int): Song {
        val list = playlist
        return list[index % list.size]
    }

    fun getNextSong(currentSong: Song?): Song {
        return NormalCatalogueRepository.getNextSong(currentSong, isShuffle = false)
    }

    fun getPreviousSong(currentSong: Song?): Song {
        return NormalCatalogueRepository.getPreviousSong(currentSong)
    }

    fun getSongById(id: String): Song? {
        return playlist.find { it.id == id } ?: radioPlaylist.find { it.id == id }
    }

    fun searchSongs(
        query: String,
        selectedGenre: String = "All Genres",
        selectedLanguage: String = "All Languages",
        selectedAlbum: String = "All Albums"
    ): List<Song> {
        return NormalCatalogueRepository.searchSongs(query, selectedGenre, selectedLanguage, selectedAlbum)
    }

    fun recordNormalRecentlyPlayed(song: Song) {
        NormalCatalogueRepository.recordRecentlyPlayed(song)
    }

    fun toggleFavorite(songId: String) {
        NormalCatalogueRepository.toggleFavorite(songId)
    }
}
