package com.amairatech.zubeenfm.radio

import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.PlaybackSource
import com.amairatech.zubeenfm.data.provider.ZubeenArtistFilter

/**
 * 24x7 Master Broadcast Manifest and Eligibility Verifier for ZUBEEN FM.
 * Rules:
 * 1. Music: 100% Verified Zubeen Garg recordings in ALL original languages (Assamese, Hindi, Bengali, etc.).
 * 2. Spoken Content: Assamese podcasts, episodes, stories, interviews about Zubeen ONLY in original Assamese audio.
 * 3. NO OTHER MUSIC ARTISTS allowed in Radio Mode (Papon, Khagen Mahanta, Bhupen Hazarika = strictly 0).
 * 4. NO translated/dubbed audio.
 */
object RadioStationManifest {

    val masterLineup: List<RadioContent> = listOf(
        // === ICONIC ZUBEEN MUSIC (ASSAMESE) ===
        RadioContent(
            id = "rad_zg_mayabini",
            title = "মায়াবিনী (Mayabini)",
            artistOrHost = "জুবিন গাৰ্গ",
            contentType = RadioContentType.ZUBEEN_SONG,
            originalLanguage = "ASSAMESE",
            durationSeconds = 322,
            albumOrSeries = "মায়া (Maya)",
            playbackSource = PlaybackSource("youtube_music_provider", "6QW7CHoLpos", null, "High Quality Audio")
        ),
        RadioContent(
            id = "rad_zg_sweetlove",
            title = "চুইট লাভ (Sweet Love)",
            artistOrHost = "জুবিন গাৰ্গ",
            contentType = RadioContentType.ZUBEEN_SONG,
            originalLanguage = "ASSAMESE",
            durationSeconds = 295,
            albumOrSeries = "চুইট লাভ (Sweet Love)",
            playbackSource = PlaybackSource("youtube_music_provider", "_3BIc99Tbcw", null, "High Quality Audio")
        ),

        // === ASSAMESE ZUBEEN SPOKEN CONTENT (EPISODE) ===
        RadioContent(
            id = "rad_spk_ep01",
            title = "শৈশৱ আৰু সংগীতৰ আৰম্ভণি",
            artistOrHost = "জুবিন গাৰ্গৰ বিশেষ খণ্ড",
            contentType = RadioContentType.ZUBEEN_EPISODE,
            originalLanguage = "ASSAMESE",
            durationSeconds = 180,
            albumOrSeries = "জুবিন দাৰ জীৱনগাথা",
            playbackSource = PlaybackSource("youtube_music_provider", "r8e063fL-T8", null, "High Quality Audio")
        ),

        // === VERIFIED ZUBEEN MUSIC (HINDI) ===
        RadioContent(
            id = "rad_zg_yaali",
            title = "য়া আলী (Ya Ali - Gangster)",
            artistOrHost = "জুবিন গাৰ্গ",
            contentType = RadioContentType.ZUBEEN_SONG,
            originalLanguage = "HINDI",
            durationSeconds = 290,
            albumOrSeries = "গেংষ্টাৰ (Gangster Bollywood)",
            playbackSource = PlaybackSource("youtube_music_provider", "P8Z-6_988t8", null, "High Quality Audio")
        ),

        // === ASSAMESE ZUBEEN PODCAST ===
        RadioContent(
            id = "rad_spk_pod01",
            title = "‘অনামিকা’ আৰু সংগীত জগতত প্ৰৱেশ",
            artistOrHost = "জুবিন এফ এম পডকাষ্ট",
            contentType = RadioContentType.ZUBEEN_PODCAST,
            originalLanguage = "ASSAMESE",
            durationSeconds = 210,
            albumOrSeries = "চিৰসেউজ জুবিন পডকাষ্ট",
            playbackSource = PlaybackSource("youtube_music_provider", "XG_fE6Fv9wE", null, "High Quality Audio")
        ),

        // === VERIFIED ZUBEEN MUSIC (BENGALI) ===
        RadioContent(
            id = "rad_zg_monmanena",
            title = "মন মানে না (Mon Mane Na)",
            artistOrHost = "জুবিন গাৰ্গ",
            contentType = RadioContentType.ZUBEEN_SONG,
            originalLanguage = "BENGALI",
            durationSeconds = 280,
            albumOrSeries = "মন মানে না (Mon Mane Na Bengali)",
            playbackSource = PlaybackSource("youtube_music_provider", "vO6vP7Yv1w4", null, "High Quality Audio")
        ),

        // === ASSAMESE ZUBEEN STORY ===
        RadioContent(
            id = "rad_spk_story01",
            title = "সমাজ আৰু মানুহৰ প্ৰতি জুবিনৰ হৃদয়",
            artistOrHost = "জুবিন গাৰ্গৰ কাহিনী",
            contentType = RadioContentType.ZUBEEN_STORY,
            originalLanguage = "ASSAMESE",
            durationSeconds = 195,
            albumOrSeries = "মানৱদৰদী জুবিন",
            playbackSource = PlaybackSource("youtube_music_provider", "E8nFRftKow8", null, "High Quality Audio")
        ),

        // === ICONIC ZUBEEN BIHU MUSIC (ASSAMESE) ===
        RadioContent(
            id = "rad_zg_kokalkhamusia",
            title = "কঁকাল খামুচীয়া (Kokal Khamusia)",
            artistOrHost = "জুবিন গাৰ্গ",
            contentType = RadioContentType.ZUBEEN_SONG,
            originalLanguage = "ASSAMESE",
            durationSeconds = 278,
            albumOrSeries = "জুবিনৰ বিহু (Zubeen Bihu Hits)",
            playbackSource = PlaybackSource("youtube_music_provider", "L8u9eE9m_n4", null, "High Quality Audio")
        ),

        // === ASSAMESE ZUBEEN INTERVIEW ===
        RadioContent(
            id = "rad_spk_interview01",
            title = "নতুন প্ৰজন্ম আৰু অসমীয়া সংগীতৰ ভৱিষ্যত",
            artistOrHost = "জুবিন দাৰ সৈতে সাক্ষাৎকাৰ",
            contentType = RadioContentType.ZUBEEN_INTERVIEW,
            originalLanguage = "ASSAMESE",
            durationSeconds = 240,
            albumOrSeries = "অসমৰ প্ৰাণৰ কণ্ঠ",
            playbackSource = PlaybackSource("youtube_music_provider", "oZ-k8J-OQnI", null, "High Quality Audio")
        ),

        // === ASSAMESE ZUBEEN MEMORIAL EPISODE ===
        RadioContent(
            id = "rad_spk_memorial01",
            title = "বহুমুখী প্ৰতিভা: গায়ক, সুৰকাৰ আৰু কবি জুবিন",
            artistOrHost = "শ্ৰদ্ধাঞ্জলি বিশেষ অনুষ্ঠান",
            contentType = RadioContentType.ZUBEEN_MEMORIAL,
            originalLanguage = "ASSAMESE",
            durationSeconds = 215,
            albumOrSeries = "হৃদয়ৰ সুৰ জুবিন",
            playbackSource = PlaybackSource("youtube_music_provider", "jZ7m9T-uL8A", null, "High Quality Audio")
        )
    )

    /**
     * Strict Radio Eligibility Validator:
     * - Music: MUST be a verified Zubeen Garg recording.
     * - Spoken: MUST be Assamese language about Zubeen Garg.
     */
    fun isEligibleForRadio(content: RadioContent): Boolean {
        if (!content.verified || !content.eligibleForRadio) return false

        return when (content.contentType) {
            RadioContentType.ZUBEEN_SONG -> {
                ZubeenArtistFilter.isValidZubeenRecording(content.title, content.artistOrHost)
            }
            RadioContentType.ZUBEEN_PODCAST,
            RadioContentType.ZUBEEN_EPISODE,
            RadioContentType.ZUBEEN_STORY,
            RadioContentType.ZUBEEN_INTERVIEW,
            RadioContentType.ZUBEEN_MEMORIAL -> {
                content.originalLanguage.equals("ASSAMESE", ignoreCase = true)
            }
        }
    }

    /**
     * Helper for Song model eligibility.
     */
    fun isEligibleSongForRadio(song: Song): Boolean {
        // Music by other artists is strictly rejected
        return ZubeenArtistFilter.isValidZubeenRecording(song.titleEnglish, song.artistEnglish) ||
               song.artistAssamese.contains("জুবিন")
    }

    /**
     * Converts the entire master lineup to Song list for player consumption.
     */
    fun getEligibleRadioSongs(): List<Song> {
        return masterLineup
            .filter { isEligibleForRadio(it) }
            .map { it.toSong() }
    }
}
