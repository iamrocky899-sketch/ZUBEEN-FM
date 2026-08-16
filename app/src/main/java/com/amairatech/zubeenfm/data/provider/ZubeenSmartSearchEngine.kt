package com.amairatech.zubeenfm.data.provider

import com.amairatech.zubeenfm.data.model.Song
import java.util.Locale
import kotlin.math.min

/**
 * YouTube-Music-style Smart Music Search Engine for ZUBEEN FM.
 * Features:
 * 1. Multi-token relevance scoring.
 * 2. Intent extraction (Artist, Genre, Mood, Era).
 * 3. Fuzzy matching for small spelling errors.
 * 4. Assamese Unicode & Romanized Assamese search support.
 * 5. Deterministic priority ranking.
 */
object ZubeenSmartSearchEngine {

    private val STOP_WORDS = setOf(
        "song", "songs", "music", "track", "by", "the", "of", "assamese", "gana", "gaan", "best", "hits", "original"
    )

    private val GENRE_ALIASES = mapOf(
        "bihu" to listOf("বিহু", "লোকসংগীত"),
        "romantic" to listOf("ৰ'মাণ্টিক", "মৰম", "ভালপোৱা", "love"),
        "classic" to listOf("চিৰসেউজ", "পুৰণি", "old"),
        "modern" to listOf("আধুনিক"),
        "devotional" to listOf("ভক্তিগীত", "বৰগীত", "ঈশ্বৰ"),
        "film" to listOf("কথাছবি", "চিনেমা", "movie", "soundtrack")
    )

    private val ARTIST_ALIASES = mapOf(
        "zubeen" to listOf("zubeen garg", "জুবিন", "জুবিন গাৰ্গ"),
        "papon" to listOf("angaraag mahanta", "পাপন", "অংগৰাগ মহন্ত"),
        "bhupen" to listOf("bhupen hazarika", "ভূপেন", "ভূপেন হাজৰিকা"),
        "jayanta" to listOf("jayanta hazarika", "জয়ন্ত", "জয়ন্ত হাজৰিকা")
    )

    /**
     * Searches a catalogue of songs using smart relevance ranking.
     */
    fun search(catalogue: List<Song>, query: String): List<Song> {
        val trimmed = query.trim().lowercase(Locale.getDefault())
        if (trimmed.isEmpty()) return catalogue

        val tokens = trimmed.split(Regex("\\s+")).filter { it.length > 1 && it !in STOP_WORDS }
        if (tokens.isEmpty() && trimmed.isNotEmpty()) {
            // If all tokens were stop words, search with raw query
            return searchWithTokens(catalogue, listOf(trimmed), trimmed)
        }

        return searchWithTokens(catalogue, tokens, trimmed)
    }

    private fun searchWithTokens(catalogue: List<Song>, tokens: List<String>, rawQuery: String): List<Song> {
        val scoredList = catalogue.mapNotNull { song ->
            val score = calculateRelevanceScore(song, tokens, rawQuery)
            if (score > 0) Pair(song, score) else null
        }

        return scoredList.sortedByDescending { it.second }.map { it.first }
    }

    private fun calculateRelevanceScore(song: Song, tokens: List<String>, rawQuery: String): Int {
        var totalScore = 0

        val titleEng = song.titleEnglish.lowercase()
        val titleAs = song.titleAssamese.lowercase()
        val artistEng = song.artistEnglish.lowercase()
        val artistAs = song.artistAssamese.lowercase()
        val albumEng = song.albumEnglish.lowercase()
        val albumAs = song.albumAssamese.lowercase()
        val genreAs = song.genreAssamese.lowercase()

        val titleTokens = (titleEng.split(Regex("\\s+")) + titleAs.split(Regex("\\s+"))).filter { it.length > 1 }
        val artistTokens = (artistEng.split(Regex("\\s+")) + artistAs.split(Regex("\\s+"))).filter { it.length > 1 }
        val albumTokens = (albumEng.split(Regex("\\s+")) + albumAs.split(Regex("\\s+"))).filter { it.length > 1 }

        // 1. EXACT TITLE MATCH (Highest Priority)
        if (titleEng == rawQuery || titleAs == rawQuery) return 1000

        // 2. TITLE STARTS WITH
        if (titleEng.startsWith(rawQuery) || titleAs.startsWith(rawQuery)) {
            totalScore += 850
        }

        // 3. TITLE CONTAINS RAW QUERY
        if (titleEng.contains(rawQuery) || titleAs.contains(rawQuery)) {
            totalScore += 700
        }

        // 4. MULTI-TOKEN SCORING
        var tokensMatchedInTitle = 0
        var tokensMatchedInArtist = 0
        var tokensMatchedInAlbum = 0
        var tokensMatchedInGenre = 0

        for (token in tokens) {
            var tokenScore = 0
            
            // Check Title tokens with fuzzy
            val titleMatch = titleTokens.any { t -> 
                t == token || levenshteinDistance(token, t) <= (if (token.length > 4) 2 else 1) || (token.length > 2 && t.startsWith(token))
            }
            if (titleMatch) {
                tokenScore += 150
                tokensMatchedInTitle++
            }

            // Check Artist tokens with fuzzy
            val artistMatch = artistTokens.any { t -> 
                t == token || levenshteinDistance(token, t) <= (if (token.length > 4) 2 else 1) || isArtistAlias(token, artistEng, artistAs)
            }
            if (artistMatch) {
                tokenScore += 120
                tokensMatchedInArtist++
            }

            // Check Album
            if (albumTokens.any { it == token || levenshteinDistance(token, it) <= 1 }) {
                tokenScore += 80
                tokensMatchedInAlbum++
            }

            // Check Genre / Mood intent
            if (genreAs.contains(token) || isGenreAlias(token, genreAs)) {
                tokenScore += 60
                tokensMatchedInGenre++
            }

            // Check Era (e.g. "90s", "80s", "old")
            if (isEraMatch(token, song.releaseYear)) {
                tokenScore += 50
            }

            totalScore += tokenScore
        }

        // Bonus for matching all tokens in title
        if (tokensMatchedInTitle == tokens.size) totalScore += 200
        
        // Bonus for strong title + artist combination
        if (tokensMatchedInTitle > 0 && tokensMatchedInArtist > 0) totalScore += 300

        // 5. FUZZY RAW QUERY MATCH (If no strong matches yet)
        if (totalScore < 100) {
            val dist = levenshteinDistance(rawQuery, titleEng)
            if (dist <= 2) {
                totalScore += (100 - dist * 20)
            }
        }

        return totalScore
    }

    private fun isArtistAlias(token: String, artistEng: String, artistAs: String): Boolean {
        for ((key, aliases) in ARTIST_ALIASES) {
            if (token == key || token in aliases) {
                if (aliases.any { artistEng.contains(it) || artistAs.contains(it) }) return true
            }
        }
        return false
    }

    private fun isGenreAlias(token: String, genreAs: String): Boolean {
        for ((key, aliases) in GENRE_ALIASES) {
            if (token == key || token in aliases) {
                if (aliases.any { genreAs.contains(it) }) return true
            }
        }
        return false
    }

    private fun isEraMatch(token: String, releaseYear: String): Boolean {
        if (releaseYear == "Online" || releaseYear == "Local") return false
        val year = releaseYear.toIntOrNull() ?: return false
        
        return when (token) {
            "90s", "90's" -> year in 1990..1999
            "80s", "80's" -> year in 1980..1989
            "2000s", "2000's" -> year in 2000..2009
            "old" -> year < 2010
            "new", "latest" -> year >= 2023
            else -> token == releaseYear
        }
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val n = s1.length
        val m = s2.length
        if (n == 0) return m
        if (m == 0) return n

        var prev = IntArray(m + 1) { it }
        var curr = IntArray(m + 1)

        for (i in 1..n) {
            curr[0] = i
            for (j in 1..m) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                curr[j] = min(min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost)
            }
            val temp = prev
            prev = curr
            curr = temp
        }
        return prev[m]
    }
}
