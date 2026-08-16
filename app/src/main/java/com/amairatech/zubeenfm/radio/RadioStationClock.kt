package com.amairatech.zubeenfm.radio

import com.amairatech.zubeenfm.data.model.Song
import java.util.Random

/**
 * Universal Station Clock for ZUBEEN FM.
 * Computes a deterministic, universally synchronized broadcast schedule
 * across all client devices based on a stable station epoch and actual song durations.
 *
 * Guaranteed Properties:
 * 1. Multi-Device Synchronization: Phone A and Phone B querying at timestamp T receive the EXACT same song and offset.
 * 2. Non-Repeating Bag Sequence: Shuffles all discovered Zubeen songs into a deterministic deck before repeating.
 * 3. Real-Time Drift Compensation: Resuming after pause instantly jumps to the active broadcast position.
 */
object RadioStationClock {

    // Stable Epoch: Jan 1, 2024 00:00:00 UTC
    const val STATION_EPOCH_MS: Long = 1704067200000L
    private const val STATION_CYCLE_SEED: Long = 0x5A4245454EL // "ZBEEN" in hex

    data class StationBroadcastSlot(
        val song: Song,
        val sequenceIndex: Int,
        val intraSongOffsetMs: Long,
        val songDurationMs: Long,
        val cycleTotalDurationMs: Long,
        val stationTimeMs: Long
    )

    /**
     * Builds a deterministic sequence of songs using a seeded Fisher-Yates shuffle over the catalogue.
     */
    fun buildDeterministicStationSequence(catalogue: List<Song>): List<Song> {
        if (catalogue.isEmpty()) return emptyList()
        // Sort canonically first to guarantee identical initial state across all devices
        val canonicalList = catalogue.sortedBy { it.id }.toMutableList()
        val rng = Random(STATION_CYCLE_SEED)
        for (i in canonicalList.size - 1 downTo 1) {
            val j = rng.nextInt(i + 1)
            val temp = canonicalList[i]
            canonicalList[i] = canonicalList[j]
            canonicalList[j] = temp
        }
        return canonicalList
    }

    /**
     * Calculates the universal active station broadcast song and playback offset for a given timestamp.
     */
    fun calculateCurrentBroadcastSlot(
        catalogue: List<Song>,
        currentTimestampMs: Long = System.currentTimeMillis()
    ): StationBroadcastSlot? {
        if (catalogue.isEmpty()) return null

        val sequence = buildDeterministicStationSequence(catalogue)
        if (sequence.isEmpty()) return null

        // Total cycle duration in milliseconds
        val cycleTotalDurationMs = sequence.sumOf { (it.durationSeconds.coerceAtLeast(60)) * 1000L }
        if (cycleTotalDurationMs <= 0) return null

        // Elapsed time since station epoch, wrapped modulo total cycle length
        val elapsedSinceEpoch = currentTimestampMs - STATION_EPOCH_MS
        val currentCycleOffsetMs = ((elapsedSinceEpoch % cycleTotalDurationMs) + cycleTotalDurationMs) % cycleTotalDurationMs

        var accumulatedMs = 0L
        for ((index, song) in sequence.withIndex()) {
            val songDurationMs = (song.durationSeconds.coerceAtLeast(60)) * 1000L
            val songEndMs = accumulatedMs + songDurationMs

            if (currentCycleOffsetMs in accumulatedMs until songEndMs) {
                val intraSongOffsetMs = currentCycleOffsetMs - accumulatedMs
                return StationBroadcastSlot(
                    song = song,
                    sequenceIndex = index,
                    intraSongOffsetMs = intraSongOffsetMs,
                    songDurationMs = songDurationMs,
                    cycleTotalDurationMs = cycleTotalDurationMs,
                    stationTimeMs = currentTimestampMs
                )
            }
            accumulatedMs = songEndMs
        }

        // Fallback to first song in cycle if loop reaches end
        val firstSong = sequence.first()
        return StationBroadcastSlot(
            song = firstSong,
            sequenceIndex = 0,
            intraSongOffsetMs = 0L,
            songDurationMs = (firstSong.durationSeconds.coerceAtLeast(60)) * 1000L,
            cycleTotalDurationMs = cycleTotalDurationMs,
            stationTimeMs = currentTimestampMs
        )
    }
}
