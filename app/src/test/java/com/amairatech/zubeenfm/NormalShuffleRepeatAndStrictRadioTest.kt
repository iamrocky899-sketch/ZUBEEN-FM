package com.amairatech.zubeenfm

import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.ZubeenArtistFilter
import com.amairatech.zubeenfm.data.repository.NormalQueueManager
import com.amairatech.zubeenfm.radio.RadioSongEligibilityChecker
import com.amairatech.zubeenfm.ui.NormalRepeatMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NormalShuffleRepeatAndStrictRadioTest {

    private val testSongs = listOf(
        Song(id = "s1", titleAssamese = "গীত ১", titleEnglish = "Song A", artistEnglish = "Zubeen Garg", artistAssamese = "জুবিন গাৰ্গ", durationSeconds = 200),
        Song(id = "s2", titleAssamese = "গীত ২", titleEnglish = "Song B", artistEnglish = "Khagen Mahanta", artistAssamese = "খগেন মহন্ত", durationSeconds = 210),
        Song(id = "s3", titleAssamese = "গীত ৩", titleEnglish = "Song C", artistEnglish = "Papon", artistAssamese = "পাপন", durationSeconds = 220),
        Song(id = "s4", titleAssamese = "গীত ৪", titleEnglish = "Song D", artistEnglish = "Bhupen Hazarika", artistAssamese = "ভূপেন হাজৰিকা", durationSeconds = 230),
        Song(id = "s5", titleAssamese = "গীত ৫", titleEnglish = "Song E", artistEnglish = "Deeplina Deka", artistAssamese = "দীপলিনা ডেকা", durationSeconds = 240),
        Song(id = "s6", titleAssamese = "গীত ৬", titleEnglish = "Song F", artistEnglish = "Jayanta Hazarika", artistAssamese = "জয়ন্ত হাজৰিকা", durationSeconds = 250)
    )

    // =========================================================================
    // 1. NORMAL MODE SHUFFLE TESTS (Deck / Shuffled Bag)
    // =========================================================================

    @Test
    fun testShuffleDeckPlaysAllSongsBeforeRepeating() {
        val manager = NormalQueueManager()
        manager.updatePlaylist(testSongs, testSongs[0])
        manager.setShuffleEnabled(true, testSongs[0])
        manager.setRepeatMode(NormalRepeatMode.ALL)

        val playedOrder = mutableListOf<Song>()
        var current: Song? = testSongs[0]
        playedOrder.add(current!!)

        // Traverse one full deck of 6 songs
        for (i in 1 until testSongs.size) {
            current = manager.getNextSong(current)
            assertNotNull("Next song in deck must not be null", current)
            playedOrder.add(current!!)
        }

        // 1. Deck must contain all 6 unique items without duplicates within the cycle
        val uniqueIds = playedOrder.map { it.id }.toSet()
        assertEquals("Shuffled deck must play every song in the catalogue once without duplicates", testSongs.size, uniqueIds.size)
    }

    @Test
    fun testShuffleDeckAvoidsImmediateRepeatOnReshuffle() {
        val manager = NormalQueueManager()
        manager.updatePlaylist(testSongs, testSongs[0])
        manager.setShuffleEnabled(true, testSongs[0])
        manager.setRepeatMode(NormalRepeatMode.ALL)

        var current: Song? = testSongs[0]
        // Play through first deck
        for (i in 1 until testSongs.size) {
            current = manager.getNextSong(current)
        }
        val lastSongOfDeck1 = current!!

        // Next call triggers reshuffle for deck 2
        val firstSongOfDeck2 = manager.getNextSong(lastSongOfDeck1)
        assertNotNull("Reshuffled deck must provide next song", firstSongOfDeck2)

        // Must avoid immediate repeat
        assertNotEquals(
            "New shuffled deck must not immediately repeat the last song of the exhausted deck",
            lastSongOfDeck1.id,
            firstSongOfDeck2!!.id
        )
    }

    @Test
    fun testShuffleOffFollowsStrictSequentialOrder() {
        val manager = NormalQueueManager()
        manager.updatePlaylist(testSongs, testSongs[0])
        manager.setShuffleEnabled(false, testSongs[0])
        manager.setRepeatMode(NormalRepeatMode.ALL)

        var current: Song? = testSongs[0]
        val next1 = manager.getNextSong(current)
        assertEquals("s2", next1?.id)

        val next2 = manager.getNextSong(next1)
        assertEquals("s3", next2?.id)

        val next3 = manager.getNextSong(next2)
        assertEquals("s4", next3?.id)
    }

    // =========================================================================
    // 2. NORMAL MODE REPEAT MODES (OFF, ALL, ONE)
    // =========================================================================

    @Test
    fun testRepeatOneAlwaysReturnsCurrentSong() {
        val manager = NormalQueueManager()
        manager.updatePlaylist(testSongs, testSongs[0])
        manager.setRepeatMode(NormalRepeatMode.ONE)

        val next = manager.getNextSong(testSongs[0])
        assertEquals("Repeat ONE must replay current song", testSongs[0].id, next?.id)

        // Even with shuffle enabled, Repeat ONE must repeat current song
        manager.setShuffleEnabled(true, testSongs[0])
        val nextShuffled = manager.getNextSong(testSongs[0])
        assertEquals("Repeat ONE with shuffle must still replay current song", testSongs[0].id, nextShuffled?.id)
    }

    @Test
    fun testRepeatOffStopsAtEndOfQueue() {
        val manager = NormalQueueManager()
        manager.updatePlaylist(testSongs, testSongs[0])
        manager.setShuffleEnabled(false, testSongs[0])
        manager.setRepeatMode(NormalRepeatMode.OFF)

        // Last song in playlist
        val lastSong = testSongs.last()
        val next = manager.getNextSong(lastSong)
        assertNull("Repeat OFF must return null at the end of queue to stop playback", next)
    }

    @Test
    fun testRepeatAllWrapsAroundQueue() {
        val manager = NormalQueueManager()
        manager.updatePlaylist(testSongs, testSongs[0])
        manager.setShuffleEnabled(false, testSongs[0])
        manager.setRepeatMode(NormalRepeatMode.ALL)

        val lastSong = testSongs.last()
        val next = manager.getNextSong(lastSong)
        assertEquals("Repeat ALL must wrap around to the first song in queue", testSongs.first().id, next?.id)
    }

    @Test
    fun testPreviousSongNavigation() {
        val manager = NormalQueueManager()
        manager.updatePlaylist(testSongs, testSongs[0])
        manager.setShuffleEnabled(false, testSongs[0])

        val prevFromSecond = manager.getPreviousSong(testSongs[1])
        assertEquals("Previous from song 2 must be song 1", testSongs[0].id, prevFromSecond?.id)

        val prevFromFirst = manager.getPreviousSong(testSongs[0])
        assertEquals("Previous from song 1 in wrap mode returns last song", testSongs.last().id, prevFromFirst?.id)
    }

    // =========================================================================
    // 3. STRICT RADIO FILTERING & SAFETY GATE
    // =========================================================================

    @Test
    fun testRadioSongEligibilityCheckerStrictlyAllowsOnlyZubeenGarg() {
        val verifiedZubeenSong = Song(
            id = "zg_mayabini",
            titleAssamese = "মায়াবিনী",
            titleEnglish = "Mayabini",
            artistAssamese = "জুবিন গাৰ্গ",
            artistEnglish = "Zubeen Garg",
            durationSeconds = 245,
            isPlayable = true
        )

        assertTrue(
            "Genuine Zubeen recording must be eligible for Radio Mode",
            RadioSongEligibilityChecker.isEligibleForRadio(verifiedZubeenSong)
        )

        val otherAssameseArtistSong = Song(
            id = "norm_bihu",
            titleAssamese = "বিহুৰে বতৰত",
            titleEnglish = "Bihure Botorot",
            artistAssamese = "খগেন মহন্ত",
            artistEnglish = "Khagen Mahanta",
            durationSeconds = 245,
            isPlayable = true
        )

        assertFalse(
            "Other Assamese artists MUST be rejected by Radio Mode",
            RadioSongEligibilityChecker.isEligibleForRadio(otherAssameseArtistSong)
        )

        val coverSong = Song(
            id = "cover_01",
            titleAssamese = "মায়াবিনী কভাৰ",
            titleEnglish = "Mayabini (Female Acoustic Cover)",
            artistAssamese = "কভাৰ আৰ্টিষ্ট",
            artistEnglish = "Cover Singer",
            durationSeconds = 210,
            isPlayable = true
        )
        assertFalse(
            "Covers must be rejected by Radio Mode",
            RadioSongEligibilityChecker.isEligibleForRadio(coverSong)
        )

        val podcastTrack = Song(
            id = "podcast_01",
            titleAssamese = "জুবিন গাৰ্গ সাক্ষাৎকাৰ",
            titleEnglish = "Zubeen Garg Exclusive Interview Podcast",
            artistAssamese = "জুবিন গাৰ্গ",
            artistEnglish = "Zubeen Garg",
            durationSeconds = 600,
            originalLanguage = "ASSAMESE",
            contentType = com.amairatech.zubeenfm.data.model.ContentType.ZUBEEN_PODCAST,
            isPlayable = true
        )

        assertTrue(
            "Podcasts and interviews MUST be ACCEPTED by Radio Mode if they are about Zubeen and are originally Assamese",
            RadioSongEligibilityChecker.isEligibleForRadio(podcastTrack)
        )

        val controversyTalk = Song(
            id = "talk_01",
            titleAssamese = "বিতৰ্ক",
            titleEnglish = "Rahul Gautam Sharma EXPLICIT - on Controversy, Alcohol, Zubeen Garg",
            artistAssamese = "ৰাহুল গৌতম শৰ্মা",
            artistEnglish = "The MONCAST Talks",
            durationSeconds = 600,
            contentType = com.amairatech.zubeenfm.data.model.ContentType.SONG,
            isPlayable = true
        )

        assertFalse(
            "Talk shows and podcasts discussing Zubeen must be rejected by Radio Mode",
            RadioSongEligibilityChecker.isEligibleForRadio(controversyTalk)
        )

        val newsChannelVideo = Song(
            id = "news_01",
            titleAssamese = "খবৰ",
            titleEnglish = "Zubeen Garg Live Speech at Guwahati Press Meet",
            artistAssamese = "ডি ৱাই ৩৬৫",
            artistEnglish = "DY365 News",
            durationSeconds = 400,
            isPlayable = true
        )

        assertFalse(
            "News channel press meets must be rejected by Radio Mode",
            RadioSongEligibilityChecker.isEligibleForRadio(newsChannelVideo)
        )
    }

    @Test
    fun testRadioPlaybackSourceVerification() {
        val testSong = testSongs[0]

        assertTrue(
            "Valid HTTPS stream source must pass verification",
            RadioSongEligibilityChecker.verifyPlaybackSource(testSong, "https://example.com/audio.mp3")
        )

        assertFalse(
            "Null stream source must be rejected",
            RadioSongEligibilityChecker.verifyPlaybackSource(testSong, null)
        )

        assertFalse(
            "Blank stream source must be rejected",
            RadioSongEligibilityChecker.verifyPlaybackSource(testSong, "   ")
        )

        assertFalse(
            "Invalid scheme stream source must be rejected",
            RadioSongEligibilityChecker.verifyPlaybackSource(testSong, "ftp://invalid/file.mp3")
        )
    }
}
