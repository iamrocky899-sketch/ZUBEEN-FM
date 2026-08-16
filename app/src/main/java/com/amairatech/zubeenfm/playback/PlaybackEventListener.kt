package com.amairatech.zubeenfm.playback

/**
 * Bridge interface between the background Media3 session and the ViewModel.
 */
interface PlaybackEventListener {
    fun onPlayPauseRequested()
    fun onNextRequested()
    fun onPreviousRequested()
    fun onSeekRequested(positionMs: Long)
    fun onStopRequested()
}
