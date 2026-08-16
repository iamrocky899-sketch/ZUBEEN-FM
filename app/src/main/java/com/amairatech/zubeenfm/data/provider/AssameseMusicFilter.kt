package com.amairatech.zubeenfm.data.provider

import com.amairatech.zubeenfm.data.model.ContentType
import com.amairatech.zubeenfm.data.model.Song

/**
 * Filter engine for Normal Mode — Assamese Music Library.
 * Validates that recordings are authentic Assamese music (from classic 1980s to modern releases)
 * performed by Assamese artists, across diverse Assamese genres, rejecting non-Assamese tracks.
 */
object AssameseMusicFilter {

    private val REJECTED_KEYWORDS = listOf(
        "cover by",
        "fan cover",
        "reaction",
        "reacting",
        "interview",
        "podcast",
        "behind the scenes",
        "making of",
        "trailer",
        "teaser",
        "review",
        "vlog",
        "speech",
        "news"
    )

    private val NON_ASSAMESE_LANGUAGE_MARKERS = listOf(
        "bengali",
        "bangla",
        "punjabi",
        "bhojpuri",
        "tamil",
        "telugu",
        "malayalam",
        "kannada",
        "marathi",
        "gujarati",
        "nepali",
        "odia"
    )

    private val KNOWN_ASSAMESE_ARTISTS = listOf(
        "zubeen garg", "জুবিন গাৰ্গ", "zubeen",
        "bhupen hazarika", "ভূপেন হাজৰিকা",
        "jayanta hazarika", "জয়ন্ত হাজৰিকা",
        "dipali barthakur", "দীপালী বৰঠাকুৰ",
        "pratima barua pandey", "প্ৰতিমা বৰুৱা পাণ্ডে",
        "khagen mahanta", "খগেন মহন্ত",
        "archana mahanta", "অৰ্চনা মহন্ত",
        "angaraag mahanta", "papon", "পাপন",
        "jitul sonowal", "জিতুল সোণোৱাল",
        "bornali kalita", "বৰ্ণালী কলিতা",
        "vitali das", "বৈতালী দাস",
        "zublee baruah", "জুবলী বৰুৱা",
        "neel akash", "নীল আকাশ",
        "deeplina deka", "দীপলিনা ডেকা",
        "babu baruah", "বাবু বৰুৱা",
        "achurjya borpatra", "আশ্চৰ্য্য বৰপাত্ৰ",
        "shankuraj konwar", "শঙ্কুৰাজ কোঁৱৰ",
        "montumoni saikia", "মণ্টুমণি শইকীয়া",
        "vreegu kashyap", "ভৃগু কাশ্যপ",
        "rupam bhuyan", "ৰূপম ভূঞা",
        "maitrayee patar", "মৈত্ৰেয়ী পাতৰ",
        "priyanka bharali", "প্ৰিয়ংকা ভৰালী",
        "kusum kailash", "কুসুম কৈলাস",
        "dwipen baruah", "দ্বিপেন বৰুৱা",
        "pulak banerjee", "পুলক বেনাৰ্জী",
        "ridip dutta", "ৰীদিপ দত্ত",
        "samar hazarika", "সমৰ হাজৰিকা",
        "anima choudhury", "অণিমা চৌধুৰী",
        "subasana dutta", "সুবাসনা দত্ত",
        "dikshu sarma", "দীক্ষু শৰ্মা",
        "simanta shekhar", "সীমান্ত শেখৰ",
        "sadananda gogoi", "সদানন্দ গগৈ",
        "anindita paul", "অনিন্দিতা পাল",
        "tarali sarma", "তৰালী শৰ্মা",
        "joy barua", "জয় বৰুৱা",
        "krishna mani chutiya", "কৃষ্ণ মণি চুতীয়া",
        "raaj j konwar", "joy barua", "manas robin", "parineeta", "shashwati phukan",
        "anaya brahma", "diganta bharati", "sudakshina sarma", "mridula das"
    )

    private val ASSAMESE_GENRE_AND_KEYWORDS = listOf(
        "bihu", "বিহু", "husori", "হুঁচৰি",
        "lokogeet", "লোকগীত", "kamrupiya", "goalparia", "borgeet", "বৰগীত",
        "assamese", "অসমীয়া", "asomiya", "axomiya",
        "tokari", "টোকোৰী", "zubeen", "জুবিন", "papon", "পাপন",
        "anamika", "অনামীকা", "maya", "মায়া", "mukuta", "মুকুতা",
        "hiya diya niya", "mon jaai", "মন যায়", "junbai", "জোনবাই",
        "janmoni", "জানমণি", "jaanmoni", "morom", "moromi", "মৰমী", "bohagi", "বোহাগী",
        "luit", "ব্রহ্মপুত্ৰ", "asom", "axom", "অসম", "junaki", "জোনাকী", "oi", "ai", "deuti",
        "pokhi", "পক্ষী", "rodor chithi", "jaan", "জান", "nahor", "sirene", "hiyadun", "moina", "jun"
    )

    /**
     * Determines whether a candidate track is an authentic Assamese music recording.
     */
    fun isValidAssameseRecording(
        rawTitle: String,
        rawArtist: String,
        rawAlbum: String = "",
        rawDescription: String = ""
    ): Boolean {
        val titleLower = rawTitle.lowercase()
        val artistLower = rawArtist.lowercase()
        val albumLower = rawAlbum.lowercase()
        val descLower = rawDescription.lowercase()
        val combined = "$titleLower $artistLower $albumLower $descLower"

        // 1. Rejection Check
        for (kw in REJECTED_KEYWORDS) {
            if (titleLower.contains(kw) || descLower.contains(kw)) {
                android.util.Log.v("ZubeenFilter", "REJECTED (Metadata): Keyword '$kw' in '$rawTitle'")
                return false
            }
        }

        // 2. Reject other regional languages
        for (lang in NON_ASSAMESE_LANGUAGE_MARKERS) {
            if (combined.contains(lang) && !combined.contains("assamese") && !combined.contains("asomiya")) {
                android.util.Log.v("ZubeenFilter", "REJECTED (Language): Marker '$lang' in '$rawTitle'")
                return false
            }
        }

        // 3. Known Assamese Artist match
        val isAssameseArtist = KNOWN_ASSAMESE_ARTISTS.any { artist ->
            artistLower.contains(artist) || titleLower.contains(artist)
        }

        if (isAssameseArtist) {
            return true
        }

        // 4. Assamese keywords / Bihu / Lokogeet / Film context match
        val isAssameseKeywordPresent = ASSAMESE_GENRE_AND_KEYWORDS.any { kw ->
            combined.contains(kw)
        }

        if (!isAssameseKeywordPresent) {
            android.util.Log.v("ZubeenFilter", "REJECTED (Context): No Assamese markers for '$rawTitle'")
        }

        return isAssameseKeywordPresent
    }

    fun isZubeenGarg(rawTitle: String, rawArtist: String): Boolean {
        val artistLower = rawArtist.lowercase()
        val titleLower = rawTitle.lowercase()
        return artistLower.contains("zubeen") || artistLower.contains("জুবিন") ||
            (titleLower.contains("zubeen garg") || titleLower.contains("জুবিন গাৰ্গ"))
    }

    fun isZubeenGarg(song: com.amairatech.zubeenfm.data.model.Song): Boolean {
        return song.isZubeenGarg || isZubeenGarg(song.titleEnglish, song.artistEnglish)
    }

    /**
     * Strict Normal Catalogue Filter Rule:
     * Accepts ONLY Assamese-language recordings for the general Assamese Normal Catalogue.
     * Non-Assamese songs by any artist (including Hindi/Bengali Zubeen songs or Papon Hindi songs)
     * are REJECTED from the general Assamese catalogue.
     */
    fun isValidForNormalCatalogue(
        rawTitle: String,
        rawArtist: String,
        rawAlbum: String = "",
        rawDescription: String = "",
        originalLanguage: String = "ASSAMESE"
    ): Boolean {
        // Must pass basic rejection checks (no covers/podcasts/interviews)
        if (!isValidAssameseRecording(rawTitle, rawArtist, rawAlbum, rawDescription)) {
            return false
        }

        // Detect language conservatively
        val detectedLang = MetadataNormalizer.detectOriginalLanguage(
            title = rawTitle,
            album = rawAlbum,
            artist = rawArtist,
            description = rawDescription,
            explicitLanguage = originalLanguage
        )

        // General Assamese Normal Catalogue ONLY accepts ASSAMESE language recordings!
        // BUG FIX: If detected as UNKNOWN but it's a known Assamese artist and passed basic validation, 
        // we allow it as Assamese music by default to avoid losing songs with incomplete metadata.
        if (detectedLang == "UNKNOWN") {
            val isKnown = KNOWN_ASSAMESE_ARTISTS.any { artist ->
                rawArtist.lowercase().contains(artist) || rawTitle.lowercase().contains(artist)
            }
            if (!isKnown) {
                android.util.Log.v("ZubeenFilter", "REJECTED (Lang): Unknown language & artist for '$rawTitle'")
            }
            return isKnown
        }

        val isAssamese = detectedLang == "ASSAMESE"
        if (!isAssamese) {
            android.util.Log.v("ZubeenFilter", "REJECTED (Lang): Language '$detectedLang' is not Assamese for '$rawTitle'")
        }
        return isAssamese
    }

    fun isValidForNormalCatalogue(song: Song): Boolean {
        if (song.contentType != ContentType.SONG) return false
        
        return isValidForNormalCatalogue(
            rawTitle = song.titleEnglish,
            rawArtist = song.artistEnglish,
            rawAlbum = song.albumEnglish,
            originalLanguage = song.originalLanguage
        )
    }
}
