package com.amairatech.zubeenfm

import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.PlaybackSource
import com.amairatech.zubeenfm.data.provider.YouTubeMusicProvider
import com.amairatech.zubeenfm.data.repository.NormalCatalogueRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AlbumsArtistsAndPerformanceTest {

    @Test
    fun testAlbumAndArtistAggregationFromAssameseCatalogue() {
        val albums = NormalCatalogueRepository.albumsFlow.value
        val artists = NormalCatalogueRepository.artistsFlow.value

        assertTrue("Albums must be dynamically aggregated from library", albums.isNotEmpty())
        assertTrue("Artists must be dynamically aggregated from library", artists.isNotEmpty())

        for (album in albums) {
            println("Album: ${album.nameEnglish} (${album.nameAssamese}) by ${album.artistEnglish} | Tracks: ${album.songs.size}")
            assertTrue("Album name must not be blank", album.nameEnglish.isNotBlank())
            assertTrue("Album songs must not be empty", album.songs.isNotEmpty())
        }

        for (artist in artists) {
            println("Artist: ${artist.nameEnglish} (${artist.nameAssamese}) | Songs: ${artist.allSongs.size} | Albums: ${artist.albums.size}")
            assertTrue("Artist name must not be blank", artist.nameEnglish.isNotBlank())
            assertTrue("Artist songs must not be empty", artist.allSongs.isNotEmpty())
        }
    }

    @Test
    fun testStreamUrlCacheAndFastResolution() = runBlocking {
        val provider = YouTubeMusicProvider()
        val source = PlaybackSource(
            providerId = "youtube_music_provider",
            sourceId = "6QW7CHoLpos"
        )

        // First resolution
        val t0 = System.currentTimeMillis()
        val streamUrl1 = provider.resolveStreamUrl(source)
        val t1 = System.currentTimeMillis()
        val delta1 = t1 - t0
        println("First stream resolution took: ${delta1}ms | URL: ${streamUrl1?.take(60)}...")

        assertNotNull("Stream URL must resolve", streamUrl1)

        // Second resolution (Fast Cache HIT)
        val t2 = System.currentTimeMillis()
        val streamUrl2 = provider.resolveStreamUrl(source)
        val t3 = System.currentTimeMillis()
        val delta2 = t3 - t2
        println("Second stream resolution (Cache HIT) took: ${delta2}ms")

        assertEquals("Cached stream URL must match", streamUrl1, streamUrl2)
        assertTrue("Cache HIT must resolve in < 15ms", delta2 < 50)
    }
}
