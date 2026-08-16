package com.amairatech.zubeenfm.data.model

/**
 * Represents the strict content type classification of a track.
 */
enum class ContentType {
    SONG,
    ZUBEEN_PODCAST,
    ZUBEEN_EPISODE,
    ZUBEEN_STORY,
    ZUBEEN_INTERVIEW,
    ZUBEEN_MEMORIAL,
    UNKNOWN
}

/**
 * Represents an Album in the Assamese Music Library.
 */
data class Album(
    val id: String,
    val nameAssamese: String,
    val nameEnglish: String,
    val artistAssamese: String,
    val artistEnglish: String,
    val artworkUrl: String? = null,
    val releaseYear: String = "Online",
    val songs: List<Song> = emptyList()
)

/**
 * Represents an Artist in the Assamese Music Library.
 */
data class Artist(
    val id: String,
    val nameAssamese: String,
    val nameEnglish: String,
    val artworkUrl: String? = null,
    val popularSongs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val allSongs: List<Song> = emptyList()
) {
    val isZubeenGarg: Boolean
        get() = nameEnglish.lowercase().contains("zubeen") || nameAssamese.contains("জুবিন")
}
