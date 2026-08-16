package com.amairatech.zubeenfm.data.repository

import com.amairatech.zubeenfm.data.model.ZubeenStory

/**
 * Repository providing verified, respectful, Assamese-only biographical and cultural stories
 * about Zubeen Garg ("জুবিন দাৰ কাহিনী").
 *
 * Implements a non-repeating shuffled-bag/deck selector to ensure users explore
 * diverse stories without immediate repetition.
 */
object ZubeenStoriesRepository {

    private val stories: List<ZubeenStory> = listOf(
        // 1. শৈশৱ আৰু সংগীতৰ আৰম্ভণি
        ZubeenStory(
            id = "story_01_childhood",
            title = "সুৰৰ কোলাত শৈশৱৰ আৰম্ভণি",
            assameseText = "১৯৭২ চনৰ ১৮ নৱেম্বৰত মেঘালয়ৰ তুৰাত জন্মগ্ৰহণ কৰা জুবিন গাৰ্গৰ পৰিয়ালটো আছিল সাহিত্য আৰু সংগীতৰ এক উৰ্বৰ ক্ষেত্র। পিতৃ মোহিনী বৰঠাকুৰ (কপিল ঠাকুৰ) আছিল প্ৰসিদ্ধ গীতিকাৰ আৰু কবি, আৰু মাতৃ ইলি বৰঠাকুৰ আছিল কণ্ঠশিল্পী। ঘৰৰ এই সাংগীতিক পৰিৱেশতেই অতি কম বয়সৰ পৰাই জুবিনে সংগীতৰ প্ৰথম পাঠ লাভ কৰিছিল আৰু মাত্ৰ তিনি বছৰ বয়সতেই সংগীতৰ প্ৰতি গভীৰ অনুৰাগ প্ৰকাশ কৰিছিল।",
            sourceName = "অসমীয়া জীৱনী অভিধান আৰু সাংস্কৃতিক তথ্যকোষ",
            sourceReference = "বৰঠাকুৰ পৰিয়ালৰ সাহিত্য-সাংস্কৃতিক ঐতিহ্য",
            category = "শৈশৱ আৰু সংগীতৰ আৰম্ভণি"
        ),

        // 2. সংগীত শিক্ষাৰ দিনবোৰ
        ZubeenStory(
            id = "story_02_musical_training",
            title = "গুৰু ৰবীন বেনাৰ্জীৰ ওচৰত সংগীত সাধনা",
            assameseText = "জুবিন গাৰ্গে প্ৰাথমিক অৱস্থাত গুৰু ৰবীন বেনাৰ্জীৰ ওচৰত শাস্ত্ৰীয় তবলাৰ আনুষ্ঠানিক শিক্ষা গ্ৰহণ কৰিছিল। পৰৱৰ্তী সময়ত গুৱাহাটীত পণ্ডিত ৰমণী ৰায়ৰ ওচৰত শাস্ত্ৰীয় সংগীতৰ সুৰ আৰু তালৰ সূক্ষ্ম পাঠ লয়। তবলাৰ ছন্দৰ ওপৰত দখল থকাৰ বাবেই পৰৱৰ্তী সময়ত তেওঁৰ সংগীত পৰিচালনাত জটিল তালৰ ব্যৱহাৰ অতি স্বাভাৱিক আৰু মনোৰম হৈ পৰিছিল।",
            sourceName = "অসম সংগীত নাটক অকাডেমী সংগ্ৰহ",
            sourceReference = "অসমৰ সংগীত সাধকসকলৰ তথ্যপঞ্জী",
            category = "সংগীত শিক্ষাৰ দিনবোৰ"
        ),

        // 3. ‘অনামিকা’ আৰু সংগীত জগতত প্ৰৱেশ
        ZubeenStory(
            id = "story_03_anamika_breakthrough",
            title = "‘অনামিকা’ৰ অভূতপূৰ্ব জাগৰণ",
            assameseText = "১৯৯২ চনত মুক্তি লাভ কৰা জুবিন গাৰ্গৰ প্ৰথম একক এলবাম ‘অনামিকা’ই অসমৰ সংগীত জগতত এক ইতিহাসৰ সৃষ্টি কৰিছিল। গুৱাহাটীৰ জ্যোতি চিত্ৰবনত বাণীবদ্ধ কৰা এই এলবামটোৰ প্ৰতিটো গানে অসমৰ যুৱ প্ৰজন্মক উন্মাদ কৰি তুলিছিল। আধুনিক সুৰ আৰু হৃদয়স্পৰ্শী শব্দৰ সংমিশ্ৰণেৰে ‘অনামিকা’ই অসমীয়া আধুনিক সংগীতৰ ইতিহাসত এক নতুন যুগৰ সূচনা কৰিছিল।",
            sourceName = "অসমীয়া আধুনিক সংগীতৰ ইতিহাস",
            sourceReference = "১৯৯২ চনৰ ক্যাছেট বিপ্লৱ আৰু অনামিকা",
            category = "‘অনামিকা’ আৰু সংগীত জগতত প্ৰৱেশ"
        ),

        // 4. বহুমুখী সংগীত প্ৰতিভা
        ZubeenStory(
            id = "story_04_multifaceted_talent",
            title = "বাৰটাতকৈও অধিক বাদ্যযন্ত্ৰৰ নিপুণ শিল্পী",
            assameseText = "জুবিন গাৰ্গ কেৱল এগৰাকী অনন্য কণ্ঠশিল্পীয়েই নাছিল, তেওঁ আছিল এজন দক্ষ বাদ্যযন্ত্ৰ বাদক। ঢোল, পেঁপা, গগনা, বাঁহী, দোতাৰা, তবলা, গীটাৰ, কী-ব’ৰ্ড, ড্ৰাম আদি বাৰটাতকৈও অধিক দেশী-বিদেশী বাদ্যযন্ত্ৰ তেওঁ নিপুণভাৱে বজাব পাৰিছিল। ষ্টুডিঅ’ত সংগীত বাণীবন্ধনৰ সময়ত প্ৰায়ে তেওঁ নিজেই বিভিন্ন বাদ্য বজাই সংগীতৰ স্বকীয়তা প্ৰদান কৰিছিল।",
            sourceName = "অসম সাংস্কৃতিক সঞ্চালকালয় তথ্যকোষ",
            sourceReference = "অসমৰ বাদ্যযন্ত্ৰ আৰু শিল্পী প্ৰতিভা সমীক্ষা",
            category = "বহুমুখী সংগীত প্ৰতিভা"
        ),

        // 5. অসমীয়া সংগীতলৈ অৱদান
        ZubeenStory(
            id = "story_05_contribution_to_music",
            title = "অসমীয়া আধুনিক আৰু বিহু সুৰৰ নৱ-ৰূপায়ণ",
            assameseText = "বিহু সংগীত আৰু লোকসংগীতক আধুনিক বাদ্যযন্ত্ৰৰ সৈতে সংযোজন কৰি বিশ্বমুখী কৰাত জুবিন গাৰ্গৰ অৱদান অতুলনীয়। ‘হিয়া দিয়া নিয়া’, ‘নায়ক’, ‘দাগ’, ‘কন্যাদান’ আদি এলবাম আৰু ছবিৰ জৰিয়তে তেওঁ অসমীয়া সংগীতক এক নতুন মাত্ৰা প্ৰদান কৰিছিল। লোকসংগীতৰ ঐতিহ্য ৰক্ষা কৰিও আধুনিক যুৱ প্ৰজন্মৰ মন জয় কৰাৰ এক অনন্য কৌশল তেওঁৰ আছিল।",
            sourceName = "অসম সাহিত্য সভা বাৰ্তা",
            sourceReference = "কুৰি শতিকাৰ অসমীয়া সংগীতৰ বিকাশ আৰু গতিধাৰা",
            category = "অসমীয়া সংগীতলৈ অৱদান"
        ),

        // 6. চলচ্চিত্ৰৰ সৈতে জুবিন
        ZubeenStory(
            id = "story_06_cinema_revolution",
            title = "চলচ্চিত্ৰ জগতত জুবিনৰ সফল যাত্ৰা",
            assameseText = "২০০০ চনত মুক্তিপ্ৰাপ্ত ‘হিয়া দিয়া নিয়া’ চলচ্চিত্ৰৰ সংগীতেৰে অসমীয়া ছবি জগতক পুনৰুজ্জীৱিত কৰাৰ মূল নায়ক আছিল জুবিন গাৰ্গ। পৰৱৰ্তী সময়ত ‘মিছন চাইনা’ (২০১৭) আৰু ‘কাঞ্চনজংঘা’ (২০১৯) চলচ্চিত্ৰ পৰিচালনা আৰু অভিনয় কৰি তেওঁ অসমীয়া চিনেমাক বাণিজ্যিকভাৱে অভূতপূৰ্ব সফলতা প্ৰদান কৰে। অসমীয়া চলচ্চিত্ৰক দৰ্শকৰ মাজলৈ পুনৰ ঘূৰাই অনাত তেওঁ মুখ্য ভূমিকা লৈছিল।",
            sourceName = "অসম চলচ্চিত্ৰ বঁটা আৰু সমীক্ষা",
            sourceReference = "জ্যোতি চিত্ৰবন চলচ্চিত্ৰ কোষ",
            category = "চলচ্চিত্ৰৰ সৈতে জুবিন"
        ),

        // 7. কবি জুবিন
        ZubeenStory(
            id = "story_07_poet_zubeen",
            title = "শব্দৰ সুবাস — কবি জুবিন গাৰ্গ",
            assameseText = "সংগীতৰ সমান্তৰালকৈ জুবিন গাৰ্গ আছিল এগৰাকী সংবেদনশীল কবি। তেওঁৰ প্ৰকাশিত কবিতা পুথি ‘শব্দ’ আৰু ‘অনুৰাগ’ত অসমৰ প্ৰকৃতি, প্ৰেম আৰু সমাজ চেতনাৰ গভীৰ প্ৰতিফলন দেখা যায়। তেওঁৰ বহুতো জনপ্ৰিয় গানৰ কথা তেওঁ নিজেই ৰচনা কৰিছিল, য’ত শব্দৰ কাব্যিক গাঁথনি আৰু জীৱনৰ গভীৰ দৰ্শন পৰিস্ফুট হৈছিল।",
            sourceName = "অসমীয়া সমসাময়িক কবিতা সংকলন",
            sourceReference = "জুবিন গাৰ্গৰ স্বৰচিত কবিতা পুথি ‘শব্দ’",
            category = "কবি জুবিন"
        ),

        // 8. সমাজ আৰু মানুহৰ প্ৰতি জুবিন
        ZubeenStory(
            id = "story_08_philanthropy_social_work",
            title = "‘কলাগুৰু আৰ্ট ফাউণ্ডেশ্যন’ আৰু মানৱ সেৱা",
            assameseText = "জুবিন গাৰ্গে প্ৰতিষ্ঠা কৰা ‘কলাগুৰু আৰ্ট ফাউণ্ডেশ্যন’ৰ জৰিয়তে অসমৰ বানপীড়িত ৰাইজ, দুৰাৰোগ্য ৰোগত আক্ৰান্ত লোক আৰু দুখীয়া শিল্পীসকলক নিৰন্তৰ সহায় আগবঢ়োৱা হৈছিল। সোণাপুৰত জৈৱিক কৃষি কাৰ্য্যৰে নতুন প্ৰজন্মক স্বাৱলম্বনৰ বাট দেখুওৱা আৰু ক’ভিডকালীন সময়ত আৰ্তজনক খাদ্য-সামগ্ৰী যোগান ধৰাত তেওঁ সদায় অগ্ৰণী ভূমিকা পালন কৰিছিল।",
            sourceName = "অসম মানৱ কল্যাণ প্ৰতিবেদন",
            sourceReference = "কলাগুৰু আৰ্ট ফাউণ্ডেশ্যনৰ সেৱামূলক কাম-কাজৰ খতিয়ান",
            category = "সমাজ আৰু মানুহৰ প্ৰতি জুবিন"
        ),

        // 9. নতুন প্ৰজন্মৰ ওপৰত প্ৰভাৱ
        ZubeenStory(
            id = "story_09_youth_inspiration",
            title = "নতুন প্ৰজন্মৰ প্ৰেৰণা আৰু ভাষা-সংস্কৃতিৰ প্ৰতি শ্ৰদ্ধা",
            assameseText = "অসমৰ কোটি কোটি যুৱক-যুৱতীক নিজৰ ভাষা, গামোচা আৰু সংস্কৃতিৰ প্ৰতি গৌৰৱ অনুভৱ কৰিবলৈ জুবিন গাৰ্গে উদ্বুদ্ধ কৰিছিল। তেওঁৰ স্পষ্টবাদিতা, মাটিৰ প্ৰতি টান আৰু সংগীতৰ প্ৰতি থকা অসীম সমৰ্পণে যুৱ সমাজক সদায় আত্মবিশ্বাসী হৈ নিজৰ ভেটি শক্তিশালী কৰিবলৈ অনুপ্ৰাণিত কৰিছিল।",
            sourceName = "অসম যুৱ সাংস্কৃতিক সমীক্ষা",
            sourceReference = "অসমীয়া সংস্কৃতিৰ আত্মপ্ৰত্যয় আৰু জুবিন গাৰ্গ",
            category = "নতুন প্ৰজন্মৰ ওপৰত প্ৰভাৱ"
        ),

        // 10. জুবিনৰ সাংস্কৃতিক যাত্ৰা
        ZubeenStory(
            id = "story_10_cultural_journey",
            title = "লোক-সংস্কৃতি আৰু বৰগীতক বিশ্বমঞ্চত প্ৰতিষ্ঠা",
            assameseText = "মহাপুৰুষ শ্ৰীমন্ত শংকৰদেৱ-মাধৱদেৱৰ সৃষ্টি বৰগীত, টোকাৰী গীত, কামৰূপী লোকগীত আৰু গোৱালপৰীয়া লোকগীতক জুবিন গাৰ্গে অতি শ্ৰদ্ধাৰে নতুন প্ৰজন্মৰ কাষলৈ লৈ গৈছিল। সমগ্ৰ ভাৰতবৰ্ষৰ লগতে আমেৰিকা, লণ্ডন, ডুবাই আদি বিভিন্ন দেশৰ মঞ্চত অসমৰ বিহু আৰু লোকসংগীত পৰিৱেশন কৰি তেওঁ অসমৰ সাংস্কৃতিক ঐতিহ্য বিশ্বৰ বুকুত তুলি ধৰিছিল।",
            sourceName = "সদৌ অসম সাংস্কৃতিক মঞ্চ",
            sourceReference = "অসমীয়া লোকসংগীতৰ বিশ্বযাত্ৰা",
            category = "জুবিনৰ সাংস্কৃতিক যাত্ৰা"
        ),

        // 11. তেওঁৰ সৃষ্টিশীলতা আৰু সংগীতৰ পৰীক্ষা-নিৰীক্ষা
        ZubeenStory(
            id = "story_11_musical_experimentation",
            title = "সুৰৰ বৈচিত্ৰ্য আৰু অভিনৱ সংমিশ্ৰণ",
            assameseText = "পৰম্পৰাগত পেঁপা আৰু ঢোলৰ মাতৰ সৈতে আধুনিক ৰক আৰু একাউষ্টিক গীটাৰৰ সংমিশ্ৰণ ঘটাই জুবিনে সৃষ্টি কৰিছিল এক স্বতন্ত্ৰ শৈলী। তেওঁ কেতিয়াও কোনো এটা নিৰ্দিষ্ট ধাৰাতে আবদ্ধ হৈ নাথাকিল; ক্লাছিকেল, পপ, ছুফী, ফক্‌ আদি সকলো ধাৰাতেই সমানে সৃষ্টিশীল পৰীক্ষা-নিৰীক্ষা চলাই অসমীয়া সংগীতৰ পৰিসৰ বৃদ্ধি কৰিছিল।",
            sourceName = "অসমীয়া আধুনিক সংগীত কোষ",
            sourceReference = "সংগীত পৰিচালনাৰ বিৱৰ্তন আৰু আধুনিকতা",
            category = "তেওঁৰ সৃষ্টিশীলতা আৰু সংগীতৰ পৰীক্ষা-নিৰীক্ষা"
        ),

        // 12. জুবিনৰ স্মৃতি আৰু অসমীয়া সমাজত তেওঁৰ স্থান
        ZubeenStory(
            id = "story_12_eternal_legacy",
            title = "অসমৰ হৃদয়ত চিৰপ্ৰবাহমান জুবিন দা",
            assameseText = "জাতি-জনগোষ্ঠী, ধৰ্ম-বৰ্ণ নিৰ্বিশেষে সমগ্ৰ অসমবাসীক সংগীতৰ এনাজৰীৰে বান্ধি ৰখা এজন বিৰল ব্যক্তি আছিল জুবিন গাৰ্গ। অসমীয়া মানুহৰ সুখত, দুখত, আৱেগত আৰু সংগ্ৰামত জুবিনৰ কণ্ঠ সদায় এক অবিচ্ছেদ্য অংগ হৈ থাকিব। তেওঁৰ সৃষ্টিৰাজি আৰু মানবীয় আদৰ্শই চিৰকাল অসমৰ আকাশ-বতাহক সুৰভিত কৰি ৰাখিব।",
            sourceName = "অসমীয়া স্মৃতি আৰু সমাজ সাহিত্য",
            sourceReference = "অসমীয়া জাতিৰ সাংস্কৃতিক একতাৰ প্ৰতীক জুবিন গাৰ্গ",
            category = "জুবিনৰ স্মৃতি আৰু অসমীয়া সমাজত তেওঁৰ স্থান"
        )
    )

    private val lock = Any()
    private var shuffledIndices: MutableList<Int> = mutableListOf()
    private var currentDeckPointer: Int = 0

    init {
        resetDeck(null)
    }

    /**
     * Resets and reshuffles the deck, ensuring the new first story does not repeat the last story.
     */
    private fun resetDeck(lastStoryId: String?) {
        val indices = stories.indices.toMutableList()
        indices.shuffle()

        if (lastStoryId != null && stories.size > 1 && stories[indices.first()].id == lastStoryId) {
            val swapIndex = if (indices.size > 1) 1 else 0
            val temp = indices[0]
            indices[0] = indices[swapIndex]
            indices[swapIndex] = temp
        }

        shuffledIndices = indices
        currentDeckPointer = 0
    }

    /**
     * Retrieves the next story in the non-repeating shuffled deck.
     */
    fun getNextStory(): ZubeenStory = synchronized(lock) {
        if (stories.isEmpty()) {
            throw IllegalStateException("Stories catalogue is empty")
        }

        if (currentDeckPointer >= shuffledIndices.size) {
            val lastStoryId = if (shuffledIndices.isNotEmpty()) stories[shuffledIndices.last()].id else null
            resetDeck(lastStoryId)
        }

        val storyIndex = shuffledIndices[currentDeckPointer]
        currentDeckPointer++
        return stories[storyIndex]
    }

    /**
     * Gets a random initial story or the first in the current shuffled deck.
     */
    fun getInitialStory(): ZubeenStory = synchronized(lock) {
        if (currentDeckPointer == 0 && shuffledIndices.isNotEmpty()) {
            val storyIndex = shuffledIndices[0]
            currentDeckPointer = 1
            return stories[storyIndex]
        }
        return getNextStory()
    }

    /**
     * Resets the shuffled deck pointer for deterministic unit testing.
     */
    fun resetDeckForTesting(lastStoryId: String? = null) = synchronized(lock) {
        resetDeck(lastStoryId)
    }

    /**
     * Returns the full immutable list of curated Assamese stories.
     */
    fun getAllStories(): List<ZubeenStory> = stories
}
