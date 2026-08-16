package com.amairatech.zubeenfm.data.provider

import com.amairatech.zubeenfm.data.model.Song

/**
 * Resolves duplicate song entries across multiple provider discovery strategies,
 * merging multiple legitimate playback sources into a single unified Song catalog item.
 */
object DuplicateResolver {

    fun generateFingerprint(song: Song): String {
        val titleNorm = song.titleEnglish.lowercase().replace(Regex("[^a-z0-9]"), "")
        val albumNorm = song.albumEnglish.lowercase().replace(Regex("[^a-z0-9]"), "")
        val langNorm = song.originalLanguage.lowercase()

        val version = when {
            titleNorm.contains("live") || titleNorm.contains("performance") || titleNorm.contains("concert") || titleNorm.contains("stage") -> "live"
            titleNorm.contains("acoustic") || titleNorm.contains("unplugged") || titleNorm.contains("guitar") -> "acoustic"
            titleNorm.contains("remix") || titleNorm.contains("mix") || titleNorm.contains("reprise") || titleNorm.contains("lofi") -> "remix"
            else -> "studio"
        }

        return if (albumNorm.isNotEmpty() && albumNorm != "single") {
            "$titleNorm::$albumNorm::$langNorm::$version"
        } else {
            "$titleNorm::$langNorm::$version"
        }
    }

    /**
     * Deduplicates a list of songs while preserving multiple playback sources.
     */
    fun deduplicateSongs(songs: List<Song>): List<Song> {
        val map = linkedMapOf<String, Song>()

        for (song in songs) {
            val key = generateFingerprint(song)
            val existing = map[key]

            if (existing == null) {
                map[key] = song
            } else {
                // Merge playback sources
                val combinedSources = (existing.playbackSources + song.playbackSources).distinctBy { it.providerId + it.streamUrl }
                val primaryStream = existing.streamUrl ?: song.streamUrl ?: combinedSources.firstOrNull()?.streamUrl
                val isPlayable = primaryStream != null || combinedSources.any { it.streamUrl != null }

                map[key] = existing.copy(
                    streamUrl = primaryStream,
                    isPlayable = isPlayable,
                    playbackSources = combinedSources
                )
            }
        }

        return map.values.toList()
    }
}
