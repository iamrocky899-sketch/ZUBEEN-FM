package com.amairatech.zubeenfm

import com.amairatech.zubeenfm.data.repository.ZubeenFactRepository
import com.amairatech.zubeenfm.data.repository.ZubeenStoriesRepository
import com.amairatech.zubeenfm.ui.tribute.TributeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ZubeenStoriesTest {

    @Test
    fun testAllStoriesLoadedAndContainMandatoryFields() {
        val allStories = ZubeenStoriesRepository.getAllStories()
        assertTrue("Stories catalogue must contain at least 12 stories", allStories.size >= 12)

        for (story in allStories) {
            assertTrue("Story id must not be blank", story.id.isNotBlank())
            assertTrue("Story title must not be blank", story.title.isNotBlank())
            assertTrue("Story assameseText must not be blank", story.assameseText.isNotBlank())
            assertTrue("Story sourceName must not be blank", story.sourceName.isNotBlank())
            assertTrue("Story sourceReference must not be blank", story.sourceReference.isNotBlank())
            assertTrue("Story category must not be blank", story.category.isNotBlank())
        }
    }

    @Test
    fun testStoriesStrictlyAssameseLanguageScript() {
        val allStories = ZubeenStoriesRepository.getAllStories()

        // English alphabet regex check (Story title & body text must not contain Latin letters A-Z / a-z)
        val latinLetterRegex = Regex("[A-Za-z]")

        for (story in allStories) {
            assertFalse(
                "Story title [${story.title}] must not contain Latin / English letters",
                latinLetterRegex.containsMatchIn(story.title)
            )
            assertFalse(
                "Story text for [${story.title}] must not contain Latin / English letters",
                latinLetterRegex.containsMatchIn(story.assameseText)
            )

            // Verify Assamese / Bengali-Assamese Unicode script block (\u0980-\u09FF) is present
            val assameseScriptRegex = Regex("[\u0980-\u09FF]")
            assertTrue(
                "Story title [${story.title}] must contain Assamese script characters",
                assameseScriptRegex.containsMatchIn(story.title)
            )
            assertTrue(
                "Story text for [${story.title}] must contain Assamese script characters",
                assameseScriptRegex.containsMatchIn(story.assameseText)
            )
        }
    }

    @Test
    fun testStoriesCoverAllRequiredCategories() {
        val allStories = ZubeenStoriesRepository.getAllStories()
        val categories = allStories.map { it.category }.toSet()

        val expectedCategories = listOf(
            "শৈশৱ আৰু সংগীতৰ আৰম্ভণি",
            "সংগীত শিক্ষাৰ দিনবোৰ",
            "‘অনামিকা’ আৰু সংগীত জগতত প্ৰৱেশ",
            "বহুমুখী সংগীত প্ৰতিভা",
            "অসমীয়া সংগীতলৈ অৱদান",
            "চলচ্চিত্ৰৰ সৈতে জুবিন",
            "কবি জুবিন",
            "সমাজ আৰু মানুহৰ প্ৰতি জুবিন",
            "নতুন প্ৰজন্মৰ ওপৰত প্ৰভাৱ",
            "জুবিনৰ সাংস্কৃতিক যাত্ৰা",
            "তেওঁৰ সৃষ্টিশীলতা আৰু সংগীতৰ পৰীক্ষা-নিৰীক্ষা",
            "জুবিনৰ স্মৃতি আৰু অসমীয়া সমাজত তেওঁৰ স্থান"
        )

        for (cat in expectedCategories) {
            assertTrue("Category [$cat] must be represented in stories repository", categories.contains(cat))
        }
    }

    @Test
    fun testNonRepeatingShuffledDeckPlaysAllStoriesBeforeRepeating() {
        ZubeenStoriesRepository.resetDeckForTesting()
        val totalStories = ZubeenStoriesRepository.getAllStories().size
        val retrievedIds = mutableListOf<String>()

        for (i in 0 until totalStories) {
            val story = ZubeenStoriesRepository.getNextStory()
            assertNotNull("Retrieved story must not be null", story)
            retrievedIds.add(story.id)
        }

        val uniqueIds = retrievedIds.toSet()
        assertEquals(
            "Shuffled deck must play all stories once without duplicates within a cycle",
            totalStories,
            uniqueIds.size
        )
    }

    @Test
    fun testReshuffleAvoidsImmediateStoryRepeat() {
        ZubeenStoriesRepository.resetDeckForTesting()
        val totalStories = ZubeenStoriesRepository.getAllStories().size

        // Exhaust first deck
        var lastStoryOfDeck1 = ZubeenStoriesRepository.getNextStory()
        for (i in 1 until totalStories) {
            lastStoryOfDeck1 = ZubeenStoriesRepository.getNextStory()
        }

        // Trigger deck 2
        val firstStoryOfDeck2 = ZubeenStoriesRepository.getNextStory()

        assertNotEquals(
            "First story of reshuffled deck must not immediately repeat last story of exhausted deck",
            lastStoryOfDeck1.id,
            firstStoryOfDeck2.id
        )
    }

    @Test
    fun testTributeViewModelStoryIntegration() {
        val viewModel = TributeViewModel(
            factRepository = ZubeenFactRepository,
            storiesRepository = ZubeenStoriesRepository,
            coroutineScope = null
        )

        val initialStory = viewModel.uiState.value.currentStory
        assertNotNull("ViewModel must have initial story loaded", initialStory)

        viewModel.loadNextStory()
        val secondStory = viewModel.uiState.value.currentStory
        assertNotNull("Second story must be loaded", secondStory)
        assertNotEquals("Loading next story must change the current story", initialStory?.id, secondStory?.id)
    }
}

