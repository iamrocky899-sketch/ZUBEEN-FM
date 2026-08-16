package com.amairatech.zubeenfm.ui.tribute

import com.amairatech.zubeenfm.data.model.ZubeenFact
import com.amairatech.zubeenfm.data.model.ZubeenStory

/**
 * UI State for the Zubeen FM Tribute (শ্ৰদ্ধাঞ্জলী) memorial screen.
 */
data class TributeUiState(
    val isFactVisible: Boolean = false,
    val currentFact: ZubeenFact? = null,
    val currentStory: ZubeenStory? = null,
    val secondsElapsed: Int = 0,
    val isScreenVisible: Boolean = true
)
