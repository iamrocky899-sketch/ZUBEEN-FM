package com.amairatech.zubeenfm.data.provider

import android.content.Context
import android.provider.MediaStore
import com.amairatech.zubeenfm.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Local Device Audio Provider for ZUBEEN FM.
 * Discovers and streams audio tracks stored locally on the user's device.
 */
class LocalAudioProvider(
    private val context: Context? = null
) : MusicProvider {

    override val providerId: String = "local_audio_provider"
    override val providerName: String = "Local Device Storage"
    override val canSearch: Boolean = true
    override val canProvideMetadata: Boolean = true
    override val canProvideArtwork: Boolean = false
    override val canProvidePlayback: Boolean = true
    override val supportsPagination: Boolean = false

    override suspend fun discoverAssameseMusic(page: Int, pageSize: Int, query: String?): ProviderPageResult = withContext(Dispatchers.IO) {
        val all = queryLocalAudio(isZubeenOnly = false)
        ProviderPageResult(all, 1, false, all.size)
    }

    override suspend fun discoverNewAssameseReleases(page: Int, pageSize: Int): ProviderPageResult = 
        ProviderPageResult(emptyList(), page, false, 0)

    override suspend fun discoverZubeenMusic(page: Int, pageSize: Int): ProviderPageResult = withContext(Dispatchers.IO) {
        val all = queryLocalAudio(isZubeenOnly = true)
        ProviderPageResult(all, 1, false, all.size)
    }

    override suspend fun discoverCompleteAssameseMusic(): List<Song> = withContext(Dispatchers.IO) {
        queryLocalAudio(isZubeenOnly = false)
    }

    private fun queryLocalAudio(isZubeenOnly: Boolean): List<Song> {
        if (context == null) return emptyList()

        val localSongs = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (it.moveToNext()) {
                    val rawTitle = it.getString(titleCol) ?: ""
                    val rawArtist = it.getString(artistCol) ?: ""
                    val rawAlbum = it.getString(albumCol) ?: "Local Album"
                    val durationMs = it.getInt(durCol)
                    val filePath = it.getString(dataCol) ?: ""
                    val trackId = it.getLong(idCol)

                    val isValid = if (isZubeenOnly) {
                        ZubeenArtistFilter.isValidZubeenRecording(rawTitle, rawArtist)
                    } else {
                        AssameseMusicFilter.isValidAssameseRecording(rawTitle, rawArtist, rawAlbum)
                    }

                    if (isValid) {
                        val cleanedTitle = MetadataNormalizer.cleanTitle(rawTitle)
                        val language = MetadataNormalizer.detectLanguage(cleanedTitle, rawAlbum)
                        val genre = MetadataNormalizer.inferGenre(cleanedTitle, rawAlbum, language)

                        localSongs.add(
                            Song(
                                id = "local_$trackId",
                                titleAssamese = cleanedTitle,
                                titleEnglish = cleanedTitle,
                                albumAssamese = rawAlbum,
                                albumEnglish = rawAlbum,
                                artistAssamese = rawArtist.ifBlank { "অসমীয়া শিল্পী" },
                                artistEnglish = rawArtist.ifBlank { "Assamese Artist" },
                                durationSeconds = (durationMs / 1000).coerceAtLeast(30),
                                genreAssamese = genre,
                                languageAssamese = language,
                                releaseYear = "Local",
                                accentColorHex = 0xFF455A64,
                                streamUrl = filePath,
                                isPlayable = true,
                                playbackSources = listOf(
                                    PlaybackSource(
                                        providerId = "local_audio_provider",
                                        sourceId = "local_$trackId",
                                        streamUrl = filePath,
                                        isDirectStream = true,
                                        qualityLabel = "Local Lossless / HQ"
                                    )
                                )
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Permission or MediaStore access fallback
        }
        return localSongs
    }

    override suspend fun searchSongs(query: String, page: Int, pageSize: Int): ProviderPageResult {
        val all = queryLocalAudio(isZubeenOnly = false)
        val filtered = all.filter {
            it.titleEnglish.contains(query, ignoreCase = true) || it.albumEnglish.contains(query, ignoreCase = true)
        }
        return ProviderPageResult(filtered, page, false, filtered.size)
    }
}
