package com.amairatech.zubeenfm.data.provider

/**
 * Represents a distinct playback source from a specific provider.
 * Supports static direct stream URLs as well as dynamic stream resolution (with expiry).
 */
data class PlaybackSource(
    val providerId: String,
    val sourceId: String,
    val streamUrl: String? = null,
    val format: String = "audio/mp3",
    val bitrateKbps: Int = 320,
    val isDirectStream: Boolean = true,
    val isPlayable: Boolean = true,
    val qualityLabel: String = "HD Audio",
    val expiryTimestampMs: Long = 0L
)
