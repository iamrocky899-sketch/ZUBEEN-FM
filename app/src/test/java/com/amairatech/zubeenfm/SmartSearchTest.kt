package com.amairatech.zubeenfm

import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.ZubeenSmartSearchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartSearchTest {

    private val testCatalogue = listOf(
        Song(
            id = "s1",
            titleAssamese = "মায়াবিনী",
            titleEnglish = "Mayabini",
            artistAssamese = "জুবিন গাৰ্গ",
            artistEnglish = "Zubeen Garg",
            albumEnglish = "Maya",
            genreAssamese = "আধুনিক সুৰীয়া",
            releaseYear = "2001"
        ),
        Song(
            id = "s2",
            titleAssamese = "মণিহাৰা",
            titleEnglish = "Monihara",
            artistAssamese = "পাপন",
            artistEnglish = "Papon",
            albumEnglish = "Papon Special",
            genreAssamese = "চিৰসেউজ মেল'ডী",
            releaseYear = "2012"
        ),
        Song(
            id = "s3",
            titleAssamese = "বিহুৰে বতৰত",
            titleEnglish = "Bihure Botorot",
            artistAssamese = "খগেন মহন্ত",
            artistEnglish = "Khagen Mahanta",
            genreAssamese = "বিহু আৰু লোকসংগীত",
            releaseYear = "1988"
        ),
        Song(
            id = "s4",
            titleAssamese = "অনামী",
            titleEnglish = "Anamika",
            artistAssamese = "জুবিন গাৰ্গ",
            artistEnglish = "Zubeen Garg",
            albumEnglish = "Anamika",
            genreAssamese = "ৰ'মাণ্টিক সুৰ",
            releaseYear = "1992"
        )
    )

    @Test
    fun testExactTitleMatch() {
        val results = ZubeenSmartSearchEngine.search(testCatalogue, "Mayabini")
        assertEquals("s1", results.first().id)
    }

    @Test
    fun testMultiWordSearch() {
        // Query "zubeen maya" should prioritize Mayabini by Zubeen Garg
        val results = ZubeenSmartSearchEngine.search(testCatalogue, "zubeen maya")
        assertEquals("s1", results.first().id)
    }

    @Test
    fun testFuzzyMatch() {
        // "zubin" should match "Zubeen"
        val results = ZubeenSmartSearchEngine.search(testCatalogue, "zubin")
        assertTrue(results.any { it.artistEnglish.contains("Zubeen") })
        
        // "mya" should match "Maya" or "Mayabini"
        val results2 = ZubeenSmartSearchEngine.search(testCatalogue, "mya")
        assertTrue(results2.any { it.titleEnglish.contains("Maya") })
    }

    @Test
    fun testGenreSearch() {
        // "bihu" should match "Bihure Botorot"
        val results = ZubeenSmartSearchEngine.search(testCatalogue, "bihu")
        assertEquals("s3", results.first().id)
    }

    @Test
    fun testAssameseUnicodeSearch() {
        val results = ZubeenSmartSearchEngine.search(testCatalogue, "জুবিন")
        assertTrue(results.any { it.artistAssamese.contains("জুবিন") })
    }
    
    @Test
    fun testEraSearch() {
        // "80s" should match "Bihure Botorot" (1988)
        val results = ZubeenSmartSearchEngine.search(testCatalogue, "80s")
        assertEquals("s3", results.first().id)
        
        // "old zubeen" should match "Anamika" (1992) or "Mayabini" (2001)
        val results2 = ZubeenSmartSearchEngine.search(testCatalogue, "old zubeen")
        assertTrue(results2.first().id == "s4" || results2.first().id == "s1")
    }
}
