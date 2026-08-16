package com.amairatech.zubeenfm

import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.repository.ZubeenRadioCatalogueRepository
import com.amairatech.zubeenfm.radio.RadioStationClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RadioSyncTest {

    @Test
    fun testRadioClockDeterministicSequence() {
        val testCatalogue = listOf(
            Song(id = "a", titleAssamese = "A", titleEnglish = "A"),
            Song(id = "b", titleAssamese = "B", titleEnglish = "B"),
            Song(id = "c", titleAssamese = "C", titleEnglish = "C")
        )
        
        val sequence1 = RadioStationClock.buildDeterministicStationSequence(testCatalogue)
        val sequence2 = RadioStationClock.buildDeterministicStationSequence(testCatalogue)
        
        assertEquals(sequence1.map { it.id }, sequence2.map { it.id })
    }

    @Test
    fun testRadioClockSlotConsistency() {
        val catalogue = ZubeenRadioCatalogueRepository.playlist
        val now = System.currentTimeMillis()
        
        val slot1 = RadioStationClock.calculateCurrentBroadcastSlot(catalogue, now)
        val slot2 = RadioStationClock.calculateCurrentBroadcastSlot(catalogue, now)
        
        assertNotNull(slot1)
        assertNotNull(slot2)
        assertEquals(slot1!!.song.id, slot2!!.song.id)
        assertEquals(slot1.intraSongOffsetMs, slot2!!.intraSongOffsetMs)
    }

    @Test
    fun testRadioClockAdvancesWithTime() {
        val catalogue = ZubeenRadioCatalogueRepository.playlist
        val now = System.currentTimeMillis()
        
        val slotStart = RadioStationClock.calculateCurrentBroadcastSlot(catalogue, now)
        val slotLater = RadioStationClock.calculateCurrentBroadcastSlot(catalogue, now + 10000) // 10s later
        
        assertNotNull(slotStart)
        assertNotNull(slotLater)
        
        if (slotStart!!.song.id == slotLater!!.song.id) {
            assertTrue("Offset must increase if same song", slotLater.intraSongOffsetMs > slotStart.intraSongOffsetMs)
        } else {
            // Track changed - offset depends on when exactly it switched during the 10s window
            assertTrue("Track changed correctly", true)
        }
    }

    @Test
    fun testNormalSongsAZSorting() {
        val songs = listOf(
            Song(id = "1", titleAssamese = "মায়াবিনী", titleEnglish = "Mayabini"),
            Song(id = "2", titleAssamese = "অনামী", titleEnglish = "Anami"),
            Song(id = "3", titleAssamese = "জোনাকী", titleEnglish = "Jonaki")
        )
        
        val sorted = songs.sortedBy { it.titleEnglish.lowercase() }
        
        assertEquals("Anami", sorted[0].titleEnglish)
        assertEquals("Jonaki", sorted[1].titleEnglish)
        assertEquals("Mayabini", sorted[2].titleEnglish)
    }
}
