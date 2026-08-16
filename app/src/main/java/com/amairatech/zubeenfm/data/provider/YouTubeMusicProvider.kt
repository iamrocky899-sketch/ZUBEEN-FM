package com.amairatech.zubeenfm.data.provider

import android.util.Log
import com.amairatech.zubeenfm.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.StreamInfo
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Real YouTube Music & Audio Network Provider for ZUBEEN FM.
 * Provides separate discovery pipelines:
 * 1. discoverAssameseMusic(): Deep multi-batch discovery across all Assamese eras (1980s -> current) & artists.
 * 2. discoverZubeenMusic(): Deep multi-batch discovery strictly for 100% Zubeen Garg recordings.
 * 3. resolveStreamUrl(): Ultra-fast stream resolution with 4-hour caching & preloading.
 */
class YouTubeMusicProvider : MusicProvider {

    override val providerId: String = "youtube_music_provider"
    override val providerName: String = "YouTube Music & Audio Network"
    override val canSearch: Boolean = true
    override val canProvideMetadata: Boolean = true
    override val canProvideArtwork: Boolean = true
    override val canProvidePlayback: Boolean = true
    override val supportsPagination: Boolean = true

    companion object {
        private val isNewPipeInitialized = AtomicBoolean(false)
        // Global stream cache: videoId -> Pair(streamUrl, expirationTimestamp)
        private val resolvedStreamCache = ConcurrentHashMap<String, Pair<String, Long>>()

        private class RobustDownloader : Downloader() {
            private val cookieManager = CookieManager().apply {
                setCookiePolicy(CookiePolicy.ACCEPT_ALL)
            }

            init {
                CookieHandler.setDefault(cookieManager)
            }

            override fun execute(request: Request): Response {
                val url = URL(request.url())
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = request.httpMethod()
                conn.connectTimeout = 12000
                conn.readTimeout = 12000
                conn.instanceFollowRedirects = true

                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9")

                for ((key, values) in request.headers()) {
                    for (v in values) {
                        conn.addRequestProperty(key, v)
                    }
                }

                val dataToSend = request.dataToSend()
                if (dataToSend != null && dataToSend.isNotEmpty()) {
                    conn.doOutput = true
                    conn.outputStream.use { it.write(dataToSend) }
                }

                val code = conn.responseCode
                val isr = InputStreamReader(if (code in 200..299) conn.inputStream else conn.errorStream)
                val body = BufferedReader(isr).use { it.readText() }

                val responseHeaders = mutableMapOf<String, List<String>>()
                for ((k, v) in conn.headerFields) {
                    if (k != null) {
                        responseHeaders[k] = v
                    }
                }

                return Response(code, conn.responseMessage ?: "OK", responseHeaders, body, conn.url.toString())
            }
        }

        fun initExtractor() {
            if (isNewPipeInitialized.compareAndSet(false, true)) {
                try {
                    NewPipe.init(RobustDownloader(), Localization("en", "US"), ContentCountry("US"))
                } catch (t: Throwable) {
                    // Log error in non-performance path
                    Log.e("ZubeenPlayback", "Error initializing NewPipeExtractor: ${t.message}")
                }
            }
        }

        fun getCachedStreamUrl(videoId: String): String? {
            val cached = resolvedStreamCache[videoId]
            val now = System.currentTimeMillis()
            return if (cached != null && cached.second > now) cached.first else null
        }

        // Multi-dimensional queries for Normal Mode covering all eras, genres & artists
        private val ERAS = listOf("80s", "90s", "2000s", "2010s", "2020s", "latest")
        private val CATEGORIES = listOf("Bihu", "Folk", "Film", "Romantic", "Modern", "Devotional", "Classical")
        private val KNOWN_ARTISTS = listOf(
            "Zubeen Garg", "Bhupen Hazarika", "Jayanta Hazarika", "Dipali Barthakur", "Pratima Barua Pandey",
            "Khagen Mahanta", "Archana Mahanta", "Papon", "Jitul Sonowal", "Bornali Kalita", "Vitali Das",
            "Zublee Baruah", "Neel Akash", "Deeplina Deka", "Babu Baruah", "Achurjya Borpatra", "Shankuraj Konwar",
            "Montumoni Saikia", "Vreegu Kashyap", "Rupam Bhuyan", "Maitrayee Patar", "Priyanka Bharali",
            "Kusum Kailash", "Dwipen Baruah", "Pulak Banerjee", "Ridip Dutta", "Samar Hazarika", "Anima Choudhury",
            "Subasana Dutta", "Dikshu Sarma", "Simanta Shekhar", "Sadananda Gogoi", "Anindita Paul", "Tarali Sarma",
            "Joy Barua", "Krishna Mani Chutiya"
        )

        private val ASSAMESE_QUERY_BATCHES: List<List<String>> = run {
            val queries = mutableListOf<String>()
            ERAS.forEach { era -> queries.add("Assamese $era hits") }
            CATEGORIES.forEach { cat -> queries.add("Assamese $cat songs") }
            KNOWN_ARTISTS.forEach { artist -> queries.add("$artist Assamese songs") }
            queries.chunked(4)
        }

        // Multi-dimensional queries for Radio Mode strictly covering Zubeen Garg catalogue
        private val ZUBEEN_QUERY_BATCHES: List<List<String>> = run {
            val queries = mutableListOf(
                "Zubeen Garg Assamese hits", "Zubeen Garg Maya Anamika", "Zubeen Garg Mon Jaai",
                "Zubeen Garg 1990s Assamese songs", "Zubeen Garg 2000s Assamese album", "Zubeen Garg Mukuta",
                "Zubeen Garg 2010s Assamese songs", "Zubeen Garg 2020s Assamese hits", "Zubeen Garg new Assamese song",
                "Zubeen Garg film soundtrack Assamese", "Zubeen Garg Bihu hits", "Zubeen Garg Hiya Diya Niya",
                "Zubeen Garg Borgeet Lokageet", "Zubeen Garg Acoustic Unplugged", "Zubeen Garg evergreen Assamese classics",
                "Zubeen Garg Hindi songs hits", "Zubeen Garg Bengali songs hits", "Zubeen Garg Nepali songs hits"
            )
            queries.chunked(4)
        }

        private val NEW_RELEASES_QUERIES = listOf(
            "latest Assamese songs",
            "new Assamese songs",
            "new Assamese releases",
            "Assamese new songs 2026",
            "latest Assamese music",
            "latest Assamese Bihu songs",
            "new Assamese artists",
            "new Assamese film songs",
            "latest Assamese romantic songs",
            "latest Assamese folk songs"
        )
    }

    private val assameseTracksCache = mutableListOf<Song>()
    private var assameseLoadedBatchIndex = 0

    private val zubeenTracksCache = mutableListOf<Song>()
    private var zubeenLoadedBatchIndex = 0

    private val newReleasesCache = mutableListOf<Song>()
    private var newReleasesBatchIndex = 0

    private class DiscoveryStats(
        var queriesProcessed: Int = 0,
        var pagesProcessed: Int = 0,
        var rawResultsFound: Int = 0,
        var acceptedCount: Int = 0,
        var rejectedCount: Int = 0,
        var duplicateCount: Int = 0
    ) {
        override fun toString(): String {
            return "Discovery Stats: Queries=$queriesProcessed, Pages=$pagesProcessed, Raw=$rawResultsFound, Accepted=$acceptedCount, Rejected=$rejectedCount, Duplicates=$duplicateCount"
        }
    }

    override suspend fun discoverNewAssameseReleases(page: Int, pageSize: Int): ProviderPageResult = withContext(Dispatchers.IO) {
        val stats = DiscoveryStats()
        while (newReleasesCache.size < (page * pageSize) && newReleasesBatchIndex < NEW_RELEASES_QUERIES.size) {
            val query = NEW_RELEASES_QUERIES[newReleasesBatchIndex]
            try {
                val results = fetchEveryPageForQuery(query, stats)
                for (song in results) {
                    if (AssameseMusicFilter.isValidForNormalCatalogue(song)) {
                        if (newReleasesCache.none { it.id == song.id }) {
                            newReleasesCache.add(song.copy(releaseTimestamp = System.currentTimeMillis()))
                            stats.acceptedCount++
                        } else {
                            stats.duplicateCount++
                        }
                    } else {
                        stats.rejectedCount++
                    }
                }
            } catch (e: Exception) {
                Log.w("ZubeenPlayback", "New release query '$query' failed: ${e.message}")
            }
            newReleasesBatchIndex++
            delay(300L)
        }

        val deduplicatedPool = DuplicateResolver.deduplicateSongs(newReleasesCache)
        val startIndex = (page - 1) * pageSize
        if (startIndex >= deduplicatedPool.size || startIndex < 0) {
            return@withContext ProviderPageResult(emptyList(), page, newReleasesBatchIndex < NEW_RELEASES_QUERIES.size, deduplicatedPool.size)
        }

        val endIndex = (startIndex + pageSize).coerceAtMost(deduplicatedPool.size)
        return@withContext ProviderPageResult(deduplicatedPool.subList(startIndex, endIndex), page, endIndex < deduplicatedPool.size || newReleasesBatchIndex < NEW_RELEASES_QUERIES.size, deduplicatedPool.size)
    }

    private suspend fun fetchEveryPageForQuery(query: String, stats: DiscoveryStats, isZubeenOnly: Boolean = false): List<Song> {
        val allSongs = mutableListOf<Song>()
        var continuation: String? = null
        var pageNum = 1
        
        var queryResults = 0
        var queryAccepted = 0
        var queryRejected = 0
        var queryDuplicates = 0

        do {
            val (songs, nextContinuation) = fetchLiveYouTubeMusicSearchResults(query, continuation)
            queryResults += songs.size
            
            for (song in songs) {
                val isValid = if (isZubeenOnly) {
                    ZubeenArtistFilter.isValidZubeenRecording(song.titleEnglish, song.artistEnglish)
                } else {
                    AssameseMusicFilter.isValidForNormalCatalogue(song)
                }

                if (isValid) {
                    if (allSongs.none { it.id == song.id }) {
                        allSongs.add(song)
                        queryAccepted++
                    } else {
                        queryDuplicates++
                    }
                } else {
                    queryRejected++
                }
            }
            
            stats.rawResultsFound += songs.size
            stats.pagesProcessed++
            
            Log.d("ZubeenDiscovery", "Query: '$query' - Page $pageNum processed (${songs.size} results)")
            
            continuation = if (nextContinuation == continuation || nextContinuation.isNullOrBlank()) null else nextContinuation
            pageNum++
            if (continuation != null) delay(100L) // Rate limiting between pages
        } while (continuation != null && songs.isNotEmpty() && pageNum <= 15) // Exhaustive limit
        
        Log.i("ZubeenDiscovery", "Query Stats for '$query': Total=$queryResults, Accepted=$queryAccepted, Rejected=$queryRejected, Duplicates=$queryDuplicates")
        
        stats.queriesProcessed++
        return allSongs
    }

    override suspend fun discoverCompleteAssameseMusic(): List<Song> = coroutineScope {
        val completeList = mutableListOf<Song>()
        val stats = DiscoveryStats()
        Log.i("ZubeenDiscovery", "Starting EXHAUSTIVE Assamese discovery pass across ${ASSAMESE_QUERY_BATCHES.flatten().size} queries...")
        
        for (batch in ASSAMESE_QUERY_BATCHES) {
            val batchResults = batch.map { query ->
                async(Dispatchers.IO) {
                    fetchEveryPageForQuery(query, stats, isZubeenOnly = false)
                }
            }.awaitAll().flatten()
            
            for (song in batchResults) {
                if (completeList.none { it.id == song.id }) {
                    completeList.add(song)
                    stats.acceptedCount++
                } else {
                    stats.duplicateCount++
                }
            }
            delay(400L) // Rate limiting between batches
        }
        
        val deduplicated = DuplicateResolver.deduplicateSongs(completeList)
        Log.i("ZubeenDiscovery", "COMPLETE DISCOVERY FINISHED.")
        Log.i("ZubeenDiscovery", "--------------------------------------")
        Log.i("ZubeenDiscovery", "Queries Processed: ${stats.queriesProcessed}")
        Log.i("ZubeenDiscovery", "Pages Processed: ${stats.pagesProcessed}")
        Log.i("ZubeenDiscovery", "Raw Results Found: ${stats.rawResultsFound}")
        Log.i("ZubeenDiscovery", "Final Unique Count: ${deduplicated.size}")
        Log.i("ZubeenDiscovery", "--------------------------------------")
        return@coroutineScope deduplicated
    }

    override suspend fun discoverAssameseMusic(page: Int, pageSize: Int, query: String?): ProviderPageResult = withContext(Dispatchers.IO) {
        if (!query.isNullOrBlank()) {
            val (liveTracks, _) = fetchLiveYouTubeMusicSearchResults("Assamese $query")
            val filtered = liveTracks.filter {
                AssameseMusicFilter.isValidForNormalCatalogue(it)
            }
            val deduplicated = DuplicateResolver.deduplicateSongs(filtered)
            val startIndex = (page - 1) * pageSize
            if (startIndex >= deduplicated.size || startIndex < 0) {
                return@withContext ProviderPageResult(emptyList(), page, false, deduplicated.size)
            }
            val endIndex = (startIndex + pageSize).coerceAtMost(deduplicated.size)
            return@withContext ProviderPageResult(
                songs = deduplicated.subList(startIndex, endIndex),
                page = page,
                hasMorePages = endIndex < deduplicated.size,
                totalCount = deduplicated.size
            )
        }

        val requiredCount = page * pageSize

        // Load more query batches dynamically as user scrolls
        while (assameseTracksCache.size < requiredCount && assameseLoadedBatchIndex < ASSAMESE_QUERY_BATCHES.size) {
            val currentBatch = ASSAMESE_QUERY_BATCHES[assameseLoadedBatchIndex]

            for (q in currentBatch) {
                val (results, _) = fetchLiveYouTubeMusicSearchResults(q)
                for (song in results) {
                    if (AssameseMusicFilter.isValidForNormalCatalogue(song)) {
                        if (assameseTracksCache.none { it.id == song.id }) {
                            assameseTracksCache.add(song)
                        }
                    }
                }
            }
            assameseLoadedBatchIndex++
        }

        val deduplicatedPool = DuplicateResolver.deduplicateSongs(assameseTracksCache)
        val startIndex = (page - 1) * pageSize
        if (startIndex >= deduplicatedPool.size || startIndex < 0) {
            val hasMore = assameseLoadedBatchIndex < ASSAMESE_QUERY_BATCHES.size
            return@withContext ProviderPageResult(emptyList(), page, hasMore, deduplicatedPool.size)
        }

        val endIndex = (startIndex + pageSize).coerceAtMost(deduplicatedPool.size)
        val paged = deduplicatedPool.subList(startIndex, endIndex)
        val hasMore = endIndex < deduplicatedPool.size || assameseLoadedBatchIndex < ASSAMESE_QUERY_BATCHES.size

        Log.d("ZubeenPlayback", "Normal Mode page $page delivered ${paged.size} songs (Pool size: ${deduplicatedPool.size}, hasMore=$hasMore)")

        return@withContext ProviderPageResult(
            songs = paged,
            page = page,
            hasMorePages = hasMore,
            totalCount = deduplicatedPool.size
        )
    }

    override suspend fun discoverZubeenMusic(page: Int, pageSize: Int): ProviderPageResult = withContext(Dispatchers.IO) {
        val requiredCount = page * pageSize
        val stats = DiscoveryStats()

        while (zubeenTracksCache.size < requiredCount && zubeenLoadedBatchIndex < ZUBEEN_QUERY_BATCHES.size) {
            val currentBatch = ZUBEEN_QUERY_BATCHES[zubeenLoadedBatchIndex]

            for (q in currentBatch) {
                val results = fetchEveryPageForQuery(q, stats, isZubeenOnly = true)
                for (song in results) {
                    if (zubeenTracksCache.none { it.id == song.id }) {
                        zubeenTracksCache.add(song)
                    }
                }
            }
            zubeenLoadedBatchIndex++
            delay(400L)
        }

        val deduplicatedPool = DuplicateResolver.deduplicateSongs(zubeenTracksCache)
        val startIndex = (page - 1) * pageSize
        if (startIndex >= deduplicatedPool.size || startIndex < 0) {
            val hasMore = zubeenLoadedBatchIndex < ZUBEEN_QUERY_BATCHES.size
            return@withContext ProviderPageResult(emptyList(), page, hasMore, deduplicatedPool.size)
        }

        val endIndex = (startIndex + pageSize).coerceAtMost(deduplicatedPool.size)
        val paged = deduplicatedPool.subList(startIndex, endIndex)
        val hasMore = endIndex < deduplicatedPool.size || zubeenLoadedBatchIndex < ZUBEEN_QUERY_BATCHES.size

        Log.d("ZubeenPlayback", "Radio Mode page $page delivered ${paged.size} Zubeen songs (Pool size: ${deduplicatedPool.size}, hasMore=$hasMore)")

        return@withContext ProviderPageResult(
            songs = paged,
            page = page,
            hasMorePages = hasMore,
            totalCount = deduplicatedPool.size
        )
    }

    override suspend fun searchSongs(query: String, page: Int, pageSize: Int): ProviderPageResult = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        val searchQuery = if (trimmed.isEmpty()) "Assamese songs" else "Assamese $trimmed"
        val (liveTracks, _) = fetchLiveYouTubeMusicSearchResults(searchQuery)
        val filtered = liveTracks.filter {
            AssameseMusicFilter.isValidAssameseRecording(it.titleEnglish, it.artistEnglish, it.albumEnglish)
        }
        val deduplicated = DuplicateResolver.deduplicateSongs(filtered)

        val startIndex = (page - 1) * pageSize
        if (startIndex >= deduplicated.size || startIndex < 0) {
            return@withContext ProviderPageResult(emptyList(), page, false, deduplicated.size)
        }

        val endIndex = (startIndex + pageSize).coerceAtMost(deduplicated.size)
        val paged = deduplicated.subList(startIndex, endIndex)
        val hasMore = endIndex < deduplicated.size

        return@withContext ProviderPageResult(
            songs = paged,
            page = page,
            hasMorePages = hasMore,
            totalCount = deduplicated.size
        )
    }

    fun invalidateStream(sourceId: String) {
        val videoId = sourceId.removePrefix("yt_")
        resolvedStreamCache.remove(videoId)
        Log.d("ZubeenBufferDebug", "SOURCE_SWITCH (cache invalidated) videoId=$videoId")
    }

    override suspend fun resolveStreamUrl(source: PlaybackSource): String? = withContext(Dispatchers.IO) {
        val videoId = source.sourceId.removePrefix("yt_")
        if (videoId.isBlank()) {
            Log.w("ZubeenPlayback", "resolveStreamUrl called with blank videoId")
            return@withContext null
        }

        val startTime = System.currentTimeMillis()
        Log.i("ZubeenBufferDebug", "SOURCE_RESOLUTION_START timestamp=$startTime provider=YouTubeMusic videoId=$videoId")

        // 1. Check Fast Cache (Sub-millisecond hit)
        val cached = resolvedStreamCache[videoId]
        val now = System.currentTimeMillis()
        if (cached != null && cached.second > now) {
            Log.i("ZubeenBufferDebug", "SOURCE_RESOLUTION_END timestamp=$now provider=YouTubeMusic videoId=$videoId cacheHit=true deltaMs=${now - startTime}")
            return@withContext cached.first
        }

        // 2. Perform Extractor Resolution
        initExtractor()

        try {
            val service = ServiceList.YouTube
            val streamInfo = StreamInfo.getInfo(service, "https://www.youtube.com/watch?v=$videoId")
            val audioStreams = streamInfo.audioStreams

            // Mobile-Optimized Audio Stream Selection:
            // 1. Opus ~160kbps / 128kbps (high perceptual fidelity, low data usage)
            // 2. AAC / M4A ~128-160kbps
            // 3. Other good-quality streams >= 96kbps
            // 4. Lowest fallback only when necessary
            val bestAudio: AudioStream? = audioStreams
                .filter { it.averageBitrate in 96..192 }
                .sortedWith(
                    compareByDescending<AudioStream> { it.format?.name?.contains("Opus", ignoreCase = true) == true }
                        .thenByDescending { it.format?.name?.contains("m4a", ignoreCase = true) == true || it.format?.name?.contains("aac", ignoreCase = true) == true }
                        .thenByDescending { it.averageBitrate }
                )
                .firstOrNull()
                ?: audioStreams.maxByOrNull { it.averageBitrate }

            val audioUrl = bestAudio?.url
            val endTime = System.currentTimeMillis()

            if (!audioUrl.isNullOrBlank() && bestAudio != null) {
                val codec = if (bestAudio.format?.name?.contains("Opus", ignoreCase = true) == true) "opus" else "aac"
                val container = bestAudio.format?.suffix ?: "webm"
                val bitrate = bestAudio.averageBitrate

                Log.i("ZubeenQuality", "codec=$codec bitrate=${bitrate}kbps container=$container")
                Log.i("ZubeenBufferDebug", "SOURCE_RESOLUTION_END timestamp=$endTime provider=YouTubeMusic videoId=$videoId codec=$codec bitrate=${bitrate}kbps deltaMs=${endTime - startTime}")

                // Dynamic expiry extraction from URL query parameter
                val expireParamSec = try {
                    android.net.Uri.parse(audioUrl).getQueryParameter("expire")?.toLongOrNull()
                } catch (e: Exception) {
                    null
                }
                val expiryMs = if (expireParamSec != null && expireParamSec > 0) {
                    (expireParamSec * 1000L) - (15 * 60 * 1000L) // 15-minute safety buffer before token expiration
                } else {
                    now + (3 * 60 * 60 * 1000L)
                }

                resolvedStreamCache[videoId] = Pair(audioUrl, expiryMs)
                return@withContext audioUrl
            } else {
                Log.w("ZubeenPlayback", "No suitable audio streams found for $videoId")
            }
        } catch (t: Throwable) {
            Log.e("ZubeenPlayback", "Failed to resolve stream for $videoId: ${t.message}", t)
        }

        return@withContext null
    }

    suspend fun preloadStreamUrl(source: PlaybackSource) = withContext(Dispatchers.IO) {
        try {
            val videoId = source.sourceId.removePrefix("yt_")
            val cached = resolvedStreamCache[videoId]
            if (cached == null || cached.second <= System.currentTimeMillis()) {
                Log.d("ZubeenPlayback", "Preloading next song stream for videoId=$videoId in background")
                resolveStreamUrl(source)
            }
        } catch (e: Exception) {
            // Background preloading ignore
        }
    }

    private val searchQueryCache = ConcurrentHashMap<String, Pair<List<Song>, Long>>()

    private fun fetchLiveYouTubeMusicSearchResults(query: String, continuation: String? = null): Pair<List<Song>, String?> {
        val trimmedQuery = query.trim().lowercase()
        if (continuation == null) {
            val cached = searchQueryCache[trimmedQuery]
            val now = System.currentTimeMillis()
            if (cached != null && cached.second > now) {
                return Pair(cached.first, null)
            }
        }

        val results = mutableListOf<Song>()
        var nextContinuation: String? = null
        try {
            val searchUrl = URL("https://music.youtube.com/youtubei/v1/search?alt=json")
            val conn = searchUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
            conn.setRequestProperty("Origin", "https://music.youtube.com")
            conn.setRequestProperty("X-YouTube-Client-Name", "67")
            conn.setRequestProperty("X-YouTube-Client-Version", "1.20240101.01.00")
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.doOutput = true

            val requestJson = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "WEB_REMIX")
                        put("clientVersion", "1.20240101.01.00")
                        put("hl", "en")
                        put("gl", "IN")
                    })
                })
                if (continuation != null) {
                    put("continuation", continuation)
                } else {
                    put("query", query)
                    // Force "Songs" category for focused results
                    put("params", "EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D")
                }
            }

            OutputStreamWriter(conn.outputStream).use { it.write(requestJson.toString()) }

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                return Pair(emptyList(), null)
            }

            val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            val rootJson = JSONObject(responseText)
            
            // Extract items
            val extractedItems = mutableListOf<Map<String, String>>()
            findResponsiveItems(rootJson, extractedItems)
            
            // Extract continuation token
            nextContinuation = findContinuationToken(rootJson)

            for (map in extractedItems) {
                val rawTitle = map["title"] ?: ""
                val rawArtist = map["artist"] ?: "Assamese Artist"
                val rawAlbum = map["album"] ?: "Single"
                val durationStr = map["duration"] ?: ""
                val videoId = map["videoId"] ?: ""
                val artworkUrl = map["thumbnail"]
                val rawYear = map["year"] ?: "Online"

                if (videoId.isNotBlank() && rawTitle.isNotBlank()) {
                    val cleanedTitle = MetadataNormalizer.cleanTitle(rawTitle)
                    val origLang = MetadataNormalizer.detectOriginalLanguage(cleanedTitle, rawAlbum, rawArtist)
                    val contentType = MetadataNormalizer.detectContentType(cleanedTitle, rawArtist, "")
                    val lang = MetadataNormalizer.getLanguageAssameseLabel(origLang)
                    val genre = MetadataNormalizer.inferGenre(cleanedTitle, rawAlbum, lang)
                    val durationSecs = parseDurationToSeconds(durationStr)
                    val timestamp = MetadataNormalizer.parseReleaseYearToTimestamp(rawYear)

                    results.add(
                        Song(
                            id = "yt_$videoId",
                            titleAssamese = cleanedTitle,
                            titleEnglish = cleanedTitle,
                            albumAssamese = rawAlbum,
                            albumEnglish = rawAlbum,
                            artistAssamese = rawArtist,
                            artistEnglish = rawArtist,
                            durationSeconds = durationSecs,
                            genreAssamese = genre,
                            languageAssamese = lang,
                            originalLanguage = origLang,
                            releaseYear = rawYear,
                            releaseTimestamp = timestamp,
                            accentColorHex = 0xFFD84315,
                            streamUrl = null,
                            artworkUrl = artworkUrl,
                            isPlayable = true,
                            playbackSources = listOf(
                                PlaybackSource(
                                    providerId = providerId,
                                    sourceId = videoId,
                                    streamUrl = null,
                                    qualityLabel = "YouTube Music High"
                                )
                            ),
                            contentType = contentType
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("ZubeenPlayback", "Error in fetchLiveYouTubeMusicSearchResults: ${e.message}", e)
        }
        if (results.isNotEmpty() && continuation == null) {
            searchQueryCache[trimmedQuery] = Pair(results, System.currentTimeMillis() + (60 * 60 * 1000L))
        }
        return Pair(results, nextContinuation)
    }

    private fun findContinuationToken(node: Any?): String? {
        when (node) {
            is JSONObject -> {
                if (node.has("continuation")) {
                    return node.optString("continuation")
                }
                if (node.has("nextContinuationData")) {
                    return node.getJSONObject("nextContinuationData").optString("continuation")
                }
                val keys = node.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val result = findContinuationToken(node.opt(key))
                    if (result != null) return result
                }
            }
            is JSONArray -> {
                for (i in 0 until node.length()) {
                    val result = findContinuationToken(node.opt(i))
                    if (result != null) return result
                }
            }
        }
        return null
    }

    private fun findResponsiveItems(node: Any?, results: MutableList<Map<String, String>>) {
        when (node) {
            is JSONObject -> {
                if (node.has("musicResponsiveListItemRenderer")) {
                    val item = node.getJSONObject("musicResponsiveListItemRenderer")
                    val parsed = parseResponsiveItem(item)
                    if (parsed != null) {
                        results.add(parsed)
                    }
                } else {
                    val keys = node.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        findResponsiveItems(node.opt(key), results)
                    }
                }
            }
            is JSONArray -> {
                for (i in 0 until node.length()) {
                    findResponsiveItems(node.opt(i), results)
                }
            }
        }
    }

    private fun parseResponsiveItem(item: JSONObject): Map<String, String>? {
        var videoId = item.optJSONObject("playlistItemData")?.optString("videoId")
        if (videoId.isNullOrBlank()) {
            val overlay = item.optJSONObject("overlay")?.optJSONObject("musicItemThumbnailOverlayRenderer")
            val playBtn = overlay?.optJSONObject("content")?.optJSONObject("musicPlayButtonRenderer")
            videoId = playBtn?.optJSONObject("playNavigationEndpoint")?.optJSONObject("watchEndpoint")?.optString("videoId")
        }

        val flexColumns = item.optJSONArray("flexColumns") ?: return null
        if (flexColumns.length() == 0) return null

        val col0Runs = flexColumns.optJSONObject(0)
            ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
            ?.optJSONObject("text")
            ?.optJSONArray("runs")
        val title = col0Runs?.optJSONObject(0)?.optString("text") ?: ""

        if (videoId.isNullOrBlank()) {
            videoId = col0Runs?.optJSONObject(0)?.optJSONObject("navigationEndpoint")?.optJSONObject("watchEndpoint")?.optString("videoId")
        }

        var artist = "Assamese Artist"
        var album = "Single"
        var duration = "3:30"
        var year = "Online"

        if (flexColumns.length() > 1) {
            val col1Runs = flexColumns.optJSONObject(1)
                ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")
                ?.optJSONArray("runs")
            if (col1Runs != null) {
                val runTexts = mutableListOf<String>()
                for (i in 0 until col1Runs.length()) {
                    val t = col1Runs.optJSONObject(i)?.optString("text") ?: ""
                    if (t.isNotBlank() && t != " • ") {
                        runTexts.add(t)
                    }
                }
                // Order in YTM: [Artist, Album, Views, Duration] or [Artist, Views, Duration]
                if (runTexts.isNotEmpty()) artist = runTexts[0]
                if (runTexts.size > 1) {
                    val second = runTexts[1]
                    if (second.any { it.isDigit() } && second.length == 4) {
                        year = second
                    } else {
                        album = second
                    }
                }
                if (runTexts.size > 2) {
                    val third = runTexts[2]
                    if (third.any { it.isDigit() } && third.length == 4) {
                        year = third
                    }
                }
                duration = runTexts.last()
            }
        }

        var thumbnail: String? = null
        val thumbnails = item.optJSONObject("thumbnail")
            ?.optJSONObject("musicThumbnailRenderer")
            ?.optJSONObject("thumbnail")
            ?.optJSONArray("thumbnails")
        if (thumbnails != null && thumbnails.length() > 0) {
            thumbnail = thumbnails.getJSONObject(thumbnails.length() - 1).optString("url")
        }

        if (title.isBlank() || videoId.isNullOrBlank()) return null

        return mapOf(
            "title" to title,
            "artist" to artist,
            "album" to album,
            "duration" to duration,
            "videoId" to videoId,
            "thumbnail" to (thumbnail ?: ""),
            "year" to year
        )
    }

    private fun parseDurationToSeconds(durationStr: String): Int {
        if (durationStr.isBlank()) return 210
        val parts = durationStr.split(":")
        return try {
            if (parts.size == 2) {
                parts[0].trim().toInt() * 60 + parts[1].trim().toInt()
            } else if (parts.size == 3) {
                parts[0].trim().toInt() * 3600 + parts[1].trim().toInt() * 60 + parts[2].trim().toInt()
            } else {
                210
            }
        } catch (e: Exception) {
            210
        }
    }
}
