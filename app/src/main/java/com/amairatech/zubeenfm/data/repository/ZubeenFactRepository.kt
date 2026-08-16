package com.amairatech.zubeenfm.data.repository

import com.amairatech.zubeenfm.data.model.ZubeenFact

/**
 * Data-driven repository of verified, fact-checked milestones and biographical facts
 * about legendary artist Zubeen Garg.
 *
 * NOTE: All facts are written strictly in Assamese script using proper Assamese characters
 * (\u09F0 'ৰ' and \u09F1 'ৱ'). Bengali script and words are prohibited.
 */
object ZubeenFactRepository {

    val verifiedFacts: List<ZubeenFact> = listOf(
        ZubeenFact(
            id = "fact_1",
            factAssamese = "জুবিন গাৰ্গৰ জন্ম হৈছিল ১৯৭২ চনৰ ১৮ নৱেম্বৰত তুৰাত। বিখ্যাত সুৰকাৰ জুবিন মেহতাৰ নামেৰে তেওঁৰ নাম 'জুবিন' ৰখা হৈছিল।",
            categoryAssamese = "জন্ম আৰু নামাকৰণ",
            milestoneYear = "১৯৭২"
        ),
        ZubeenFact(
            id = "fact_2",
            factAssamese = "১৯৯২ চনত মুক্তি পোৱা 'অনামীকা' আছিল জুবিন গাৰ্গৰ প্ৰথমখন একক এলবাম, যিয়ে সমগ্ৰ অসমৰ সংগীত জগতত এক যুগান্তকাৰী বিপ্লৱৰ সূচনা কৰিছিল।",
            categoryAssamese = "প্ৰথম এলবাম",
            milestoneYear = "১৯৯২"
        ),
        ZubeenFact(
            id = "fact_3",
            factAssamese = "জুবিন গাৰ্গে অসমীয়া, হিন্দী, বড়ো, কাৰ্বি, তিৱা, মিচিং, বাংলা আদিকে ধৰি ৪০টাতকৈও অধিক ভাষাত ৩২,০০০ তকৈও অধিক গীতত কণ্ঠদান কৰিছে।",
            categoryAssamese = "ভাষিক বৈচিত্ৰ্য",
            milestoneYear = "সমগ্ৰ যাত্ৰা"
        ),
        ZubeenFact(
            id = "fact_4",
            factAssamese = "২০০৬ চনৰ হিন্দী ছবি 'গেংষ্টাৰ'ৰ \"য়া আলী\" (Ya Ali) গীতটোৰ অভূতপূৰ্ব সাফল্যৰ জৰিয়তে জুবিন দা সমগ্ৰ বিশ্বজুৰি প্ৰতিধ্বনিত হৈছিল।",
            categoryAssamese = "ৰাষ্ট্ৰীয় খ্যতি",
            milestoneYear = "২০০৬"
        ),
        ZubeenFact(
            id = "fact_5",
            factAssamese = "জুবিন গাৰ্গে তবলা, ঢোল, গীটাৰ, কী-ব’ৰ্ড, মন্দিৰা, দোতাৰা আদিকে ধৰি ১২ বিধৰো অধিক বাদ্যযন্ত্ৰ অতি নিপুণভাৱে নিজেই বজাব পাৰে।",
            categoryAssamese = "বাদ্যযন্ত্ৰ নৈপুণ্য",
            milestoneYear = "সংগীত সাধনা"
        ),
        ZubeenFact(
            id = "fact_6",
            factAssamese = "জুবিন গাৰ্গৰ পৰিচালনা আৰু সংগীতেৰে নিৰ্মিত 'মিছন চাইনা' (২০১৭) আৰু 'কাঞ্চনজংঘা' (২০১৯) অসমীয়া চলচ্চিত্ৰ ইতিহাসৰ সৰ্বাধিক উপাৰ্জনকাৰী ছবিৰ অন্যতম।",
            categoryAssamese = "চলচ্চিত্ৰ পৰিচালনা",
            milestoneYear = "২০১৭-২০১৯"
        ),
        ZubeenFact(
            id = "fact_7",
            factAssamese = "১৯৯২ চনত গুৱাহাটী বিশ্ববিদ্যালয়ৰ যুৱ মহোৎসৱত লোকসংগীতত শ্ৰেষ্ঠত্ব অৰ্জন কৰি সোণৰ পদক লাভ কৰাৰ পিছতেই তেওঁ পেছাদাৰী সংগীত জগতত আত্মপ্ৰকাশ কৰে।",
            categoryAssamese = "প্ৰাৰম্ভিক পুৰস্কাৰ",
            milestoneYear = "১৯৯২"
        ),
        ZubeenFact(
            id = "fact_8",
            factAssamese = "সমাজসেৱাত অগ্ৰণী ভূমিকা পালন কৰা জুবিন গাৰ্গে বানপীড়িত লোকসকলৰ সাহায্য আৰু আৰ্তজনক চিকিৎসা সাহায্য প্ৰদানৰ বাবে 'কলাগুৰু আৰ্টিষ্ট ফাউণ্ডেচন' প্ৰতিষ্ঠা কৰিছিল।",
            categoryAssamese = "সমাজসেৱা",
            milestoneYear = "সমাজহিতৈষী"
        )
    )

    private val shownFactIds = mutableSetOf<String>()

    /**
     * Returns a random fact, prioritizing unshown facts to ensure non-repeating presentation.
     */
    @Synchronized
    fun getNextRandomFact(): ZubeenFact {
        val unshown = verifiedFacts.filter { it.id !in shownFactIds }
        val selected = if (unshown.isNotEmpty()) {
            unshown.random()
        } else {
            shownFactIds.clear()
            verifiedFacts.random()
        }
        shownFactIds.add(selected.id)
        return selected
    }

    @Synchronized
    fun resetHistory() {
        shownFactIds.clear()
    }
}
