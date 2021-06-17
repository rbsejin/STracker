package com.example.stracker.ui.calendar

import android.view.View
import android.widget.TextView
import androidx.collection.LongSparseArray
import androidx.collection.contains
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.stracker.R
import com.example.stracker.TaskManager
import com.example.stracker.TaskTime
import timber.log.Timber
import java.text.DateFormat
import java.util.*

class CalendarViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }
    val text: LiveData<String> = _text

    private val _allEvents = MutableLiveData<LongSparseArray<MutableList<Event>>>()
    val allEvents: LiveData<LongSparseArray<MutableList<Event>>>
        get() = _allEvents

    init {
        _allEvents.value = LongSparseArray()

        val taskTimes = mutableListOf<TaskTime>()

        for (task in TaskManager.tasks) {
            taskTimes.addAll(task.getTaskTimes())
        }

        taskTimes.sortByDescending { taskTime ->
            taskTime.startDateTime
        }

        for (taskTime in taskTimes) {
            val calendar: Calendar = Calendar.getInstance()
            calendar.time = taskTime.startDateTime.clone() as Date

            val hour = calendar[Calendar.HOUR_OF_DAY]
            val minute = calendar[Calendar.MINUTE]

            val timeInMillis = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            if (!_allEvents.value!!.contains(timeInMillis)) {
                _allEvents.value!!.put(timeInMillis, mutableListOf())
            }

            val events: MutableList<Event> = _allEvents.value!![timeInMillis]!!
            val title = if (taskTime.getContent().isEmpty()) "제목없음" else taskTime.getContent()
            Timber.i(taskTime.startDateTime.toString())
            Timber.i("$title : $hour")
            events.add(Event(title, "", hour, minute, taskTime.getTime().toInt() / 60, android.R.color.holo_red_dark, taskTime))
        }

//        val events: MutableList<Event> = _allEvents.value!![Calendar.getInstance().apply {
//            set(Calendar.HOUR_OF_DAY, 0)
//            set(Calendar.MINUTE, 0)
//            set(Calendar.SECOND, 0)
//            set(Calendar.MILLISECOND, 0)
//        }.timeInMillis]!!
//        events.add(Event("1", "응용1단계", 0, 0, 90, android.R.color.holo_red_dark))
//        events.add(Event("2", "응용1단계", 3, 30, 90, android.R.color.holo_red_dark))
//        events.add(Event("3", "응용1단계", 22, 0, 90, android.R.color.holo_red_dark))

//        val events2: MutableList<Event> = mutableListOf()
//        events2.add(Event("11", "응용1단계", 0, 0, 90, android.R.color.holo_red_dark))
//        events2.add(Event("22", "응용1단계", 3, 30, 90, android.R.color.holo_red_dark))
//        events2.add(Event("33", "응용1단계", 22, 0, 90, android.R.color.holo_red_dark))
//
//        _allEvents.value!!.put(Calendar.getInstance().apply {
//            add(Calendar.DAY_OF_MONTH, 1)
//            set(Calendar.HOUR_OF_DAY, 0)
//            set(Calendar.MINUTE, 0)
//            set(Calendar.SECOND, 0)
//            set(Calendar.MILLISECOND, 0)
//        }.timeInMillis, events2)
    }

    fun add(timeInMillis: Long, event: Event) {
        var events = _allEvents.value!![timeInMillis]
        if (events == null) {
            events =
                ArrayList()
            _allEvents.value!!.put(timeInMillis, events)
        }
        events.add(event)
    }
}
