package com.example.stracker

import android.annotation.SuppressLint
import java.text.SimpleDateFormat

object TaskManager {
    private val tasks = mutableListOf<Task>()

    fun getTasks(): MutableList<Task> {
        return tasks
    }

    @SuppressLint("SimpleDateFormat")
    fun load(taskDTOs: List<TaskDTO>, taskTimeDTOs: List<TaskTimeDTO>) {
        tasks.clear()

        for (taskDTO in taskDTOs) {
//            if (!tasks.any { task: Task ->
//                    task.id == taskDTO.id
//                }) {

                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val created = simpleDateFormat.parse(taskDTO.created)!!
                val task = Task(taskDTO.id, null, taskDTO.content, created)

                for (taskTimeDTO in taskTimeDTOs) {
                    if (taskTimeDTO.taskId != task.id) {
                        continue
                    }

                    if (taskTimeDTO.endDateTime == null) {
                        continue
                    }

                    val startDateTime = simpleDateFormat.parse(taskTimeDTO.startDateTime)!!
                    val endDateTime = simpleDateFormat.parse(taskTimeDTO.endDateTime)
                    val taskTime = TaskTime(taskTimeDTO.id, startDateTime, endDateTime, task)

                    task.getTaskTimes().add(taskTime)
                }

                tasks.add(task)
//            }
        }

        tasks.sortByDescending { task: Task ->
            task.id
        }
    }
}