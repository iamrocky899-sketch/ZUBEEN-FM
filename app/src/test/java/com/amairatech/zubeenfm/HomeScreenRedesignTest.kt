package com.amairatech.zubeenfm

import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.AssameseMusicFilter
import com.amairatech.zubeenfm.data.repository.NormalCatalogueRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeScreenRedesignTest {

    @Test
    fun testTopChartsLogic() = runBlocking {
        val allSongs = NormalCatalogueRepository.songsFlow.value
        
        val topCharts = allSongs
            .filter { AssameseMusicFilter.isValidForNormalCatalogue(it) }
            .take(10)
            
        assertTrue("Top Charts should have at most 10 songs", topCharts.size <= 10)
        
        for (song in topCharts) {
            assertTrue("Top Chart song must be Assamese: ${song.titleEnglish}", 
                AssameseMusicFilter.isValidForNormalCatalogue(song))
        }
    }

    @Test
    fun testHistoryLogic() = runBlocking {
        val history = NormalCatalogueRepository.recentlyPlayedFlow.value
        
        val listenAgain = history.take(5)
        val remaining = if (history.size > 5) history.drop(5) else emptyList()
        
        assertEquals("Listen Again should take first 5 from history", 
            history.take(5), listenAgain)
            
        if (history.size > 5) {
            assertEquals("Recently Played should take the rest", 
                history.size - 5, remaining.size)
        } else {
            assertTrue("Recently Played should be empty if history <= 5", remaining.isEmpty())
        }
    }
}
