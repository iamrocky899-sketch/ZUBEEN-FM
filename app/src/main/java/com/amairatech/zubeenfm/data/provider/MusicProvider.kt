package com.amairatech.zubeenfm.data.provider

import com.amairatech.zubeenfm.data.model.Song

/**
 * Interface defining a Music Provider for ZUBEEN FM.
 * Declares separate discovery pipelines for the broad Assamese Music Library (Normal Mode)
 * and the dedicated 100% Zubeen Garg Radio Catalogue (Radio Mode), plus shared stream resolution.
 */
interface MusicProvider {
    val providerId: String
    val providerName: String
    val canSearch: Boolean
    val canProvideMetadata: Boolean
    val canProvideArtwork: Boolean
    val canProvidePlayback: Boolean
    val supportsPagination: Boolean

    /**
     * Discovers wide Assamese music (All artists, 1980s -> current, Bihu, Folk, Modern, Film).
     */
    suspend fun discoverAssameseMusic(page: Int, pageSize: Int = 20, query: String? = null): ProviderPageResult

    /**
     * Discovers strictly 100% Zubeen Garg recordings for Radio Mode.
     */
    suspend fun discoverZubeenMusic(page: Int, pageSize: Int = 20): ProviderPageResult

    /**
     * Discovers the entire discoverable Assamese music catalogue from this provider in one pass.
     */
    suspend fun discoverCompleteAssameseMusic(): List<Song>

    /**
     * Searches Assamese songs across the provider.
     */
    suspend fun searchSongs(query: String, page: Int = 1, pageSize: Int = 20): ProviderPageResult

    /**
     * Discovers genuinely recent Assamese recordings.
     */
    suspend fun discoverNewAssameseReleases(page: Int, pageSize: Int = 20): ProviderPageResult

    /**
     * Resolves a real playable audio stream URL for a playback source.
     */
    suspend fun resolveStreamUrl(source: PlaybackSource): String? = source.streamUrl
}

data class ProviderPageResult(
    val songs: List<Song>,
    val page: Int,
    val hasMorePages: Boolean,
    val totalCount: Int
)
