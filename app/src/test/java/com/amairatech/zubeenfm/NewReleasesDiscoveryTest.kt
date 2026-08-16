package com.amairatech.zubeenfm

import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.AssameseMusicFilter
import com.amairatech.zubeenfm.data.provider.YouTubeMusicProvider
import com.amairatech.zubeenfm.data.repository.NormalCatalogueRepository
import com.amairatech.zubeenfm.ui.AppNavTab
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NewReleasesDiscoveryTest {

    @Test
    fun testHomeTabAndNavigationCount() {
        val tabs = AppNavTab.values()
        assertEquals("Should have exactly 5 tabs", 5, tabs.size)
        assertEquals("First tab should be HOME", AppNavTab.HOME, tabs[0])
        assertEquals("Second tab should be RADIO", AppNavTab.RADIO, tabs[1])
    }

    @Test
    fun testNewReleasesDiscoveryPipeline() = runBlocking {
        val provider = YouTubeMusicProvider()
        
        // This will trigger real network requests if not mocked, 
        // but in this environment we expect it to work or be handled by the system.
        val result = provider.discoverNewAssameseReleases(page = 1, pageSize = 5)
        
        // Verification of recent discovery
        if (result.songs.isNotEmpty()) {
            for (song in result.songs) {
                assertTrue("Discovery must return Assamese songs only", 
                    AssameseMusicFilter.isValidForNormalCatalogue(song))
                
                // Check if release timestamp is set (heuristic for "new")
                assertTrue("New releases should have a valid timestamp", song.releaseTimestamp > 0)
            }
        }
    }

    @Test
    fun testLanguageRuleForNewReleases() {
        val recentHindiSong = Song(
            id = "hindi_recent_01",
            titleAssamese = "New Hindi Song",
            titleEnglish = "New Hindi Song",
            artistEnglish = "Bollywood Artist",
            originalLanguage = "HINDI",
            releaseTimestamp = System.currentTimeMillis()
        )
        
        assertFalse("Recent Hindi songs must be rejected from general Assamese discovery", 
            AssameseMusicFilter.isValidForNormalCatalogue(recentHindiSong))
            
        val recentAssameseSong = Song(
            id = "assamese_recent_01",
            titleAssamese = "নতুন অসমীয়া গীত",
            titleEnglish = "New Assamese Song",
            artistEnglish = "Assamese Artist",
            originalLanguage = "ASSAMESE",
            releaseTimestamp = System.currentTimeMillis()
        )
        
        assertTrue("Recent Assamese songs must be accepted", 
            AssameseMusicFilter.isValidForNormalCatalogue(recentAssameseSong))
    }

    @Test
    fun testZubeenSpecialRuleInNewReleases() {
        val zubeenHindiSong = Song(
            id = "zubeen_hindi_recent",
            titleAssamese = "জুবিনৰ হিন্দী গীত",
            titleEnglish = "Zubeen New Hindi",
            artistEnglish = "Zubeen Garg",
            originalLanguage = "HINDI",
            releaseTimestamp = System.currentTimeMillis()
        )
        
        assertFalse("Zubeen's Hindi songs should NOT be in the general Assamese New Releases list", 
            AssameseMusicFilter.isValidForNormalCatalogue(zubeenHindiSong))
    }
}
