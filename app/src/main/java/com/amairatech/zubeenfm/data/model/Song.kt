package com.amairatech.zubeenfm.data.model

import com.amairatech.zubeenfm.data.provider.PlaybackSource

/**
 * Represents a musical track by Zubeen Garg on ZUBEEN FM.
 * Display titles and album names preserve authentic original Assamese/Hindi orthography.
 */
data class Song(
    val id: String,
    val titleAssamese: String,
    val titleEnglish: String,
    val albumAssamese: String = "অসমীয়া এলবাম",
    val albumEnglish: String = "Assamese Album",
    val artistAssamese: String = "জুবিন গাৰ্গ",
    val artistEnglish: String = "Zubeen Garg",
    val durationSeconds: Int = 180,
    val genreAssamese: String = "আধুনিক সুৰীয়া",
    val languageAssamese: String = "অসমীয়া",
    val originalLanguage: String = "UNKNOWN",
    val releaseYear: String = "2001",
    val accentColorHex: Long = 0xFFE65100,
    val streamUrl: String? = null,
    val artworkUrl: String? = null,
    val isPlayable: Boolean = true,
    val playbackSources: List<PlaybackSource> = emptyList(),
    val contentType: ContentType = ContentType.SONG,
    val releaseTimestamp: Long = 0L
) {
    val isAssamese: Boolean
        get() = originalLanguage.equals("ASSAMESE", ignoreCase = true)

    val isZubeenGarg: Boolean
        get() {
            val artEng = artistEnglish.lowercase()
            val artAss = artistAssamese
            return artEng.contains("zubeen") || artAss.contains("জুবিন")
        }
}
