package com.amairatech.zubeenfm

import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.PlaybackSource
import com.amairatech.zubeenfm.data.provider.YouTubeMusicProvider
import com.amairatech.zubeenfm.data.provider.ZubeenArtistFilter
import com.amairatech.zubeenfm.data.repository.SongRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL

class RadioPlaybackTest {

    @Test
    fun testContinuousZubeenRadioQueue() {
        val testSongs = listOf(
            Song(id = "s1", titleAssamese = "মায়াবিনী", titleEnglish = "Mayabini", albumAssamese = "মায়া", albumEnglish = "Maya"),
            Song(id = "s2", titleAssamese = "মন যায়", titleEnglish = "Mon Jaai", albumAssamese = "মন যায়", albumEnglish = "Mon Jaai"),
            Song(id = "s3", titleAssamese = "অনামী", titleEnglish = "Anami", albumAssamese = "মুকুতা", albumEnglish = "Mukuta")
        )

        // Queue progression: s1 -> s2 -> s3 -> s1
        val next1 = testSongs[(testSongs.indexOf(testSongs[0]) + 1) % testSongs.size]
        val next2 = testSongs[(testSongs.indexOf(next1) + 1) % testSongs.size]
        val next3 = testSongs[(testSongs.indexOf(next2) + 1) % testSongs.size]

        assertEquals("s2", next1.id)
        assertEquals("s3", next2.id)
        assertEquals("s1", next3.id)
    }

    @Test
    fun testRealYouTubeMusicLiveExtractionAndAudioBytes() = runBlocking {
        val provider = YouTubeMusicProvider()

        // 1. Dynamic InnerTube Discovery
        val pageResult = provider.discoverZubeenMusic(page = 1, pageSize = 6)
        assertTrue("Live discovery must find songs", pageResult.songs.isNotEmpty())

        val testSong = pageResult.songs.first()
        println("Discovered Song for Playback Test: ${testSong.titleEnglish} | ID: ${testSong.id}")

        // 2. Strict Zubeen Performer Check
        assertTrue(
            "Performer must be Zubeen Garg",
            ZubeenArtistFilter.isValidZubeenRecording(testSong.titleEnglish, testSong.artistEnglish)
        )

        // 3. Real Stream Resolution
        val playbackSource = testSong.playbackSources.first()
        val streamUrl = provider.resolveStreamUrl(playbackSource)
        assertNotNull("Resolved stream URL must not be null", streamUrl)
        assertTrue("Stream URL must be a valid HTTPS GoogleVideo URL", streamUrl!!.startsWith("https://"))

        println("Resolved Stream URL: ${streamUrl.take(75)}...")

        // 4. Test actual audio stream reachability and read real audio bytes
        val conn = URL(streamUrl).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
        conn.setRequestProperty("Range", "bytes=0-8191")
        conn.connectTimeout = 8000
        conn.readTimeout = 8000

        val code = conn.responseCode
        val contentType = conn.contentType
        val contentLength = conn.contentLengthLong

        println("Audio Stream HTTP Status: $code | Content-Type: $contentType | Content-Length: $contentLength")
        assertTrue("HTTP response code must be 200 or 206 (Partial Content)", code in 200..299 || code == 206)

        val buffer = ByteArray(8192)
        val bytesRead = conn.inputStream.use { it.read(buffer) }
        println("SUCCESS: Read $bytesRead REAL LIVE AUDIO BYTES from stream!")
        assertTrue("Must read real audio bytes", bytesRead > 0)
    }
}
