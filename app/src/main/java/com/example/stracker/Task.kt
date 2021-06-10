package com.example.stracker

class Task(val id: Long, val project: Project?, val content: String) {
    private val taskTimeMap = mutableMapOf<Long, TaskTime>()

    fun put(id: Long, taskTime: TaskTime) {
        taskTimeMap[id] = taskTime
    }

    fun contains(id: Long): Boolean {
        return taskTimeMap.contains(id)
    }

    fun getChildCount(): Int {
        return taskTimeMap.size
    }
}
