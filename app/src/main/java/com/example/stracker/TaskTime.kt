package com.example.stracker

import java.util.*

class TaskTime(
    val id: Long,
    val startDateTime: Date,
    val endDateTime: Date?,
    private val task: Task
) {
    fun getContent(): String {
        return task.content
    }

    fun getTime(): String {
        val time = (endDateTime!!.time - startDateTime.time) / 1000
        val hour = time / 3600
        val min = (time % 3600) / 60
        val second = time % 60
        return String.format("%d:%02d:%02d", hour, min, second)
    }
}