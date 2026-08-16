package com.amairatech.zubeenfm.radio

import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.PlaybackSource

/**
 * Supported Content Types for 24x7 ZUBEEN FM Station.
 */
enum class RadioContentType {
    ZUBEEN_SONG,
    ZUBEEN_PODCAST,
    ZUBEEN_EPISODE,
    ZUBEEN_STORY,
    ZUBEEN_INTERVIEW,
    ZUBEEN_MEMORIAL
}

/**
 * Represents an individual 24x7 Radio Content item on ZUBEEN FM.
 * Includes music (all original languages by Zubeen) and Assamese spoken content.
 */
data class RadioContent(
    val id: String,
    val title: String,
    val artistOrHost: String = "জুবিন গাৰ্গ",
    val contentType: RadioContentType = RadioContentType.ZUBEEN_SONG,
    val originalLanguage: String = "ASSAMESE",
    val durationSeconds: Int = 240,
    val playbackSource: PlaybackSource? = null,
    val streamUrl: String? = null,
    val verified: Boolean = true,
    val eligibleForRadio: Boolean = true,
    val albumOrSeries: String = "ZUBEEN FM Broadcast",
    val accentColorHex: Long = 0xFFE65100
) {
    /**
     * Converts this RadioContent item to a Song representation for player compatibility.
     */
    fun toSong(): Song {
        val genreLabel = when (contentType) {
            RadioContentType.ZUBEEN_SONG -> "আধুনিক সুৰীয়া"
            RadioContentType.ZUBEEN_PODCAST -> "পডকাষ্ট • Zubeen Podcast"
            RadioContentType.ZUBEEN_EPISODE -> "বিশেষ খণ্ড • Episode"
            RadioContentType.ZUBEEN_STORY -> "কাহিনী • Story"
            RadioContentType.ZUBEEN_INTERVIEW -> "সাক্ষাৎকাৰ • Interview"
            RadioContentType.ZUBEEN_MEMORIAL -> "স্মৃতিচাৰণ • Memorial"
        }

        val langLabel = when (originalLanguage.uppercase()) {
            "HINDI" -> "হিন্দী"
            "BENGALI" -> "বাংলা"
            "ASSAMESE" -> "অসমীয়া"
            else -> "অসমীয়া"
        }

        return Song(
            id = id,
            titleAssamese = title,
            titleEnglish = title,
            albumAssamese = albumOrSeries,
            albumEnglish = albumOrSeries,
            artistAssamese = artistOrHost,
            artistEnglish = if (contentType == RadioContentType.ZUBEEN_SONG) "Zubeen Garg" else artistOrHost,
            durationSeconds = durationSeconds,
            genreAssamese = genreLabel,
            languageAssamese = langLabel,
            originalLanguage = originalLanguage,
            releaseYear = "2024",
            accentColorHex = accentColorHex,
            streamUrl = streamUrl,
            isPlayable = true,
            playbackSources = if (playbackSource != null) listOf(playbackSource) else emptyList()
        )
    }
}
