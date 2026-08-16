package com.amairatech.zubeenfm.data.provider

import com.amairatech.zubeenfm.data.model.ContentType
import com.amairatech.zubeenfm.data.model.Song

/**
 * Normalizes raw metadata from online providers into clean Zubeen Garg catalog entries.
 */
object MetadataNormalizer {

    private val JUNK_PATTERNS = listOf(
        Regex("\\[.*?\\]"),
        Regex("\\(.*?official.*?\\)", RegexOption.IGNORE_CASE),
        Regex("\\(.*?audio.*?\\)", RegexOption.IGNORE_CASE),
        Regex("\\(.*?video.*?\\)", RegexOption.IGNORE_CASE),
        Regex("\\(.*?full song.*?\\)", RegexOption.IGNORE_CASE),
        Regex("\\(.*?lyrical.*?\\)", RegexOption.IGNORE_CASE),
        Regex("\\(.*?hd.*?\\)", RegexOption.IGNORE_CASE),
        Regex("\\(.*?4k.*?\\)", RegexOption.IGNORE_CASE),
        Regex("\\|.*"),
        Regex("- Zubeen Garg.*", RegexOption.IGNORE_CASE),
        Regex("by Zubeen Garg.*", RegexOption.IGNORE_CASE)
    )

    fun cleanTitle(rawTitle: String): String {
        var cleaned = rawTitle
        for (pattern in JUNK_PATTERNS) {
            cleaned = cleaned.replace(pattern, "")
        }
        return cleaned.trim().trim('-', '–', ':', '|').trim()
    }

    fun detectOriginalLanguage(
        title: String,
        album: String = "",
        artist: String = "",
        description: String = "",
        explicitLanguage: String? = null
    ): String {
        if (!explicitLanguage.isNullOrBlank()) {
            val upper = explicitLanguage.trim().uppercase()
            if (upper in listOf("ASSAMESE", "HINDI", "BENGALI", "NEPALI", "PUNJABI", "BHOJPURI", "ENGLISH")) {
                return upper
            }
        }

        val combined = "$title $album $artist $description".lowercase()

        return when {
            combined.contains("hindi") || combined.contains("bollywood") || combined.contains("ya ali") ||
                combined.contains("gangster") || combined.contains("krrish") || combined.contains("dil tu hi bataa") -> "HINDI"

            combined.contains("bengali") || combined.contains("bangla") || combined.contains("mon mane na") ||
                combined.contains("khoka") || combined.contains("bujhena se bujhena") -> "BENGALI"

            combined.contains("nepali") -> "NEPALI"
            combined.contains("punjabi") -> "PUNJABI"
            combined.contains("bhojpuri") -> "BHOJPURI"
            combined.contains("english") -> "ENGLISH"

            combined.contains("assamese") || combined.contains("asomiya") || combined.contains("axomiya") ||
                combined.contains("bihu") || combined.contains("বিহু") || combined.contains("borgeet") ||
                combined.contains("বৰগীত") || combined.contains("lokogeet") || combined.contains("লোকগীত") ||
                combined.contains("husori") || combined.contains("হুঁচৰি") || combined.contains("anamika") ||
                combined.contains("অনামীকা") || combined.contains("maya") || combined.contains("মায়া") ||
                combined.contains("hiya diya niya") || combined.contains("mon jaai") ||
                combined.contains("kolore") || combined.contains("pokhi") || combined.contains("junaki") ||
                combined.contains("dhumuha") || combined.contains("rodor chithi") || combined.contains("pakhi") ||
                combined.contains("mur") || combined.contains("apunar") || combined.contains("desh") ||
                combined.contains("luit") || combined.contains("brahmaputra") || combined.contains("asom") ||
                combined.contains("axom") || combined.contains("jaan") || combined.contains("moni") ||
                combined.contains("moina") || combined.contains("jun") || combined.contains("morom") ||
                combined.contains("oi") || combined.contains("ai") || combined.contains("deuti") ||
                combined.contains("maya") || combined.contains("mon") || combined.contains("hiya") ||
                combined.contains("jaanmoni") || combined.contains("jaan moni") || combined.contains("hiyadun") ||
                combined.contains("hiya diya niya") || combined.contains("jaan") || combined.contains("mon") ||
                combined.contains("nahor") || combined.contains("sirene") || combined.contains("mukuta") -> "ASSAMESE"

            // Check Assamese script presence (\u0980-\u09FF covers Bengali/Assamese script)
            combined.any { it in '\u0980'..'\u09FF' } -> "ASSAMESE"

            // Ensure UNKNOWN is not falsely detected as Assamese
            else -> "UNKNOWN"
        }
    }

    fun detectContentType(
        title: String,
        artist: String,
        description: String = ""
    ): ContentType {
        val combined = "$title $artist $description".lowercase()
        
        val isZubeen = combined.contains("zubeen") || combined.contains("জুবিন")

        return when {
            combined.contains("podcast") || combined.contains("moncast") -> 
                if (isZubeen) ContentType.ZUBEEN_PODCAST else ContentType.UNKNOWN
                
            combined.contains("episode") || combined.contains("ep ") || combined.contains("ep.") || combined.contains("ep:") || combined.contains("ep-") -> 
                if (isZubeen) ContentType.ZUBEEN_EPISODE else ContentType.UNKNOWN
                
            combined.contains("interview") || combined.contains("talk show") || combined.contains("conversation") -> 
                if (isZubeen) ContentType.ZUBEEN_INTERVIEW else ContentType.UNKNOWN
                
            combined.contains("story") || combined.contains("stories") || combined.contains("documentary") -> 
                if (isZubeen) ContentType.ZUBEEN_STORY else ContentType.UNKNOWN
                
            combined.contains("memorial") || combined.contains("tribute") || combined.contains("homage") || combined.contains("শ্ৰদ্ধাঞ্জলী") -> 
                if (isZubeen) ContentType.ZUBEEN_MEMORIAL else ContentType.UNKNOWN
                
            else -> ContentType.SONG
        }
    }

    fun detectLanguage(title: String, album: String): String {
        val orig = detectOriginalLanguage(title, album)
        return getLanguageAssameseLabel(orig)
    }

    fun getLanguageAssameseLabel(originalLanguage: String): String {
        return when (originalLanguage.uppercase()) {
            "ASSAMESE" -> "অসমীয়া"
            "HINDI" -> "হিন্দী"
            "BENGALI" -> "বাংলা"
            "ENGLISH" -> "ইংৰাজী"
            "NEPALI" -> "নেপালী"
            "PUNJABI" -> "পঞ্জাবী"
            "BHOJPURI" -> "ভোজপুৰী"
            else -> "অজ্ঞাত"
        }
    }

    fun inferGenre(title: String, album: String, language: String): String {
        val lower = "$title $album".lowercase()
        return when {
            language == "হিন্দী" || language == "HINDI" -> "বলিউড ৰক"
            lower.contains("bihu") || lower.contains("বিহু") || lower.contains("লোক") -> "বিহু আৰু লোকসংগীত"
            lower.contains("maya") || lower.contains("মায়া") || lower.contains("anamika") || lower.contains("অনামীকা") -> "চিৰসেউজ মেল'ডী"
            lower.contains("hiya") || lower.contains("cinema") || lower.contains("movie") || lower.contains("film") || lower.contains("কথাছবি") -> "কথাছবিৰ গীত"
            lower.contains("mon") || lower.contains("val") || lower.contains("love") || lower.contains("ৰ'মাণ্টিক") -> "ৰ'মাণ্টিক সুৰ"
            else -> "আধুনিক সুৰীয়া"
        }
    }

    /**
     * Converts a release year or date string into a timestamp for sorting.
     */
    fun parseReleaseYearToTimestamp(releaseYear: String): Long {
        if (releaseYear.isBlank() || releaseYear == "Online" || releaseYear == "Unknown") return 0L
        return try {
            val year = releaseYear.take(4).toInt()
            // Approximate to start of year
            val cal = java.util.Calendar.getInstance()
            cal.set(year, 0, 1, 0, 0, 0)
            cal.timeInMillis
        } catch (e: Exception) {
            0L
        }
    }
}
