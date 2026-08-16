package com.amairatech.zubeenfm

import com.amairatech.zubeenfm.data.provider.YouTubeMusicProvider
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FullDiscoveryTest {

    @Test
    fun testFullDiscoveryCount() = runBlocking {
        val provider = YouTubeMusicProvider()
        println("Starting full discovery test...")
        val songs = provider.discoverCompleteAssameseMusic()
        println("--------------------------------------")
        println("FINAL UNIQUE ASSAMESE SONG COUNT: ${songs.size}")
        println("--------------------------------------")
    }
}
