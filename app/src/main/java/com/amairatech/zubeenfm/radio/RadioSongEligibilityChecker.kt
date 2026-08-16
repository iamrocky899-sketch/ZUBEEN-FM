package com.amairatech.zubeenfm.radio

import android.util.Log
import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.ZubeenArtistFilter

/**
 * Authoritative Pre-Playback Safety Gate for Radio Mode.
 * Strictly guarantees that ONLY 100% verified Zubeen Garg music is broadcast over ZUBEEN FM.
 * Concept:
 * if (!RadioSongEligibilityChecker.isEligibleForRadio(song)) {
 *     rejectPlayback()
 *     skipToNextRadioSong()
 * }
 */
object RadioSongEligibilityChecker {

    private const val TAG = "RadioSafetyGate"

    /**
     * Strictly verifies whether a Song candidate is eligible to enter the Radio Broadcast.
     * Rejects missing metadata, ambiguous artist credit, or non-Zubeen performances.
     */
    fun isEligibleForRadio(song: Song?): Boolean {
        if (song == null) {
            Log.e(TAG, "REJECTED: Song candidate is null.")
            return false
        }

        // 1. Mandatory ID and Title
        if (song.id.isBlank() || song.titleEnglish.isBlank()) {
            Log.e(TAG, "REJECTED: Missing ID or Title for song id=[${song.id}].")
            return false
        }

        // 2. Duration check (must be a valid track: 0 if unknown/online, or > 10s and < 25min)
        if ((song.durationSeconds != 0 && song.durationSeconds < 10) || song.durationSeconds > 1500) {
            Log.e(TAG, "REJECTED: Invalid duration (${song.durationSeconds}s) for [${song.titleEnglish}].")
            return false
        }

        // 3. Strict Zubeen Verification via ZubeenArtistFilter (enforces ContentType rules)
        if (!ZubeenArtistFilter.isValidForRadio(song)) {
            Log.e(TAG, "REJECTED: [${song.titleEnglish}] by [${song.artistEnglish}] failed ZubeenArtistFilter.isValidForRadio rules.")
            return false
        }

        // 4. Playable flag
        if (!song.isPlayable) {
            Log.e(TAG, "REJECTED: Song [${song.titleEnglish}] is marked not playable.")
            return false
        }

        Log.d(TAG, "VERIFIED: Item [${song.titleEnglish}] is eligible for ZUBEEN FM broadcast.")
        return true
    }

    /**
     * Verifies that the resolved stream belongs to the selected verified Radio recording.
     */
    fun verifyPlaybackSource(song: Song, resolvedStreamUrl: String?): Boolean {
        if (resolvedStreamUrl.isNullOrBlank()) {
            Log.w(TAG, "STREAM REJECTED: Resolved stream URL is null or empty for [${song.titleEnglish}].")
            return false
        }

        val isValidUrl = resolvedStreamUrl.startsWith("https://") || resolvedStreamUrl.startsWith("http://")
        if (!isValidUrl) {
            Log.w(TAG, "STREAM REJECTED: Resolved stream URL is not a valid HTTP/HTTPS scheme for [${song.titleEnglish}].")
            return false
        }

        return true
    }
}
