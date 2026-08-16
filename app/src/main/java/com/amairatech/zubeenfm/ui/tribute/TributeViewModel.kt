package com.amairatech.zubeenfm.ui.tribute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amairatech.zubeenfm.data.repository.ZubeenFactRepository
import com.amairatech.zubeenfm.data.repository.ZubeenStoriesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * ViewModel managing the Tribute screen lifecycle, the 10-second Assamese Fact timer,
 * and the non-repeating Assamese Story system ("জুবিন দাৰ কাহিনী").
 */
class TributeViewModel(
    private val factRepository: ZubeenFactRepository,
    private val storiesRepository: ZubeenStoriesRepository,
    coroutineScope: CoroutineScope?
) : ViewModel() {

    /**
     * Parameterless constructor required by ViewModelProvider.NewInstanceFactory.
     */
    constructor() : this(ZubeenFactRepository, ZubeenStoriesRepository, null)

    constructor(factRepository: ZubeenFactRepository) : this(factRepository, ZubeenStoriesRepository, null)

    constructor(
        factRepository: ZubeenFactRepository,
        coroutineScope: CoroutineScope?
    ) : this(factRepository, ZubeenStoriesRepository, coroutineScope)

    constructor(
        factRepository: ZubeenFactRepository,
        storiesRepository: ZubeenStoriesRepository
    ) : this(factRepository, storiesRepository, null)

    private val activeScope: CoroutineScope = coroutineScope ?: viewModelScope

    private val _uiState = MutableStateFlow(
        TributeUiState(currentStory = storiesRepository.getInitialStory())
    )
    val uiState: StateFlow<TributeUiState> = _uiState.asStateFlow()

    private var factTimerJob: Job? = null

    /**
     * Called when the Tribute screen becomes visible.
     * Starts the 10-second timer to reveal "আপুনি জানেনে ?" and ensures a story is ready.
     */
    fun onScreenVisible() {
        cancelTimer()
        val currentStory = _uiState.value.currentStory ?: storiesRepository.getInitialStory()
        _uiState.update {
            it.copy(
                isScreenVisible = true,
                isFactVisible = false,
                currentFact = null,
                currentStory = currentStory,
                secondsElapsed = 0
            )
        }

        factTimerJob = activeScope.launch {
            var elapsed = 0
            while (isActive && elapsed < 10) {
                delay(1000L)
                elapsed++
                _uiState.update { it.copy(secondsElapsed = elapsed) }
            }

            if (isActive && elapsed >= 10) {
                val nextFact = factRepository.getNextRandomFact()
                _uiState.update {
                    it.copy(
                        isFactVisible = true,
                        currentFact = nextFact
                    )
                }
            }
        }
    }

    /**
     * Called when the user leaves the Tribute screen.
     * Strictly cancels the timer job.
     */
    fun onScreenHidden() {
        cancelTimer()
        _uiState.update {
            it.copy(
                isScreenVisible = false,
                isFactVisible = false,
                currentFact = null,
                secondsElapsed = 0
            )
        }
    }

    fun requestNewFact() {
        val nextFact = factRepository.getNextRandomFact()
        _uiState.update { it.copy(currentFact = nextFact) }
    }

    /**
     * Advances to the next non-repeating Assamese story from the shuffled deck.
     */
    fun loadNextStory() {
        val nextStory = storiesRepository.getNextStory()
        _uiState.update { it.copy(currentStory = nextStory) }
    }

    private fun cancelTimer() {
        factTimerJob?.cancel()
        factTimerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        cancelTimer()
    }
}
