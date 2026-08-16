package com.amairatech.zubeenfm.data.model

/**
 * Verified biographical and musical story about Zubeen Garg.
 * All story titles and texts are strictly written in Assamese.
 */
data class ZubeenStory(
    val id: String,
    val title: String,
    val assameseText: String,
    val sourceName: String,
    val sourceReference: String,
    val category: String
)
