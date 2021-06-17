package com.example.stracker.ui.calendar

import androidx.annotation.ColorRes
import com.example.stracker.TaskTime

data class Event(
    val title: String?,
    val project: String?,
    val hour: Int,
    val minute: Int,
    val duration: Int,
    @field:ColorRes @param:ColorRes val color: Int,
    val taskTime: TaskTime?
)