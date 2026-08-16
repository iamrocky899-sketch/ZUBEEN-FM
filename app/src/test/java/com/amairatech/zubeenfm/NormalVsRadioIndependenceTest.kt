package com.amairatech.zubeenfm

import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.AssameseMusicFilter
import com.amairatech.zubeenfm.data.provider.PlaybackSource
import com.amairatech.zubeenfm.data.provider.YouTubeMusicProvider
import com.amairatech.zubeenfm.data.provider.ZubeenArtistFilter
import com.amairatech.zubeenfm.data.repository.NormalCatalogueRepository
import com.amairatech.zubeenfm.data.repository.ZubeenRadioCatalogueRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NormalVsRadioIndependenceTest {

    @Test
    fun testAssameseMusicFilterAcceptsMultipleArtistsAndRejectsNonAssamese() {
        // Diverse Assamese Artists & Eras -> PASS
        assertTrue(AssameseMusicFilter.isValidAssameseRecording("Manuhhe Manuhor Babe", "Bhupen Hazarika", "Classic"))
        assertTrue(AssameseMusicFilter.isValidAssameseRecording("Bihure Botorot", "Khagen Mahanta", "Bihu Hits"))
        assertTrue(AssameseMusicFilter.isValidAssameseRecording("Monihara", "Papon", "Papon Special"))
        assertTrue(AssameseMusicFilter.isValidAssameseRecording("Sopunor Moromi", "Deeplina Deka", "Single"))
        assertTrue(AssameseMusicFilter.isValidAssameseRecording("Kokal Khamusia", "Zubeen Garg", "Bihu Hits"))
        assertTrue(AssameseMusicFilter.isValidAssameseRecording("Buku Bhora Morom", "Jayanta Hazarika", "Gold 1980s"))

        // Unrelated Bengali/Punjabi/Bhojpuri/Covers -> REJECT
        assertFalse(AssameseMusicFilter.isValidAssameseRecording("Kolkata Bangla Hit", "Bengali Artist", "Bengali Album"))
        assertFalse(AssameseMusicFilter.isValidAssameseRecording("Bhojpuri Dhamaal", "Bhojpuri Singer", "Bhojpuri Album"))
        assertFalse(AssameseMusicFilter.isValidAssameseRecording("Punjabi Beat", "Punjabi Artist", "Punjabi Pop"))
        assertFalse(AssameseMusicFilter.isValidAssameseRecording("Assamese Song Reaction by Vlogger", "Vlogger XYZ", "Vlog"))
    }

    @Test
    fun testZubeenArtistFilterRejectsNonZubeenArtists() {
        // Zubeen Garg -> PASS
        assertTrue(ZubeenArtistFilter.isValidZubeenRecording("Mayabini", "Zubeen Garg"))
        assertTrue(ZubeenArtistFilter.isValidZubeenRecording("মায়াবিনী", "জুবিন গাৰ্গ"))

        // Other Assamese Artists -> NOT accepted for Radio Mode
        assertFalse(ZubeenArtistFilter.isValidZubeenRecording("Manuhhe Manuhor Babe", "Bhupen Hazarika"))
        assertFalse(ZubeenArtistFilter.isValidZubeenRecording("Monihara", "Papon"))
        assertFalse(ZubeenArtistFilter.isValidZubeenRecording("Bihure Botorot", "Khagen Mahanta"))
    }

    @Test
    fun testNormalModeDoesNotContaminateRadioQueueOrHistory() {
        val normalSongA = Song(id = "normA", titleAssamese = "গীত এ", titleEnglish = "Song A", artistEnglish = "Bhupen Hazarika", artistAssamese = "ভূপেন হাজৰিকা")
        val normalSongB = Song(id = "normB", titleAssamese = "গীত বি", titleEnglish = "Song B", artistEnglish = "Papon", artistAssamese = "পাপন")

        // 1. Record recently played in Normal Mode
        val radioHistoryBefore = ZubeenRadioCatalogueRepository.radioHistoryFlow.value
        NormalCatalogueRepository.recordRecentlyPlayed(normalSongA)
        NormalCatalogueRepository.recordRecentlyPlayed(normalSongB)

        // 2. Normal Mode Recently Played has songs
        val normalRecents = NormalCatalogueRepository.recentlyPlayedFlow.value
        assertTrue("Normal mode must contain played songs", normalRecents.any { it.id == "normA" })
        assertTrue("Normal mode must contain played songs", normalRecents.any { it.id == "normB" })

        // 3. Radio Mode history MUST NOT be contaminated by Normal Mode
        val radioHistoryAfter = ZubeenRadioCatalogueRepository.radioHistoryFlow.value
        assertEquals("Radio history must be completely untouched by Normal Mode", radioHistoryBefore, radioHistoryAfter)
        assertFalse("Radio history must NOT contain normal song A", radioHistoryAfter.any { it.id == "normA" })
        assertFalse("Radio history must NOT contain normal song B", radioHistoryAfter.any { it.id == "normB" })
    }

    @Test
    fun testRadioModeDoesNotContaminateNormalRecentlyPlayed() {
        val radioSong = Song(id = "radZ", titleAssamese = "জুবিন গীত", titleEnglish = "Zubeen Song Z", artistEnglish = "Zubeen Garg", artistAssamese = "জুবিন গাৰ্গ")

        val normalRecentsBefore = NormalCatalogueRepository.recentlyPlayedFlow.value
        ZubeenRadioCatalogueRepository.recordRadioHistory(radioSong)

        val normalRecentsAfter = NormalCatalogueRepository.recentlyPlayedFlow.value
        assertEquals("Normal recently played must NOT be altered by Radio Mode", normalRecentsBefore, normalRecentsAfter)
        assertFalse("Normal recently played must NOT contain radio song", normalRecentsAfter.any { it.id == "radZ" })
    }

    @Test
    fun testRealMultiProviderDiscoverySeparation() = runBlocking {
        val provider = YouTubeMusicProvider()

        // 1. Normal Mode Discovery (Assamese library)
        val assameseResult = provider.discoverAssameseMusic(page = 1, pageSize = 8)
        assertTrue("Assamese music discovery must return songs", assameseResult.songs.isNotEmpty())
        println("Discovered Assamese songs: ${assameseResult.songs.size}")

        // 2. Radio Mode Discovery (Strictly Zubeen)
        val zubeenResult = provider.discoverZubeenMusic(page = 1, pageSize = 8)
        assertTrue("Zubeen radio discovery must return songs", zubeenResult.songs.isNotEmpty())
        println("Discovered Zubeen radio songs: ${zubeenResult.songs.size}")

        // Every song in zubeenResult must be 100% Zubeen Garg
        for (song in zubeenResult.songs) {
            assertTrue(
                "Radio song ${song.titleEnglish} must pass ZubeenArtistFilter",
                ZubeenArtistFilter.isValidZubeenRecording(song.titleEnglish, song.artistEnglish)
            )
        }
    }
}
