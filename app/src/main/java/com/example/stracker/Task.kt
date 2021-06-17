package com.example.stracker

import java.util.*

class Task(val id: Long, var project: Project?, var content: String, val created: Date) {
    private val taskTimes = mutableListOf<TaskTime>()

    fun getTaskTimes(): MutableList<TaskTime> {
        return taskTimes
    }

    fun getTime(): Long {
        var time = 0L

        for (taskTime in taskTimes) {
            time += taskTime.getTime()
        }

        return time
    }

    fun getTimeString(): String {
        val time = getTime()

        val hour = time / 3600
        val min = (time % 3600) / 60
        val second = time % 60
        return String.format("%d:%02d:%02d", hour, min, second)
    }
}
