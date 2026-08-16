package com.amairatech.zubeenfm.data.model

/**
 * Verified biographical and musical fact about Zubeen Garg.
 * All texts must strictly adhere to the Assamese language and script.
 */
data class ZubeenFact(
    val id: String,
    val factAssamese: String,
    val categoryAssamese: String,
    val milestoneYear: String? = null
)
