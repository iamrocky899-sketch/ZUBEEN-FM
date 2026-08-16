package com.amairatech.zubeenfm.data.provider

import com.amairatech.zubeenfm.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

/**
 * Discovery Engine for ZUBEEN FM.
 * Orchestrates multi-provider discovery for:
 * 1. Normal Mode: Wide Assamese Music Library (AssameseMusicFilter).
 * 2. Radio Mode: Strictly 100% Zubeen Garg Catalogue (ZubeenArtistFilter).
 * 3. Shared Stream Resolution.
 */
class ZubeenDiscoveryEngine(
    val providers: List<MusicProvider> = listOf(
        YouTubeMusicProvider(),
        ZubeenDiscographyProvider(),
        SoundCloudProvider()
    )
) {

    /**
     * Discovers the entire Assamese catalogue across all providers once.
     */
    suspend fun discoverCompleteAssameseCatalogue(): List<Song> = withContext(Dispatchers.IO) {
        val deferredResults = providers.map { provider ->
            async {
                try {
                    provider.discoverCompleteAssameseMusic()
                } catch (e: Exception) {
                    emptyList<Song>()
                }
            }
        }

        val allResults = deferredResults.awaitAll().flatten()
        
        // Normalize metadata & detect original language
        val normalizedSongs = allResults.map { song ->
            val cleanedTitle = MetadataNormalizer.cleanTitle(song.titleEnglish)
            val origLang = MetadataNormalizer.detectOriginalLanguage(cleanedTitle, song.albumEnglish, song.artistEnglish, explicitLanguage = song.originalLanguage)
            val langLabel = MetadataNormalizer.getLanguageAssameseLabel(origLang)
            val genre = MetadataNormalizer.inferGenre(cleanedTitle, song.albumEnglish, langLabel)
            song.copy(
                titleEnglish = cleanedTitle,
                originalLanguage = origLang,
                languageAssamese = langLabel,
                genreAssamese = if (song.genreAssamese == "আধুনিক সুৰীয়া") genre else song.genreAssamese
            )
        }

        // Filter for genuine Assamese recordings
        val verifiedSongs = normalizedSongs.filter { song ->
            AssameseMusicFilter.isValidForNormalCatalogue(song)
        }

        return@withContext DuplicateResolver.deduplicateSongs(verifiedSongs)
    }

    /**
     * Executes discovery across providers for Normal Mode (Assamese Music Library).
     */
    suspend fun discoverAssameseCatalogue(page: Int, pageSize: Int = 12, query: String? = null): ProviderPageResult = withContext(Dispatchers.IO) {
        val deferredResults = providers.map { provider ->
            async {
                try {
                    provider.discoverAssameseMusic(page = page, pageSize = pageSize, query = query)
                } catch (e: Exception) {
                    ProviderPageResult(emptyList(), page, false, 0)
                }
            }
        }

        val allPageResults = deferredResults.awaitAll()
        val rawCandidateSongs = allPageResults.flatMap { it.songs }
        val hasMorePages = allPageResults.any { it.hasMorePages }

        // Normalize metadata & detect original language
        val normalizedSongs = rawCandidateSongs.map { song ->
            val cleanedTitle = MetadataNormalizer.cleanTitle(song.titleEnglish)
            val origLang = MetadataNormalizer.detectOriginalLanguage(cleanedTitle, song.albumEnglish, song.artistEnglish, explicitLanguage = song.originalLanguage)
            val langLabel = MetadataNormalizer.getLanguageAssameseLabel(origLang)
            val genre = MetadataNormalizer.inferGenre(cleanedTitle, song.albumEnglish, langLabel)
            song.copy(
                titleEnglish = cleanedTitle,
                originalLanguage = origLang,
                languageAssamese = langLabel,
                genreAssamese = if (song.genreAssamese == "আধুনিক সুৰীয়া") genre else song.genreAssamese
            )
        }

        // Filter for genuine Assamese recordings (Assamese language ONLY for general catalogue)
        val verifiedSongs = normalizedSongs.filter { song ->
            AssameseMusicFilter.isValidForNormalCatalogue(song)
        }

        val deduplicatedCatalogue = DuplicateResolver.deduplicateSongs(verifiedSongs)

        ProviderPageResult(
            songs = deduplicatedCatalogue,
            page = page,
            hasMorePages = hasMorePages,
            totalCount = deduplicatedCatalogue.size
        )
    }

    /**
     * Executes discovery for genuinely recent Assamese releases.
     */
    suspend fun discoverNewAssameseReleases(page: Int, pageSize: Int = 12): ProviderPageResult = withContext(Dispatchers.IO) {
        val deferredResults = providers.map { provider ->
            async {
                try {
                    provider.discoverNewAssameseReleases(page = page, pageSize = pageSize)
                } catch (e: Exception) {
                    ProviderPageResult(emptyList(), page, false, 0)
                }
            }
        }

        val allPageResults = deferredResults.awaitAll()
        val rawCandidateSongs = allPageResults.flatMap { it.songs }
        val hasMorePages = allPageResults.any { it.hasMorePages }

        val verifiedSongs = rawCandidateSongs.filter { song ->
            AssameseMusicFilter.isValidForNormalCatalogue(song)
        }

        val sorted = verifiedSongs.sortedByDescending { it.releaseTimestamp }
        val deduplicated = DuplicateResolver.deduplicateSongs(sorted)

        ProviderPageResult(
            songs = deduplicated,
            page = page,
            hasMorePages = hasMorePages,
            totalCount = deduplicated.size
        )
    }

    /**
     * Executes discovery across providers for Radio Mode (Strictly Zubeen Garg Catalogue).
     */
    suspend fun discoverZubeenCatalogue(page: Int, pageSize: Int = 12): ProviderPageResult = withContext(Dispatchers.IO) {
        val deferredResults = providers.map { provider ->
            async {
                try {
                    provider.discoverZubeenMusic(page = page, pageSize = pageSize)
                } catch (e: Exception) {
                    ProviderPageResult(emptyList(), page, false, 0)
                }
            }
        }

        val allPageResults = deferredResults.awaitAll()
        val rawCandidateSongs = allPageResults.flatMap { it.songs }
        val hasMorePages = allPageResults.any { it.hasMorePages }

        // Strict ZubeenArtistFilter Pass (Enforces Zubeen identity AND Original Assamese Audio rule for spoken content)
        val verifiedSongs = rawCandidateSongs.filter { song ->
            ZubeenArtistFilter.isValidForRadio(song)
        }

        // Normalize metadata
        val normalizedSongs = verifiedSongs.map { song ->
            val cleanedTitle = MetadataNormalizer.cleanTitle(song.titleEnglish)
            val lang = MetadataNormalizer.detectLanguage(cleanedTitle, song.albumEnglish)
            val genre = MetadataNormalizer.inferGenre(cleanedTitle, song.albumEnglish, lang)
            song.copy(
                titleEnglish = cleanedTitle,
                languageAssamese = if (song.languageAssamese == "অসমীয়া") lang else song.languageAssamese,
                genreAssamese = if (song.genreAssamese == "আধুনিক সুৰীয়া") genre else song.genreAssamese
            )
        }

        val deduplicatedCatalogue = DuplicateResolver.deduplicateSongs(normalizedSongs)

        ProviderPageResult(
            songs = deduplicatedCatalogue,
            page = page,
            hasMorePages = hasMorePages,
            totalCount = deduplicatedCatalogue.size
        )
    }

    /**
     * Performs a global search across all providers for Assamese songs.
     */
    suspend fun searchAssameseSongs(query: String, page: Int = 1, pageSize: Int = 20): List<Song> = withContext(Dispatchers.IO) {
        val deferred = providers.map { provider ->
            async {
                try {
                    provider.searchSongs(query = query, page = page, pageSize = pageSize).songs
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }

        val rawResults = deferred.awaitAll().flatten()
        val verified = rawResults.filter { song ->
            AssameseMusicFilter.isValidForNormalCatalogue(song) || ZubeenArtistFilter.isValidForRadio(song)
        }
        DuplicateResolver.deduplicateSongs(verified)
    }

    /**
     * Resolves the primary playable stream URL for a given song with provider fallback.
     */
    suspend fun resolvePlayableStreamWithFallback(song: Song): String? = withContext(Dispatchers.IO) {
        if (song.playbackSources.isEmpty()) {
            return@withContext song.streamUrl
        }

        for (source in song.playbackSources) {
            val provider = providers.find { it.providerId == source.providerId }
            val stream = provider?.resolveStreamUrl(source) ?: source.streamUrl
            if (!stream.isNullOrBlank()) {
                return@withContext stream
            }
        }

        return@withContext song.streamUrl
    }
}
