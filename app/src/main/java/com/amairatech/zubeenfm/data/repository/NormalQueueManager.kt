package com.amairatech.zubeenfm.data.repository

import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.ui.NormalRepeatMode

/**
 * Authoritative Queue & Deck Manager for Normal Mode (Assamese Music Library).
 * Implements:
 * 1. Shuffled-bag/deck approach:
 *    - Creates a full shuffled permutation of the active catalogue.
 *    - Traverses each track in the deck once before repeating.
 *    - Avoids immediate repeats.
 *    - When current shuffled deck is exhausted and Repeat is ALL:
 *      generates a new shuffled deck ensuring the first song != previous last song (if size > 1).
 * 2. 3-state Repeat modes: OFF, ALL, ONE.
 * 3. Independent state persisted for Normal Mode without affecting Radio Mode.
 */
class NormalQueueManager {

    private val lock = Any()

    private var activePlaylist: List<Song> = emptyList()
    private var shuffledDeck: List<Song> = emptyList()
    private var currentDeckIndex: Int = -1
    private val playedHistory: MutableList<Song> = mutableListOf()

    var isShuffleEnabled: Boolean = false
        private set

    var repeatMode: NormalRepeatMode = NormalRepeatMode.OFF
        private set

    fun setShuffleEnabled(enabled: Boolean, currentSong: Song?) {
        synchronized(lock) {
            val stateChanged = isShuffleEnabled != enabled
            isShuffleEnabled = enabled
            if (enabled && (stateChanged || shuffledDeck.isEmpty())) {
                rebuildShuffledDeck(currentSong)
            }
        }
    }

    fun setRepeatMode(mode: NormalRepeatMode) {
        synchronized(lock) {
            repeatMode = mode
        }
    }

    fun updatePlaylist(songs: List<Song>, currentSong: Song?) {
        synchronized(lock) {
            activePlaylist = songs
            if (isShuffleEnabled) {
                rebuildShuffledDeck(currentSong)
            }
        }
    }

    fun onSongSelected(song: Song) {
        synchronized(lock) {
            playedHistory.add(song)
            if (isShuffleEnabled) {
                val indexInDeck = shuffledDeck.indexOfFirst { it.id == song.id }
                if (indexInDeck != -1) {
                    currentDeckIndex = indexInDeck
                } else {
                    // Song not in deck, insert or rebuild around it
                    rebuildShuffledDeck(song)
                }
            }
        }
    }

    /**
     * Determines the next song to play based on current song, shuffle deck, and repeat mode.
     * Returns null if end of playlist is reached in Repeat OFF mode.
     */
    fun getNextSong(currentSong: Song?): Song? {
        synchronized(lock) {
            if (activePlaylist.isEmpty()) return null

            // 1. Repeat ONE: always repeat current song
            if (repeatMode == NormalRepeatMode.ONE && currentSong != null) {
                return currentSong
            }

            // 2. Shuffle Mode (Shuffled Bag / Deck)
            if (isShuffleEnabled) {
                if (shuffledDeck.isEmpty()) {
                    rebuildShuffledDeck(currentSong)
                }

                val nextIndex = currentDeckIndex + 1
                if (nextIndex < shuffledDeck.size) {
                    currentDeckIndex = nextIndex
                    return shuffledDeck[nextIndex]
                } else {
                    // Current deck is exhausted!
                    if (repeatMode == NormalRepeatMode.ALL) {
                        // Create new shuffled deck ensuring first song is not the same as the exhausted deck's last song
                        val lastPlayedSong = if (shuffledDeck.isNotEmpty()) shuffledDeck.last() else currentSong
                        createNewShuffledDeckExcludingFirst(lastPlayedSong)
                        currentDeckIndex = 0
                        return shuffledDeck.firstOrNull()
                    } else {
                        // Repeat OFF: end of shuffled deck reached
                        return null
                    }
                }
            }

            // 3. Normal Sequential Mode
            val currentIndex = if (currentSong != null) activePlaylist.indexOfFirst { it.id == currentSong.id } else -1
            if (currentIndex == -1) {
                return activePlaylist.firstOrNull()
            }

            val nextIndex = currentIndex + 1
            if (nextIndex < activePlaylist.size) {
                return activePlaylist[nextIndex]
            } else {
                // Reached end of sequential list
                return if (repeatMode == NormalRepeatMode.ALL) {
                    activePlaylist.firstOrNull()
                } else {
                    null
                }
            }
        }
    }

    /**
     * Determines the previous song to play.
     */
    fun getPreviousSong(currentSong: Song?): Song? {
        synchronized(lock) {
            if (activePlaylist.isEmpty()) return null

            if (isShuffleEnabled) {
                if (currentDeckIndex > 0 && currentDeckIndex - 1 < shuffledDeck.size) {
                    currentDeckIndex--
                    return shuffledDeck[currentDeckIndex]
                }
                // At beginning of deck, return first song or previous from history
                return shuffledDeck.firstOrNull() ?: activePlaylist.firstOrNull()
            }

            val currentIndex = if (currentSong != null) activePlaylist.indexOfFirst { it.id == currentSong.id } else -1
            if (currentIndex <= 0) {
                return activePlaylist.lastOrNull()
            }
            return activePlaylist[currentIndex - 1]
        }
    }

    /**
     * Builds a shuffled deck placing currentSong at index 0 and shuffling remaining songs.
     */
    private fun rebuildShuffledDeck(currentSong: Song?) {
        if (activePlaylist.isEmpty()) {
            shuffledDeck = emptyList()
            currentDeckIndex = -1
            return
        }

        val remaining = if (currentSong != null) {
            activePlaylist.filter { it.id != currentSong.id }.shuffled()
        } else {
            activePlaylist.shuffled()
        }

        shuffledDeck = if (currentSong != null) {
            listOf(currentSong) + remaining
        } else {
            remaining
        }
        currentDeckIndex = 0
    }

    /**
     * Creates a new shuffled deck when the previous one is exhausted.
     * Ensures the first track of the new deck != previousLastSong (if playlist.size > 1).
     */
    private fun createNewShuffledDeckExcludingFirst(previousLastSong: Song?) {
        if (activePlaylist.isEmpty()) {
            shuffledDeck = emptyList()
            return
        }

        if (activePlaylist.size == 1) {
            shuffledDeck = activePlaylist.toList()
            return
        }

        var newDeck = activePlaylist.shuffled()
        // If the new deck begins with the same song that just finished, swap with another item
        if (previousLastSong != null && newDeck.first().id == previousLastSong.id && newDeck.size > 1) {
            val mutable = newDeck.toMutableList()
            val swapIndex = 1 + (mutable.size - 1) / 2
            val temp = mutable[0]
            mutable[0] = mutable[swapIndex]
            mutable[swapIndex] = temp
            newDeck = mutable
        }

        shuffledDeck = newDeck
    }

    /**
     * Visible for testing to verify deck state.
     */
    fun getShuffledDeckForTesting(): List<Song> {
        synchronized(lock) {
            return shuffledDeck.toList()
        }
    }

    fun getCurrentDeckIndexForTesting(): Int {
        synchronized(lock) {
            return currentDeckIndex
        }
    }
}
