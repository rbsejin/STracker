package com.example.stracker

import com.squareup.moshi.Json

data class TaskDTO(
    val id: Long,
    @Json(name = "user_id") val userId: Long,
    val projectId: Long = 0,
    val content: String = "",
    val created: String
)
