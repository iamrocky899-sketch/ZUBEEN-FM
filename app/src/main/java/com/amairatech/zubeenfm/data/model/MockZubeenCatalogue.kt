package com.amairatech.zubeenfm.data.model

/**
 * Mock catalogue strictly for unit testing and offline development fallback.
 */
object MockZubeenCatalogue {

    val sampleTracks: List<Song> = listOf(
        Song(
            id = "mock_1",
            titleAssamese = "মায়াবিনী",
            titleEnglish = "Mayabini",
            albumAssamese = "মায়া",
            albumEnglish = "Maya",
            artistAssamese = "জুবিন গাৰ্গ",
            artistEnglish = "Zubeen Garg",
            durationSeconds = 195,
            genreAssamese = "আধুনিক সুৰীয়া",
            languageAssamese = "অসমীয়া",
            releaseYear = "2001",
            accentColorHex = 0xFFD84315,
            streamUrl = "https://ia800905.us.archive.org/19/items/free-music-archive-acoustic-folk/sample_track_01.mp3",
            isPlayable = true
        ),
        Song(
            id = "mock_2",
            titleAssamese = "অনামীকা",
            titleEnglish = "Anamika",
            albumAssamese = "অনামীকা",
            albumEnglish = "Anamika",
            artistAssamese = "জুবিন গাৰ্গ",
            artistEnglish = "Zubeen Garg",
            durationSeconds = 210,
            genreAssamese = "চিৰসেউজ মেল'ডী",
            languageAssamese = "অসমীয়া",
            releaseYear = "1992",
            accentColorHex = 0xFFC2185B,
            streamUrl = "https://ia800905.us.archive.org/19/items/free-music-archive-acoustic-folk/sample_track_02.mp3",
            isPlayable = true
        )
    )
}
