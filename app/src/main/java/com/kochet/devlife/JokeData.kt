package com.kochet.devlife

import kotlinx.serialization.Serializable

@Serializable
data class JokeData(val id: Int,
                val description: String,
                val votes: Int,
                val author: String,
                val date: String,
                var gifURL: String,
                val gifSize: Long,
                val previewURL: String,
                val videoURL: String,
                val videoSize: Long,
                val type: String,
                val width: Int,
                val height: Int,
                val commentsCount: Int,
                val fileSize: Long,
                val canVote: Boolean)



@Serializable
class JokePage(val result: List<JokeData>)