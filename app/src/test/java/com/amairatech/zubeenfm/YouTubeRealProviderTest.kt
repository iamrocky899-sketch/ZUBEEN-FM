package com.amairatech.zubeenfm

import com.amairatech.zubeenfm.data.provider.PlaybackSource
import com.amairatech.zubeenfm.data.provider.YouTubeMusicProvider
import com.amairatech.zubeenfm.data.provider.ZubeenArtistFilter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YouTubeRealProviderTest {

    @Test
    fun testLiveYouTubeMusicDiscoveryAndFiltering() = runBlocking {
        val provider = YouTubeMusicProvider()

        // 1. Live network discovery
        val resultPage1 = provider.discoverZubeenMusic(page = 1, pageSize = 8)
        println("Discovered songs count on Page 1: ${resultPage1.songs.size}")

        assertTrue("Dynamic discovery should return songs", resultPage1.songs.isNotEmpty())

        for (song in resultPage1.songs) {
            println("Discovered: ${song.titleEnglish} | Artist: ${song.artistEnglish} | Album: ${song.albumEnglish} | ID: ${song.id} | Artwork: ${song.artworkUrl != null}")

            // 2. Strict Zubeen artist filter validation
            assertTrue(
                "Song ${song.titleEnglish} must be genuine Zubeen recording",
                ZubeenArtistFilter.isValidZubeenRecording(song.titleEnglish, song.artistEnglish)
            )

            // 3. Metadata validation
            assertTrue("Title must not be empty", song.titleEnglish.isNotBlank())
            assertTrue("ID must start with yt_", song.id.startsWith("yt_"))
            assertTrue("Playback sources must be attached", song.playbackSources.isNotEmpty())
        }

        // 4. Test Search
        val searchResult = provider.searchSongs("Maya", page = 1, pageSize = 5)
        println("Search results for 'Maya': ${searchResult.songs.size}")
        assertTrue("Search should return results", searchResult.songs.isNotEmpty())

        // 5. Test Stream Resolution Call (handles gracefully if stream is protected)
        val testSong = resultPage1.songs.first()
        val streamUrl = provider.resolveStreamUrl(testSong.playbackSources.first())
        println("Resolved stream url: $streamUrl")
    }
}
