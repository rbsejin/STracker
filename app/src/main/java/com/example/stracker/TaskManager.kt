package com.example.stracker

import android.annotation.SuppressLint
import java.text.SimpleDateFormat

object TaskManager {
    private val taskMap = mutableMapOf<Long, Task>()

    fun getTaskMap(): MutableMap<Long, Task> {
        return taskMap
    }

    @SuppressLint("SimpleDateFormat")
    fun load(taskDTOs: List<TaskDTO>, taskTimeDTOs: List<TaskTimeDTO>) {
        for (taskDTO in taskDTOs) {
            if (!taskMap.contains(taskDTO.id)) {
                val task = Task(taskDTO.id, null, taskDTO.content)

                for (taskTimeDTO in taskTimeDTOs) {
                    if (taskTimeDTO.taskId != task.id) {
                        continue
                    }

                    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val startDateTime = simpleDateFormat.parse(taskTimeDTO.startDateTime)!!
                    val endDateTime = simpleDateFormat.parse(taskTimeDTO.endDateTime)
                    val taskTime = TaskTime(taskTimeDTO.id, startDateTime, endDateTime, task)

                    task.put(taskTime.id, taskTime)
                }

                taskMap[taskDTO.id] = task
            }
        }
    }
}