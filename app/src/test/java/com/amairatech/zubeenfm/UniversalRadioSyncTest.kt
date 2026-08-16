package com.amairatech.zubeenfm

import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.PlaybackSource
import com.amairatech.zubeenfm.radio.RadioStationClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UniversalRadioSyncTest {

    private val sampleRadioCatalogue = listOf(
        Song(id = "z1", titleAssamese = "মায়াবিনী", titleEnglish = "Mayabini", durationSeconds = 300, albumAssamese = "মায়া", albumEnglish = "Maya"),
        Song(id = "z2", titleAssamese = "অনামীকা", titleEnglish = "Anamika", durationSeconds = 240, albumAssamese = "অনামীকা", albumEnglish = "Anamika"),
        Song(id = "z3", titleAssamese = "মন যায়", titleEnglish = "Mon Jaai", durationSeconds = 360, albumAssamese = "মন যায়", albumEnglish = "Mon Jaai"),
        Song(id = "z4", titleAssamese = "ৰ'দালি", titleEnglish = "Rodali", durationSeconds = 280, albumAssamese = "ৰ'দালি", albumEnglish = "Rodali"),
        Song(id = "z5", titleAssamese = "মুকুতা", titleEnglish = "Mukuta", durationSeconds = 320, albumAssamese = "মুকুতা", albumEnglish = "Mukuta")
    )

    @Test
    fun testTwoDevicesReceiveExactSameSongAtTimestampT() {
        val timestampT = 1786779000000L // arbitrary fixed station timestamp

        val deviceASlot = RadioStationClock.calculateCurrentBroadcastSlot(sampleRadioCatalogue, timestampT)
        val deviceBSlot = RadioStationClock.calculateCurrentBroadcastSlot(sampleRadioCatalogue, timestampT)

        assertNotNull("Device A slot must not be null", deviceASlot)
        assertNotNull("Device B slot must not be null", deviceBSlot)

        // 1. Same song
        assertEquals("Both devices must calculate identical song at time T", deviceASlot!!.song.id, deviceBSlot!!.song.id)

        // 2. Same sequence index in station schedule
        assertEquals("Both devices must have identical sequence index", deviceASlot.sequenceIndex, deviceBSlot.sequenceIndex)

        // 3. Same intra-song playback offset
        assertEquals("Both devices must have identical intra-song offset", deviceASlot.intraSongOffsetMs, deviceBSlot.intraSongOffsetMs)
    }

    @Test
    fun testTwoDevicesReceiveExactSameSongAtTimestampTPlus10Minutes() {
        val timestampT = 1786779000000L
        val timestampTPlus10Min = timestampT + (10 * 60 * 1000L) // +10 minutes

        val deviceASlot10Min = RadioStationClock.calculateCurrentBroadcastSlot(sampleRadioCatalogue, timestampTPlus10Min)
        val deviceBSlot10Min = RadioStationClock.calculateCurrentBroadcastSlot(sampleRadioCatalogue, timestampTPlus10Min)

        assertNotNull(deviceASlot10Min)
        assertNotNull(deviceBSlot10Min)

        assertEquals("Both devices at T+10min must receive same song", deviceASlot10Min!!.song.id, deviceBSlot10Min!!.song.id)
        assertEquals("Both devices at T+10min must receive same sequence index", deviceASlot10Min.sequenceIndex, deviceBSlot10Min.sequenceIndex)
        assertEquals("Both devices at T+10min must receive same offset", deviceASlot10Min.intraSongOffsetMs, deviceBSlot10Min.intraSongOffsetMs)

        // Ensure station moved forward over 10 minutes
        val initialSlot = RadioStationClock.calculateCurrentBroadcastSlot(sampleRadioCatalogue, timestampT)
        assertTrue(
            "Station position after 10 minutes must advance from initial timestamp",
            initialSlot!!.song.id != deviceASlot10Min.song.id || initialSlot.intraSongOffsetMs != deviceASlot10Min.intraSongOffsetMs
        )
    }

    @Test
    fun testDeterministicBagSequencePlaysEntireCatalogueBeforeRepeating() {
        val sequence = RadioStationClock.buildDeterministicStationSequence(sampleRadioCatalogue)

        assertEquals(sampleRadioCatalogue.size, sequence.size)
        val uniqueIds = sequence.map { it.id }.toSet()
        assertEquals("Sequence must contain all unique catalogue songs without duplicates in a cycle", sampleRadioCatalogue.size, uniqueIds.size)
    }
}
