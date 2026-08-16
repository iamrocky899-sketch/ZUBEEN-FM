package com.amairatech.zubeenfm.data.provider

import com.amairatech.zubeenfm.data.model.ContentType
import com.amairatech.zubeenfm.data.model.Song

/**
 * Strict filtering engine for ZUBEEN FM Radio Mode.
 * Strictly verifies that recordings are genuine Zubeen Garg performances,
 * rejecting covers, tributes by third parties, interviews, podcasts,
 * reaction videos, shorts, ads, non-Zubeen Assamese artists, and unrelated artists.
 */
object ZubeenArtistFilter {

    private val REJECTED_KEYWORDS = listOf(
        "cover",
        "covered by",
        "female version",
        "male version",
        "fan cover",
        "acoustic cover",
        "unplugged cover",
        "instrumental cover",
        "karaoke",
        "tribute to zubeen",
        "tribute by",
        "homage by",
        "reaction",
        "reacting",
        "reacts",
        "controversy",
        "alcohol",
        "explicit",
        "scandal",
        "hospital",
        "health update",
        "behind the scenes",
        "making of",
        "trailer",
        "teaser",
        "review",
        "vlog",
        "speech",
        "news",
        "press meet",
        "press conference",
        "status video",
        "shorts",
        "tiktok",
        "reels",
        "dialogue",
        "slowed reverb",
        "slowed and reverb",
        "promo",
        "sponsored",
        "advertisement",
        "ad "
    )

    private val REJECTED_CHANNEL_KEYWORDS = listOf(
        "talks",
        "podcast",
        "cast",
        "news",
        "tv",
        "media",
        "times",
        "channel",
        "daily",
        "updates",
        "reaction",
        "gamer",
        "vlog",
        "pratidin",
        "dy365",
        "time8",
        "nk tv"
    )

    private val VALID_ZUBEEN_IDENTIFIERS = listOf(
        "zubeen garg",
        "জুবিন গাৰ্গ",
        "zubeen",
        "জুবিন"
    )

    private val NON_ZUBEEN_ARTISTS = listOf(
        // Other Assamese Artists (belong to Normal Mode only)
        "bhupen hazarika", "ভূপেন হাজৰিকা",
        "khagen mahanta", "খগেন মহন্ত",
        "papon", "পাপন", "angaraag mahanta",
        "deeplina deka", "দীপলিনা ডেকা",
        "jayanta hazarika", "জয়ন্ত হাজৰিকা",
        "neel akash", "নীল আকাশ",
        "achurjya borpatra", "আচ্যুৰ্য্য বৰপাত্ৰ",
        "gayatri hazarika", "গায়ত্ৰী হাজৰিকা",
        "zublee baruah", "জুবলী বৰুৱা",
        "bornali kalita", "বৰ্ণালী কলিতা",
        "kuldeep patgiri", "কুলদীপ পাটগিৰী",
        "subasana dutta", "শুভাশনা দত্ত",
        "simanta shekhar", "সীমান্ত শেখৰ",
        "manas robin", "মানস ৰবীন",
        "pranabiram baruah",
        "dipen baruah", "দ্বীপেন বৰুৱা",
        "pulak banerjee", "পুলক বেনাৰ্জী",

        // Other Indian / Global Artists
        "arijit singh", "sonu nigam", "neha kakkar", "badshah",
        "jubin nautiyal", "shreya ghoshal", "kumar sanu", "udit narayan",
        "alka yagnik", "kishore kumar", "lata mangeshkar", "mohit chauhan",
        "armaan malik", "darshan raval", "anuv jain", "yo yo honey singh",
        "sidhu moose wala", "diljit dosanjh", "khesari lal yadav", "pawan singh"
    )

    /**
     * Determines whether a candidate recording is a genuine, verified Zubeen Garg performance.
     */
    fun isValidZubeenRecording(
        rawTitle: String,
        rawArtist: String,
        rawDescription: String = ""
    ): Boolean {
        val titleClean = rawTitle.trim()
        val artistClean = rawArtist.trim()

        if (titleClean.isEmpty() && artistClean.isEmpty()) {
            return false
        }

        val titleLower = titleClean.lowercase()
        val artistLower = artistClean.lowercase()
        val descLower = rawDescription.trim().lowercase()

        // 1. Rejection Check: Look for non-Zubeen covers, tributes, podcasts, reactions, interviews, shorts, controversies
        for (kw in REJECTED_KEYWORDS) {
            if (titleLower.contains(kw) || descLower.contains(kw) || artistLower.contains(kw)) {
                return false
            }
        }

        // 2. Reject talk show/news channels acting as artist
        for (chKw in REJECTED_CHANNEL_KEYWORDS) {
            if (artistLower.contains(chKw) && !artistLower.contains("zubeen") && !artistLower.contains("জুবিন")) {
                return false
            }
        }

        // 3. Reject other artists if artist field explicitly names them and does NOT include Zubeen
        val containsOtherArtist = NON_ZUBEEN_ARTISTS.any { nonZubeen ->
            (artistLower.contains(nonZubeen) || (titleLower.contains("by $nonZubeen") || titleLower.contains("feat. $nonZubeen") || titleLower.contains("ft. $nonZubeen"))) &&
                !artistLower.contains("zubeen") && !artistLower.contains("জুবিন")
        }
        if (containsOtherArtist) {
            return false
        }

        // 4. Positive Artist Verification:
        val isArtistFieldZubeen = VALID_ZUBEEN_IDENTIFIERS.any { artistLower.contains(it) }
        if (isArtistFieldZubeen) {
            return true
        }

        // If artist field is not explicitly Zubeen (e.g. video title only), title must clearly attribute Zubeen as the singer
        val isTitleZubeen = VALID_ZUBEEN_IDENTIFIERS.any { titleLower.contains(it) }
        if (!isTitleZubeen) {
            return false
        }

        // Must not be a discussion "on Zubeen" or "about Zubeen"
        if (titleLower.contains(" on zubeen") || titleLower.contains(" about zubeen") || titleLower.contains(" with zubeen")) {
            return false
        }

        return true
    }

    /**
     * Strict Radio Filter Rule:
     * Accepts verified Zubeen Garg music in ANY language.
     * Accepts verified Assamese Zubeen spoken content.
     * Rejects non-Assamese spoken content, translated audio, and unrelated content.
     */
    fun isValidForRadio(song: Song): Boolean {
        if (!isValidZubeenRecording(song.titleEnglish, song.artistEnglish, song.albumEnglish)) {
            return false
        }

        return when (song.contentType) {
            ContentType.SONG -> true
            ContentType.ZUBEEN_PODCAST,
            ContentType.ZUBEEN_EPISODE,
            ContentType.ZUBEEN_STORY,
            ContentType.ZUBEEN_INTERVIEW,
            ContentType.ZUBEEN_MEMORIAL -> song.originalLanguage.equals("ASSAMESE", ignoreCase = true)
            ContentType.UNKNOWN -> false
        }
    }
}
