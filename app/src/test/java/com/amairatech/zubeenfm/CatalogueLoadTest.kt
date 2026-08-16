package com.amairatech.zubeenfm

import com.amairatech.zubeenfm.data.repository.NormalCatalogueRepository
import com.amairatech.zubeenfm.data.repository.ZubeenRadioCatalogueRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogueLoadTest {

    @Test
    fun testCompleteCatalogueLoadOnce() = runBlocking {
        // Wait for any initial background load triggered by object init to settle
        var attempts = 0
        while (NormalCatalogueRepository.providerManager.isNormalLoading.value && attempts < 50) {
            delay(100)
            attempts++
        }

        // Trigger complete load
        NormalCatalogueRepository.providerManager.loadCompleteNormalCatalogue(isRefresh = true)
        
        // Wait for it to finish completely - increase timeout for broad discovery
        attempts = 0
        while (NormalCatalogueRepository.providerManager.isNormalLoading.value && attempts < 5000) {
            delay(100)
            attempts++
        }
        delay(2000) // Ensure StateFlow collector in Repository has updated _songsFlow
        
        val initialSize = NormalCatalogueRepository.playlist.size
        assertTrue("Catalogue should not be empty. Size: $initialSize", initialSize > 0)
        
        // Ensure first sync finished successfully before testing subsequent skip
        assertEquals("First sync should be completed", com.amairatech.zubeenfm.data.provider.SyncState.COMPLETED, NormalCatalogueRepository.providerManager.normalSyncState.value)

        // Attempt another load without refresh - should return true immediately as normalIsEndReached = true
        var success2 = NormalCatalogueRepository.providerManager.loadCompleteNormalCatalogue(isRefresh = false)
        assertTrue("Subsequent load should return true (skipped).", success2)
        
        // Verify size is stable
        assertEquals("Catalogue size should remain stable", initialSize, NormalCatalogueRepository.playlist.size)
    }

    @Test
    fun testIncrementalCatalogueMerge() = runBlocking {
        val initialSize = NormalCatalogueRepository.playlist.size
        
        // Simulate background sync discovering new songs
        // In this test, we just call the real provider manager which merges
        val success = NormalCatalogueRepository.providerManager.loadCompleteNormalCatalogue(isRefresh = true)
        assertTrue("Sync should report success", success || NormalCatalogueRepository.providerManager.isNormalLoading.value)
        
        // Wait for finish
        var attempts = 0
        while (NormalCatalogueRepository.providerManager.isNormalLoading.value && attempts < 600) {
            delay(100)
            attempts++
        }
        
        val finalSize = NormalCatalogueRepository.playlist.size
        assertTrue("Catalogue size should be >= initial size. initial=$initialSize, final=$finalSize", finalSize >= initialSize)
        
        // Verify no duplicates (DuplicateResolver is used internally)
        val uniqueIds = NormalCatalogueRepository.playlist.map { it.id }.distinct()
        assertEquals("Catalogue should not have duplicates", uniqueIds.size, NormalCatalogueRepository.playlist.size)
    }

    @Test
    fun testBackgroundSyncDoesNotAffectRadio() = runBlocking {
        val initialRadioSong = ZubeenRadioCatalogueRepository.playlist.first()
        
        // Trigger normal mode background sync
        NormalCatalogueRepository.providerManager.loadCompleteNormalCatalogue(isRefresh = true)
        
        // Verify radio catalogue is still the same or at least still present
        val radioSongNow = ZubeenRadioCatalogueRepository.playlist.first()
        assertEquals("Radio catalogue should be unaffected by normal mode sync", initialRadioSong.id, radioSongNow.id)
    }
}
