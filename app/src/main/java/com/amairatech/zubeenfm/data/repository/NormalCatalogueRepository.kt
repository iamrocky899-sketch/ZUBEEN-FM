package com.amairatech.zubeenfm.data.repository

import com.amairatech.zubeenfm.data.model.Album
import com.amairatech.zubeenfm.data.model.Artist
import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.AssameseMusicFilter
import com.amairatech.zubeenfm.data.provider.PlaybackSource
import com.amairatech.zubeenfm.data.provider.ProviderManager
import com.amairatech.zubeenfm.data.provider.ZubeenArtistFilter
import com.amairatech.zubeenfm.ui.NormalRepeatMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Repository for Normal Mode — Assamese Music Library.
 * Manages the broad Assamese music collection (1980s -> current, all Assamese artists,
 * genres: Bihu, Folk, Modern, Film, Romantic, Devotional, etc.),
 * plus Albums, Artists, independent Normal Mode queue, recently played history, favorites, shuffle, and repeat.
 */
object NormalCatalogueRepository {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    val providerManager = ProviderManager()
    private const val CACHE_FILE_NAME = "normal_catalogue_cache.json"

    // Seed tracks representing rich Assamese music history
    private val initialSeedSongs: List<Song> = listOf(
        Song(
            id = "yt_6QW7CHoLpos",
            titleAssamese = "মায়াবিনী",
            titleEnglish = "Mayabini",
            albumAssamese = "মায়া",
            albumEnglish = "Maya",
            artistAssamese = "জুবিন গাৰ্গ",
            artistEnglish = "Zubeen Garg",
            durationSeconds = 322,
            genreAssamese = "আধুনিক সুৰীয়া",
            languageAssamese = "অসমীয়া",
            originalLanguage = "ASSAMESE",
            releaseYear = "2001",
            accentColorHex = 0xFFD84315,
            streamUrl = null,
            isPlayable = true,
            playbackSources = listOf(
                PlaybackSource(
                    providerId = "youtube_music_provider",
                    sourceId = "6QW7CHoLpos",
                    streamUrl = null,
                    qualityLabel = "YouTube Music High"
                )
            )
        ),
        Song(
            id = "yt_v3BihuClassic",
            titleAssamese = "বিহুৰে বতৰত",
            titleEnglish = "Bihure Botorot",
            albumAssamese = "বাপতিসাহোন বিহু",
            albumEnglish = "Baputisahon Bihu",
            artistAssamese = "খগেন মহন্ত",
            artistEnglish = "Khagen Mahanta",
            durationSeconds = 245,
            genreAssamese = "বিহু আৰু লোকসংগীত",
            languageAssamese = "অসমীয়া",
            originalLanguage = "ASSAMESE",
            releaseYear = "1988",
            accentColorHex = 0xFF2E7D32,
            streamUrl = null,
            isPlayable = true,
            playbackSources = listOf(
                PlaybackSource(
                    providerId = "youtube_music_provider",
                    sourceId = "6QW7CHoLpos",
                    streamUrl = null,
                    qualityLabel = "High Quality Audio"
                )
            )
        ),
        Song(
            id = "yt_paponBorgeet",
            titleAssamese = "মণিহাৰা",
            titleEnglish = "Monihara",
            albumAssamese = "পাপন স্পেচিয়েল",
            albumEnglish = "Papon Special",
            artistAssamese = "পাপন",
            artistEnglish = "Papon",
            durationSeconds = 280,
            genreAssamese = "চিৰসেউজ মেল'ডী",
            languageAssamese = "অসমীয়া",
            originalLanguage = "ASSAMESE",
            releaseYear = "2012",
            accentColorHex = 0xFF1565C0,
            streamUrl = null,
            isPlayable = true,
            playbackSources = listOf(
                PlaybackSource(
                    providerId = "youtube_music_provider",
                    sourceId = "6QW7CHoLpos",
                    streamUrl = null,
                    qualityLabel = "High Quality Audio"
                )
            )
        )
    )

    private val _songsFlow = MutableStateFlow<List<Song>>(initialSeedSongs)
    val songsFlow: StateFlow<List<Song>> = _songsFlow.asStateFlow()

    private val _newReleasesFlow = MutableStateFlow<List<Song>>(emptyList())
    val newReleasesFlow: StateFlow<List<Song>> = _newReleasesFlow.asStateFlow()

    // Independent Normal Mode Queue & History
    val queueManager = NormalQueueManager()

    private val _recentlyPlayedFlow = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayedFlow: StateFlow<List<Song>> = _recentlyPlayedFlow.asStateFlow()

    private val _favoritesFlow = MutableStateFlow<Set<String>>(emptySet())
    val favoritesFlow: StateFlow<Set<String>> = _favoritesFlow.asStateFlow()

    val playlist: List<Song>
        get() = _songsFlow.value

    init {
        queueManager.updatePlaylist(initialSeedSongs, initialSeedSongs.first())

        // Load from local cache immediately on startup
        val cachedSongs = loadCatalogueFromCache()
        if (cachedSongs.isNotEmpty()) {
            _songsFlow.value = cachedSongs
            queueManager.updatePlaylist(cachedSongs, null)
        }

        scope.launch {
            providerManager.normalCatalogue.collect { list ->
                if (list.isNotEmpty()) {
                    _songsFlow.value = list
                    queueManager.updatePlaylist(list, null)
                    saveCatalogueToCache(list)
                }
            }
        }

        scope.launch {
            providerManager.newReleases.collect { list ->
                _newReleasesFlow.value = list
            }
        }
        
        // Start background synchronization on app open
        scope.launch {
            providerManager.loadCompleteNormalCatalogue()
        }
    }

    private fun saveCatalogueToCache(songs: List<Song>) {
        try {
            val app = try { com.amairatech.zubeenfm.ZubeenApplication.instance } catch (_: Exception) { null }
            if (app == null) return
            val file = File(app.filesDir, CACHE_FILE_NAME)
            val jsonArray = JSONArray()
            for (song in songs) {
                val jsonObj = JSONObject().apply {
                    put("id", song.id)
                    put("titleAssamese", song.titleAssamese)
                    put("titleEnglish", song.titleEnglish)
                    put("albumAssamese", song.albumAssamese)
                    put("albumEnglish", song.albumEnglish)
                    put("artistAssamese", song.artistAssamese)
                    put("artistEnglish", song.artistEnglish)
                    put("durationSeconds", song.durationSeconds)
                    put("genreAssamese", song.genreAssamese)
                    put("languageAssamese", song.languageAssamese)
                    put("originalLanguage", song.originalLanguage)
                    put("releaseYear", song.releaseYear)
                    put("accentColorHex", song.accentColorHex)
                    put("streamUrl", song.streamUrl ?: JSONObject.NULL)
                    put("artworkUrl", song.artworkUrl ?: JSONObject.NULL)
                    put("isPlayable", song.isPlayable)
                    
                    val sourcesArray = JSONArray()
                    for (src in song.playbackSources) {
                        sourcesArray.put(JSONObject().apply {
                            put("providerId", src.providerId)
                            put("sourceId", src.sourceId)
                            put("streamUrl", src.streamUrl ?: JSONObject.NULL)
                            put("qualityLabel", src.qualityLabel)
                        })
                    }
                    put("playbackSources", sourcesArray)
                }
                jsonArray.put(jsonObj)
            }
            file.writeText(jsonArray.toString())
        } catch (e: Exception) {
            // Ignore cache save errors
        }
    }

    private fun loadCatalogueFromCache(): List<Song> {
        return try {
            val app = try { com.amairatech.zubeenfm.ZubeenApplication.instance } catch (e: Exception) { null }
            if (app == null) return emptyList()
            val file = File(app.filesDir, CACHE_FILE_NAME)
            if (!file.exists()) return emptyList()
            
            val jsonArray = JSONArray(file.readText())
            val songs = mutableListOf<Song>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val sourcesArray = obj.getJSONArray("playbackSources")
                val sources = mutableListOf<PlaybackSource>()
                for (j in 0 until sourcesArray.length()) {
                    val sObj = sourcesArray.getJSONObject(j)
                    sources.add(PlaybackSource(
                        providerId = sObj.getString("providerId"),
                        sourceId = sObj.getString("sourceId"),
                        streamUrl = if (sObj.isNull("streamUrl")) null else sObj.getString("streamUrl"),
                        qualityLabel = sObj.getString("qualityLabel")
                    ))
                }
                
                songs.add(Song(
                    id = obj.getString("id"),
                    titleAssamese = obj.getString("titleAssamese"),
                    titleEnglish = obj.getString("titleEnglish"),
                    albumAssamese = obj.getString("albumAssamese"),
                    albumEnglish = obj.getString("albumEnglish"),
                    artistAssamese = obj.getString("artistAssamese"),
                    artistEnglish = obj.getString("artistEnglish"),
                    durationSeconds = obj.getInt("durationSeconds"),
                    genreAssamese = obj.getString("genreAssamese"),
                    languageAssamese = obj.getString("languageAssamese"),
                    originalLanguage = obj.getString("originalLanguage"),
                    releaseYear = obj.getString("releaseYear"),
                    accentColorHex = obj.getLong("accentColorHex"),
                    streamUrl = if (obj.isNull("streamUrl")) null else obj.getString("streamUrl"),
                    artworkUrl = if (obj.isNull("artworkUrl")) null else obj.getString("artworkUrl"),
                    isPlayable = obj.getBoolean("isPlayable"),
                    playbackSources = sources
                ))
            }
            songs
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Special Zubeen Garg Exception:
     * Pulls ALL verified Zubeen Garg recordings across ALL original languages (Assamese, Hindi, Bengali, etc.)
     * exclusively for Zubeen Garg's Artist Details / folder.
     */
    fun getZubeenAllLanguageCatalogue(): List<Song> {
        val normalPlaylist = playlist
        val radioPlaylist = ZubeenRadioCatalogueRepository.playlist
        val discographyProvider = com.amairatech.zubeenfm.data.provider.ZubeenDiscographyProvider()
        val discographySongs = discographyProvider.getAllSongsForZubeen()
        
        // Pull from BOTH normal and radio pools + discography
        val allZubeenCandidates = normalPlaylist + radioPlaylist + discographySongs
        
        val verifiedMultilingualZubeen = allZubeenCandidates.filter {
            ZubeenArtistFilter.isValidZubeenRecording(it.titleEnglish, it.artistEnglish)
        }
        return com.amairatech.zubeenfm.data.provider.DuplicateResolver.deduplicateSongs(verifiedMultilingualZubeen)
    }

    /**
     * Retrieves the song list for a specified artist.
     * If the artist is Zubeen Garg: returns ALL verified original-language recordings (Assamese, Hindi, Bengali, etc.).
     * If non-Zubeen artist: returns Assamese-language recordings ONLY.
     */
    fun getSongsForArtist(artist: Artist): List<Song> {
        val isZubeen = artist.isZubeenGarg || AssameseMusicFilter.isZubeenGarg(artist.nameEnglish, artist.nameEnglish)
        return if (isZubeen) {
            getZubeenAllLanguageCatalogue()
        } else {
            artist.allSongs.filter { AssameseMusicFilter.isValidForNormalCatalogue(it) }
        }
    }

    // Dynamic Albums aggregation (Assamese-only)
    val albumsFlow: StateFlow<List<Album>> = _songsFlow.map { songs ->
        val assameseSongs = songs.filter { AssameseMusicFilter.isValidForNormalCatalogue(it) }
        assameseSongs.groupBy { it.albumEnglish }.map { (albumNameEng, albumSongs) ->
            val first = albumSongs.first()
            Album(
                id = "alb_${albumNameEng.hashCode()}",
                nameAssamese = first.albumAssamese,
                nameEnglish = albumNameEng,
                artistAssamese = first.artistAssamese,
                artistEnglish = first.artistEnglish,
                artworkUrl = first.artworkUrl,
                releaseYear = first.releaseYear,
                songs = albumSongs
            )
        }
    }.stateIn(scope, SharingStarted.Eagerly, initialSeedSongs.groupBy { it.albumEnglish }.map { (albumNameEng, albumSongs) ->
        val first = albumSongs.first()
        Album(
            id = "alb_${albumNameEng.hashCode()}",
            nameAssamese = first.albumAssamese,
            nameEnglish = albumNameEng,
            artistAssamese = first.artistAssamese,
            artistEnglish = first.artistEnglish,
            artworkUrl = first.artworkUrl,
            releaseYear = first.releaseYear,
            songs = albumSongs
        )
    })

    // Dynamic Artists aggregation (Assamese-only catalog baseline)
    val artistsFlow: StateFlow<List<Artist>> = _songsFlow.map { songs ->
        val assameseSongs = songs.filter { AssameseMusicFilter.isValidForNormalCatalogue(it) }
        assameseSongs.groupBy { it.artistEnglish }.map { (artistNameEng, artistSongs) ->
            val first = artistSongs.first()
            val isZubeen = artistNameEng.lowercase().contains("zubeen") || first.artistAssamese.contains("জুবিন")
            val fullArtistSongs = if (isZubeen) getZubeenAllLanguageCatalogue() else artistSongs
            val albumsForArtist = fullArtistSongs.groupBy { it.albumEnglish }.map { (albName, albSongs) ->
                Album(
                    id = "alb_${albName.hashCode()}",
                    nameAssamese = albSongs.first().albumAssamese,
                    nameEnglish = albName,
                    artistAssamese = first.artistAssamese,
                    artistEnglish = artistNameEng,
                    artworkUrl = albSongs.first().artworkUrl,
                    releaseYear = albSongs.first().releaseYear,
                    songs = albSongs
                )
            }
            Artist(
                id = "art_${artistNameEng.hashCode()}",
                nameAssamese = first.artistAssamese,
                nameEnglish = artistNameEng,
                artworkUrl = first.artworkUrl,
                popularSongs = fullArtistSongs.take(5),
                albums = albumsForArtist,
                allSongs = fullArtistSongs
            )
        }
    }.stateIn(scope, SharingStarted.Eagerly, initialSeedSongs.groupBy { it.artistEnglish }.map { (artistNameEng, artistSongs) ->
        val first = artistSongs.first()
        val albumsForArtist = artistSongs.groupBy { it.albumEnglish }.map { (albName, albSongs) ->
            Album(
                id = "alb_${albName.hashCode()}",
                nameAssamese = albSongs.first().albumAssamese,
                nameEnglish = albName,
                artistAssamese = first.artistAssamese,
                artistEnglish = artistNameEng,
                artworkUrl = albSongs.first().artworkUrl,
                releaseYear = albSongs.first().releaseYear,
                songs = albSongs
            )
        }
        Artist(
            id = "art_${artistNameEng.hashCode()}",
            nameAssamese = first.artistAssamese,
            nameEnglish = artistNameEng,
            artworkUrl = first.artworkUrl,
            popularSongs = artistSongs.take(5),
            albums = albumsForArtist,
            allSongs = artistSongs
        )
    })

    val genres: List<String>
        get() = listOf("All Genres") + playlist.map { it.genreAssamese }.distinct()

    val languages: List<String>
        get() = listOf("All Languages") + playlist.map { it.languageAssamese }.distinct()

    val albums: List<String>
        get() = listOf("All Albums") + playlist.map { it.albumAssamese }.distinct()

    val artists: List<String>
        get() = listOf("All Artists") + playlist.map { it.artistAssamese }.distinct()

    fun refreshCatalogue() {
        scope.launch {
            providerManager.loadCompleteNormalCatalogue(isRefresh = true)
        }
    }

    fun loadMoreSongs() {
        // Redirection to refresh or complete load as pagination is disabled
        scope.launch {
            providerManager.loadCompleteNormalCatalogue()
        }
    }

    fun getNextSong(currentSong: Song?, isShuffle: Boolean = queueManager.isShuffleEnabled, repeatMode: NormalRepeatMode = queueManager.repeatMode): Song {
        queueManager.setShuffleEnabled(isShuffle, currentSong)
        queueManager.setRepeatMode(repeatMode)
        val next = queueManager.getNextSong(currentSong)
        return next ?: playlist.firstOrNull() ?: initialSeedSongs.first()
    }

    fun getNextSongOrNull(currentSong: Song?): Song? {
        return queueManager.getNextSong(currentSong)
    }

    fun getPreviousSong(currentSong: Song?): Song {
        val prev = queueManager.getPreviousSong(currentSong)
        return prev ?: playlist.lastOrNull() ?: initialSeedSongs.first()
    }

    fun recordRecentlyPlayed(song: Song) {
        _recentlyPlayedFlow.update { current ->
            val updated = current.filter { it.id != song.id }.toMutableList()
            updated.add(0, song)
            if (updated.size > 30) updated.take(30) else updated
        }
    }

    fun toggleFavorite(songId: String) {
        _favoritesFlow.update { current ->
            val set = current.toMutableSet()
            if (songId in set) set.remove(songId) else set.add(songId)
            set
        }
    }

    fun searchSongs(
        query: String,
        selectedGenre: String = "All Genres",
        selectedLanguage: String = "All Languages",
        selectedAlbum: String = "All Albums",
        isAzSort: Boolean = false
    ): List<Song> {
        val trimmed = query.trim().lowercase()
        
        // 1. Initial filter based on category selections (Genre, Language, Album)
        val categoryFiltered = playlist.filter { song ->
            val matchesGenre = selectedGenre == "All Genres" || song.genreAssamese == selectedGenre
            val matchesLanguage = selectedLanguage == "All Languages" || song.languageAssamese == selectedLanguage
            val matchesAlbum = selectedAlbum == "All Albums" || song.albumAssamese == selectedAlbum
            matchesGenre && matchesLanguage && matchesAlbum
        }

        if (trimmed.isEmpty()) {
            // No search query: apply A-Z sort if requested, otherwise return original order
            return if (isAzSort) categoryFiltered.sortedBy { it.titleEnglish.lowercase() } else categoryFiltered
        }

        // 2. Perform Smart Hybrid Search with Relevance Ranking
        return com.amairatech.zubeenfm.data.provider.ZubeenSmartSearchEngine.search(categoryFiltered, query)
    }
}
