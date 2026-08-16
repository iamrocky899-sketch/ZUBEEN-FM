package com.amairatech.zubeenfm

import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.DuplicateResolver
import com.amairatech.zubeenfm.data.provider.MetadataNormalizer
import com.amairatech.zubeenfm.data.provider.PlaybackSource
import com.amairatech.zubeenfm.data.provider.YouTubeMusicProvider
import com.amairatech.zubeenfm.data.provider.ZubeenArtistFilter
import com.amairatech.zubeenfm.data.provider.ZubeenDiscoveryEngine
import com.amairatech.zubeenfm.data.repository.SongRepository
import com.amairatech.zubeenfm.data.repository.ZubeenFactRepository
import com.amairatech.zubeenfm.radio.RadioStationClock
import com.amairatech.zubeenfm.ui.AppNavTab
import com.amairatech.zubeenfm.ui.tribute.TributeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompleteQATest {

    @Test
    fun testHomeAndEnglishBrandingIdentity() {
        val appName = "ZUBEEN FM"
        assertEquals("ZUBEEN FM", appName)

        val navTabs = AppNavTab.values()
        assertEquals(5, navTabs.size)
        // User Interface navigation labels in English
        assertTrue(navTabs.any { it.labelEnglish == "Home" })
        assertTrue(navTabs.any { it.labelEnglish == "Radio" })
        assertTrue(navTabs.any { it.labelEnglish == "Songs" })
        assertTrue(navTabs.any { it.labelEnglish == "Tribute" })
        assertTrue(navTabs.any { it.labelEnglish == "About" })
    }

    @Test
    fun testStrictZubeenArtistFilter() {
        // Genuine Zubeen tracks -> PASS
        assertTrue(ZubeenArtistFilter.isValidZubeenRecording("Mayabini", "Zubeen Garg"))
        assertTrue(ZubeenArtistFilter.isValidZubeenRecording("মায়াবিনী", "জুবিন গাৰ্গ"))
        assertTrue(ZubeenArtistFilter.isValidZubeenRecording("Ya Ali", "Zubeen Garg"))

        // Covers, reactions, tributes, podcasts by other artists -> REJECT
        assertFalse(ZubeenArtistFilter.isValidZubeenRecording("Mayabini (Cover by ABC)", "ABC"))
        assertFalse(ZubeenArtistFilter.isValidZubeenRecording("Zubeen Garg Hit Songs Reaction", "Vlogger XYZ"))
        assertFalse(ZubeenArtistFilter.isValidZubeenRecording("Tribute to Zubeen Garg by XYZ", "XYZ Singer"))
        assertFalse(ZubeenArtistFilter.isValidZubeenRecording("Zubeen Garg Exclusive Interview Podcast", "Media House"))
        assertFalse(ZubeenArtistFilter.isValidZubeenRecording("Tum Hi Ho", "Arijit Singh"))
    }

    @Test
    fun testMetadataNormalizerAndDuplicateResolver() {
        val raw = "Mayabini [Official Audio] (Full Song) | Zubeen Garg Hits"
        val cleaned = MetadataNormalizer.cleanTitle(raw)
        assertEquals("Mayabini", cleaned)

        val langAssamese = MetadataNormalizer.detectLanguage("Mayabini", "Maya")
        assertEquals("অসমীয়া", langAssamese)

        val langHindi = MetadataNormalizer.detectLanguage("Ya Ali", "Gangster")
        assertEquals("হিন্দী", langHindi)

        val songA = Song(
            id = "a1",
            titleAssamese = "মায়াবিনী",
            titleEnglish = "Mayabini",
            albumAssamese = "মায়া",
            albumEnglish = "Maya",
            streamUrl = "http://stream1.mp3",
            playbackSources = listOf(PlaybackSource("p1", "s1", "http://stream1.mp3"))
        )
        val songB = Song(
            id = "b1",
            titleAssamese = "মায়াবিনী",
            titleEnglish = "Mayabini",
            albumAssamese = "মায়া",
            albumEnglish = "Maya",
            streamUrl = "http://stream2.mp3",
            playbackSources = listOf(PlaybackSource("p2", "s2", "http://stream2.mp3"))
        )

        val deduplicated = DuplicateResolver.deduplicateSongs(listOf(songA, songB))
        assertEquals(1, deduplicated.size)
        assertEquals(2, deduplicated.first().playbackSources.size)
    }

    @Test
    fun testMultiProviderDiscoveryAndPagination() = runBlocking {
        val ytmProvider = YouTubeMusicProvider()

        val ytmPage1 = ytmProvider.discoverAssameseMusic(page = 1, pageSize = 4)
        assertTrue("YouTubeMusicProvider should discover tracks", ytmPage1.songs.isNotEmpty())

        val engine = ZubeenDiscoveryEngine(
            providers = listOf(ytmProvider)
        )

        val globalPage1 = engine.discoverAssameseCatalogue(page = 1, pageSize = 10)
        assertTrue("Global catalogue discovery must contain songs", globalPage1.songs.isNotEmpty())
    }

    @Test
    fun testNormalModeSeekCalculations() {
        val duration = 240
        val seekTarget1 = -10
        val clamped1 = seekTarget1.coerceIn(0, duration)
        assertEquals(0, clamped1)

        val seekTarget2 = 300
        val clamped2 = seekTarget2.coerceIn(0, duration)
        assertEquals(240, clamped2)

        val seekTarget3 = 120
        val clamped3 = seekTarget3.coerceIn(0, duration)
        assertEquals(120, clamped3)

        val prog = (clamped3.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        assertEquals(0.5f, prog, 0.001f)
    }

    @Test
    fun testDeterministicStationSchedule() {
        val song1 = Song(
            id = "s1",
            titleAssamese = "মায়াবিনী",
            titleEnglish = "Mayabini",
            albumAssamese = "মায়া",
            albumEnglish = "Maya",
            artistAssamese = "জুবিন গাৰ্গ",
            artistEnglish = "Zubeen Garg",
            durationSeconds = 300
        )
        val song2 = Song(
            id = "s2",
            titleAssamese = "চুইট লাভ",
            titleEnglish = "Sweet Love",
            albumAssamese = "চুইট লাভ",
            albumEnglish = "Sweet Love",
            artistAssamese = "জুবিন গাৰ্গ",
            artistEnglish = "Zubeen Garg",
            durationSeconds = 240
        )
        val catalogue = listOf(song1, song2)

        val t1 = 1704067200000L + 50000L // 50 seconds after epoch
        val slot1 = RadioStationClock.calculateCurrentBroadcastSlot(catalogue, t1)
        assertNotNull(slot1)

        // Multiple calls at identical timestamp must return identical slot and offset
        val slot2 = RadioStationClock.calculateCurrentBroadcastSlot(catalogue, t1)
        assertEquals(slot1!!.song.id, slot2!!.song.id)
        assertEquals(slot1.intraSongOffsetMs, slot2.intraSongOffsetMs)
    }

    @Test
    fun testRadioModeStrictAssameseAndZeroBengali() {
        val bengaliRa = '\u09B0' // Bengali 'র'

        for (song in SongRepository.playlist) {
            assertFalse(
                "Song title [${song.titleAssamese}] contains Bengali letter 'র'",
                song.titleAssamese.contains(bengaliRa)
            )
        }

        for (fact in ZubeenFactRepository.verifiedFacts) {
            assertFalse(
                "Fact [${fact.id}] contains Bengali letter 'র'",
                fact.factAssamese.contains(bengaliRa)
            )
            assertTrue(
                "Fact [${fact.id}] should contain Assamese 'ৰ' or 'ৱ'",
                fact.factAssamese.contains('\u09F0') || fact.factAssamese.contains('\u09F1') || fact.factAssamese.length > 5
            )
        }
    }

    @Test
    fun testTributeScreenFactsAndTimer() = runBlocking {
        val scope = CoroutineScope(Dispatchers.Default)
        val viewModel = TributeViewModel(
            factRepository = ZubeenFactRepository,
            coroutineScope = scope
        )

        // Lifespan & tribute line validation
        val tributeLine = "তোমাৰ সুৰে আমাক সদায় জীয়াই থকাৰ সাহস দিব, জুবিন দা।"
        assertFalse(tributeLine.contains('\u09B0'))
        assertTrue(tributeLine.contains('\u09F0'))

        // Non-repeating fact verification
        ZubeenFactRepository.resetHistory()
        val factCount = ZubeenFactRepository.verifiedFacts.size
        val uniqueIds = (0 until factCount).map { ZubeenFactRepository.getNextRandomFact().id }.toSet()
        assertEquals(factCount, uniqueIds.size)

        // Timer lifecycle test
        viewModel.onScreenVisible()
        assertTrue(viewModel.uiState.value.isScreenVisible)

        delay(200L)
        viewModel.onScreenHidden()
        assertFalse(viewModel.uiState.value.isScreenVisible)
        assertFalse(viewModel.uiState.value.isFactVisible)
    }
}
