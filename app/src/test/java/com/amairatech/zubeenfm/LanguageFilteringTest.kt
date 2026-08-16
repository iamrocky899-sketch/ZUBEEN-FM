package com.amairatech.zubeenfm

import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.AssameseMusicFilter
import com.amairatech.zubeenfm.data.provider.MetadataNormalizer
import com.amairatech.zubeenfm.data.provider.ZubeenArtistFilter
import com.amairatech.zubeenfm.data.repository.NormalCatalogueRepository
import com.amairatech.zubeenfm.data.repository.ZubeenRadioCatalogueRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageFilteringTest {

    // Test Case 1: Assamese song by Papon → ACCEPT in normal catalogue
    @Test
    fun testCase1_AssameseSongByPapon_AcceptedInNormalCatalogue() {
        val song = Song(
            id = "papon_assamese_01",
            titleAssamese = "মণিহাৰা",
            titleEnglish = "Monihara",
            artistAssamese = "পাপন",
            artistEnglish = "Papon",
            albumEnglish = "Papon Special",
            languageAssamese = "অসমীয়া",
            originalLanguage = "ASSAMESE"
        )
        assertTrue(
            "Assamese song by Papon must be accepted in normal catalogue",
            AssameseMusicFilter.isValidForNormalCatalogue(song)
        )
    }

    // Test Case 2: Hindi song by Papon → REJECT from normal catalogue
    @Test
    fun testCase2_HindiSongByPapon_RejectedFromNormalCatalogue() {
        val song = Song(
            id = "papon_hindi_01",
            titleAssamese = "মোহ মোহ কে ধাগে",
            titleEnglish = "Moh Moh Ke Dhaage",
            artistAssamese = "পাপন",
            artistEnglish = "Papon",
            albumEnglish = "Dum Laga Ke Haisha (Bollywood)",
            languageAssamese = "হিন্দী",
            originalLanguage = "HINDI"
        )
        assertFalse(
            "Hindi song by Papon must be rejected from general Assamese normal catalogue",
            AssameseMusicFilter.isValidForNormalCatalogue(song)
        )
    }

    // Test Case 3: Bengali song by another Assamese artist → REJECT from normal catalogue
    @Test
    fun testCase3_BengaliSongByAssameseArtist_RejectedFromNormalCatalogue() {
        val song = Song(
            id = "khagen_bengali_01",
            titleAssamese = "বাংলা গীতি",
            titleEnglish = "Bengali Folksong",
            artistAssamese = "খগেন মহন্ত",
            artistEnglish = "Khagen Mahanta",
            albumEnglish = "Bengali Collection",
            languageAssamese = "বাংলা",
            originalLanguage = "BENGALI"
        )
        assertFalse(
            "Bengali song by non-Zubeen Assamese artist must be rejected from normal catalogue",
            AssameseMusicFilter.isValidForNormalCatalogue(song)
        )
    }

    // Test Case 4: Assamese Zubeen song → ACCEPT in normal catalogue
    @Test
    fun testCase4_AssameseZubeenSong_AcceptedInNormalCatalogue() {
        val song = Song(
            id = "zubeen_assamese_01",
            titleAssamese = "মায়াবিনী",
            titleEnglish = "Mayabini",
            artistAssamese = "জুবিন গাৰ্গ",
            artistEnglish = "Zubeen Garg",
            albumEnglish = "Maya",
            languageAssamese = "অসমীয়া",
            originalLanguage = "ASSAMESE"
        )
        assertTrue(
            "Assamese Zubeen song must be accepted in normal catalogue",
            AssameseMusicFilter.isValidForNormalCatalogue(song)
        )
    }

    // Test Case 5: Hindi Zubeen song → REJECT from general Assamese catalogue
    @Test
    fun testCase5_HindiZubeenSong_RejectedFromGeneralAssameseCatalogue() {
        val song = Song(
            id = "zubeen_hindi_01",
            titleAssamese = "য়া আলী",
            titleEnglish = "Ya Ali",
            artistAssamese = "জুবিন গাৰ্গ",
            artistEnglish = "Zubeen Garg",
            albumEnglish = "Gangster Bollywood",
            languageAssamese = "হিন্দী",
            originalLanguage = "HINDI"
        )
        assertFalse(
            "Hindi Zubeen song must be rejected from general Assamese catalogue",
            AssameseMusicFilter.isValidForNormalCatalogue(song)
        )
    }

    // Test Case 6: Hindi Zubeen song → ACCEPT in Zubeen artist details if verified
    @Test
    fun testCase6_HindiZubeenSong_AcceptedInZubeenArtistDetails() {
        val song = Song(
            id = "zubeen_hindi_01",
            titleAssamese = "য়া আলী",
            titleEnglish = "Ya Ali",
            artistAssamese = "জুবিন গাৰ্গ",
            artistEnglish = "Zubeen Garg",
            albumEnglish = "Gangster Bollywood",
            languageAssamese = "হিন্দী",
            originalLanguage = "HINDI"
        )
        assertTrue(
            "Song must be verified Zubeen recording",
            ZubeenArtistFilter.isValidZubeenRecording(song.titleEnglish, song.artistEnglish)
        )
        val zubeenCatalogue = NormalCatalogueRepository.getZubeenAllLanguageCatalogue()
        assertTrue(
            "Zubeen special all-language catalogue must include verified Hindi songs like Ya Ali",
            zubeenCatalogue.any { it.titleEnglish.contains("Ya Ali", ignoreCase = true) || it.originalLanguage == "HINDI" }
        )
    }

    // Test Case 7: Bengali Zubeen song → ACCEPT in Zubeen artist details if verified
    @Test
    fun testCase7_BengaliZubeenSong_AcceptedInZubeenArtistDetails() {
        val zubeenCatalogue = NormalCatalogueRepository.getZubeenAllLanguageCatalogue()
        assertTrue(
            "Zubeen special all-language catalogue must include verified Bengali songs",
            zubeenCatalogue.any { it.originalLanguage == "BENGALI" || it.titleEnglish.contains("Mon Mane Na", ignoreCase = true) }
        )
    }

    // Test Case 8: Unknown-language non-Zubeen song → REJECT from normal catalogue
    @Test
    fun testCase8_UnknownLanguageNonZubeenSong_RejectedFromNormalCatalogue() {
        val song = Song(
            id = "unknown_song_01",
            titleAssamese = "Unknown Track",
            titleEnglish = "Unknown Instrument Track 123",
            artistAssamese = "অন্য শিল্পী",
            artistEnglish = "Random Artist",
            albumEnglish = "Random Album",
            languageAssamese = "অজ্ঞাত",
            originalLanguage = "UNKNOWN"
        )
        assertFalse(
            "Unknown-language non-Zubeen song must be rejected from normal catalogue",
            AssameseMusicFilter.isValidForNormalCatalogue(song)
        )
    }

    // Test Case 9: Unknown-language Zubeen song → do not falsely label as Assamese
    @Test
    fun testCase9_UnknownLanguageZubeenSong_NotFalselyLabelledAssamese() {
        val detected = MetadataNormalizer.detectOriginalLanguage(
            title = "Zubeen Experimental Track 456",
            album = "Studio Session",
            artist = "Zubeen Garg"
        )
        assertEquals("UNKNOWN", detected)
        val label = MetadataNormalizer.getLanguageAssameseLabel(detected)
        assertEquals("অজ্ঞাত", label)
    }

    // Test Case 10: Radio catalogue remains Zubeen-only
    @Test
    fun testCase10_RadioCatalogueRemainsZubeenOnly() {
        val radioSongs = ZubeenRadioCatalogueRepository.playlist
        for (song in radioSongs) {
            assertTrue(
                "Every song in Radio playlist must be a verified Zubeen recording: ${song.titleEnglish}",
                ZubeenArtistFilter.isValidZubeenRecording(song.titleEnglish, song.artistEnglish)
            )
        }
    }
}
