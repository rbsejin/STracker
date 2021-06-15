package com.example.stracker

import com.squareup.moshi.Json

data class TaskTimeDTO(
    val id: Long,
    @Json(name = "task_id") val taskId: Long,
    @Json(name = "start_datetime") val startDateTime: String,
    @Json(name = "end_datetime") val endDateTime: String?
)
