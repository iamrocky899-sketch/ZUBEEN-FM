package com.amairatech.zubeenfm

import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.DuplicateResolver
import com.amairatech.zubeenfm.data.repository.NormalCatalogueRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IncrementalSyncTest {

    @Test
    fun testIncrementalMergeLogic() {
        val existing = listOf(
            Song(id = "s1", titleAssamese = "Song 1", titleEnglish = "S1"),
            Song(id = "s2", titleAssamese = "Song 2", titleEnglish = "S2")
        )
        
        val discovered = listOf(
            Song(id = "s2", titleAssamese = "Song 2", titleEnglish = "S2"), // duplicate
            Song(id = "s3", titleAssamese = "Song 3", titleEnglish = "S3")  // new
        )
        
        val merged = existing + discovered
        val resolved = DuplicateResolver.deduplicateSongs(merged)
        
        assertEquals("Should have exactly 3 unique songs", 3, resolved.size)
        assertTrue("Should contain new song S3", resolved.any { it.id == "s3" })
    }

    @Test
    fun testNewReleasesSorting() {
        val now = System.currentTimeMillis()
        val s1 = Song(id = "old", titleAssamese = "Old", titleEnglish = "Old", releaseTimestamp = now - 1000000)
        val s2 = Song(id = "new", titleAssamese = "New", titleEnglish = "New", releaseTimestamp = now)
        
        val list = listOf(s1, s2)
        val sorted = list.sortedByDescending { it.releaseTimestamp }
        
        assertEquals("Newest song should be first", "new", sorted[0].id)
    }
}
