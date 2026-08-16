package com.amairatech.zubeenfm

import com.amairatech.zubeenfm.data.repository.ZubeenFactRepository
import com.amairatech.zubeenfm.ui.tribute.TributeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TributeSystemTest {

    @Test
    fun testAllFactsAreStrictlyAssameseAndContainNoBengaliScript() {
        val bengaliRa = '\u09B0' // Bengali letter 'র'

        for (fact in ZubeenFactRepository.verifiedFacts) {
            val text = fact.factAssamese
            val category = fact.categoryAssamese

            // Strict check: No Bengali Ra character in fact text
            assertFalse(
                "Fact [${fact.id}] contains Bengali letter 'র' (\\u09B0): $text",
                text.contains(bengaliRa)
            )

            // Strict check: No Bengali Ra character in category
            assertFalse(
                "Fact category [${fact.categoryAssamese}] contains Bengali letter 'র' (\\u09B0)",
                category.contains(bengaliRa)
            )

            // Ensure no Bengali grammatical words
            assertFalse("Found Bengali 'হয়েছিল' in $text", text.contains("হয়েছিল"))
            assertFalse("Found Bengali 'ছিলেন' in $text", text.contains("ছিলেন"))
            assertFalse("Found Bengali 'গান গেয়েছেন' in $text", text.contains("গান গেয়েছেন"))
        }
    }

    @Test
    fun testAssameseTributeQuoteOrthography() {
        val tributeLine = "তোমাৰ সুৰে আমাক সদায় জীয়াই থকাৰ সাহস দিব, জুবিন দা।"
        val bengaliRa = '\u09B0'

        assertFalse("Tribute line contains Bengali letter 'র'", tributeLine.contains(bengaliRa))
        assertTrue("Tribute line contains Assamese letter 'ৰ'", tributeLine.contains('\u09F0'))
    }

    @Test
    fun testNonRepeatingFactSelection() {
        ZubeenFactRepository.resetHistory()
        val total = ZubeenFactRepository.verifiedFacts.size
        val selectedIds = mutableSetOf<String>()

        for (i in 0 until total) {
            val fact = ZubeenFactRepository.getNextRandomFact()
            selectedIds.add(fact.id)
        }

        assertEquals("All facts should be selected without repetition in a full cycle", total, selectedIds.size)
    }

    @Test
    fun testTributeViewModelTimerLifecycle() = runBlocking {
        val scope = CoroutineScope(Dispatchers.Default)
        val viewModel = TributeViewModel(
            factRepository = ZubeenFactRepository,
            coroutineScope = scope
        )

        // 1. Initial state
        assertFalse(viewModel.uiState.value.isFactVisible)

        // 2. Visible starts timer
        viewModel.onScreenVisible()
        assertTrue(viewModel.uiState.value.isScreenVisible)

        // 3. User leaves before 10 seconds -> timer cancels immediately
        delay(200L)
        viewModel.onScreenHidden()
        assertFalse(viewModel.uiState.value.isScreenVisible)
        assertFalse(viewModel.uiState.value.isFactVisible)

        // 4. Return to screen -> timer restarts
        viewModel.onScreenVisible()
        assertTrue(viewModel.uiState.value.isScreenVisible)
        viewModel.onScreenHidden()
    }
}
