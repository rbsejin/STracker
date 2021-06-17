package com.example.stracker.ui.calendar

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.stracker.R
import com.example.stracker.TaskTime
import com.example.stracker.databinding.FragmentCalendarBinding
import com.example.stracker.ui.calendar.DayView.EventTimeRange
import java.text.DateFormat
import java.util.*


class CalendarFragment : Fragment() {
    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var binding: FragmentCalendarBinding

    private lateinit var day: Calendar
    private lateinit var dateFormat: DateFormat
    private lateinit var timeFormat: DateFormat
    private lateinit var editEventDate: Calendar
    private lateinit var editEventStartTime: Calendar
    private lateinit var editEventEndTime: Calendar
    private var editEventDraft: Event? = null

    private lateinit var content: ViewGroup
    private lateinit var dateTextView: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var dayView: DayView

    private val calendar = Calendar.getInstance()
    private var currentMonth = 0
    private var currentDay = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        calendarViewModel =
            ViewModelProvider(this).get(CalendarViewModel::class.java)

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_calendar, container, false
        )

        content = binding.sampleContent
        dateTextView = binding.sampleDate
        scrollView = binding.sampleScroll
        dayView = binding.sampleDay

        // Create a new calendar object set to the start of today

//        calendarViewModel.dateAndEventList.observe(viewLifecycleOwner, Observer {
//
//        })

//        allEvents = LongSparseArray()
//        allEvents.put(day.timeInMillis, ArrayList(listOf(*INITIAL_EVENTS)))

//        for (entry in calendarViewModel.allEvents.value!!) {
//            val c = Calendar.getInstance()
//            c.time = entry.first
//            c.set(Calendar.HOUR_OF_DAY, 0)
//            c.set(Calendar.MINUTE, 0)
//            c.set(Calendar.SECOND, 0)
//            c.set(Calendar.MILLISECOND, 0)
//            allEvents.put(c.timeInMillis, entry.second)
//        }

        day = Calendar.getInstance()
        day.set(Calendar.HOUR_OF_DAY, 0)
        day.set(Calendar.MINUTE, 0)
        day.set(Calendar.SECOND, 0)
        day.set(Calendar.MILLISECOND, 0)

        // Populate today's entry in the map with a list of example events
        dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())

        // Inflate a label view for each hour the day view will display
        val hour = day.clone() as Calendar
        val hourLabelViews: MutableList<View> = ArrayList()
        for (i in dayView.startHour..dayView.endHour) {
            hour[Calendar.HOUR_OF_DAY] = i
            val hourLabelView =
                layoutInflater.inflate(R.layout.hour_label, dayView, false) as TextView
            hourLabelView.text = timeFormat.format(hour.time)
            hourLabelViews.add(hourLabelView)
        }
        dayView.setHourLabelViews(hourLabelViews)
        onDayChange()

        // region Button ClickListener
        binding.previousButton.setOnClickListener {
            day.add(Calendar.DAY_OF_YEAR, -1)
            onDayChange()
        }

        binding.nextButton.setOnClickListener {
            day.add(Calendar.DAY_OF_YEAR, 1)
            onDayChange()
        }

        binding.addButton.setOnClickListener {
            editEventDate = day.clone() as Calendar
            editEventStartTime = day.clone() as Calendar
            editEventEndTime = day.clone() as Calendar
            editEventEndTime.add(Calendar.MINUTE, 30)
            showEditEventDialog(false, null, null, android.R.color.holo_red_dark)
        }

        binding.scrollButton.setOnClickListener {
            showScrollTargetDialog()
        }

        // endregion

        binding.weekCalendar.setOnDateClickListener { dateTime ->
            day.set(Calendar.YEAR, dateTime.year)
            day.set(Calendar.MONTH, dateTime.monthOfYear - 1)
            day.set(Calendar.DAY_OF_MONTH, dateTime.dayOfMonth)
            onDayChange()
        }

        calendarViewModel.allEvents.observe(viewLifecycleOwner, {
            onEventsChange()
        })

        scrollView.post {
            run {
                scrollToCurrentTime()
            }
        }

        return binding.root
    }

    private fun onDayChange() {
        dateTextView.text = dateFormat.format(day.time)
        onEventsChange()
//        scrollToCurrentTime()
    }

    private fun onEventsChange() {
        // The day view needs a list of event views and a corresponding list of event time ranges
        var eventViews: MutableList<View?>? = null
        var eventTimeRanges: MutableList<EventTimeRange?>? = null
        val events: MutableList<Event>? = calendarViewModel.allEvents.value!![day.timeInMillis]
        if (events != null) {
            // Sort the events by start time so the layout happens in correct order
            events.sortWith { o1, o2 ->
                when {
                    o1!!.hour < o2!!.hour -> -1
                    o1.hour == o2.hour -> {
                        when {
                            o1.minute < o2.minute -> -1
                            o1.minute == o2.minute -> 0
                            else -> 1
                        }
                    }
                    else -> {
                        1
                    }
                }
            }

            eventViews = ArrayList()
            eventTimeRanges = ArrayList()

            // Reclaim all of the existing event views so we can reuse them if needed, this process
            // can be useful if your day view is hosted in a recycler view for example
            val recycled = dayView.removeEventViews()
            var remaining = recycled?.size ?: 0
            for (event in events) {
                // Try to recycle an existing event view if there are enough left, otherwise inflate
                // a new one
                val eventView =
                    if (remaining > 0) {
                        recycled!![--remaining]
                    } else {
                        try {
                            layoutInflater.inflate(R.layout.event, dayView, false)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            return
                        }
                    }

                (eventView.findViewById<View>(R.id.event_title) as TextView).text =
                    event.title
                (eventView.findViewById<View>(R.id.event_location) as TextView).text =
                    event.project
                (eventView.findViewById<View>(R.id.event_duration) as TextView).text =
                    event.taskTime?.getTimeString()
                eventView.setBackgroundColor(resources.getColor(event.color))

                // When an event is clicked, start a new draft event and show the edit event dialog
                eventView.setOnClickListener {
                    editEventDraft = event
                    editEventDate = day.clone() as Calendar
                    editEventStartTime = Calendar.getInstance()
                    editEventStartTime.set(Calendar.HOUR_OF_DAY, editEventDraft!!.hour)
                    editEventStartTime.set(Calendar.MINUTE, editEventDraft!!.minute)
                    editEventStartTime.set(Calendar.SECOND, 0)
                    editEventStartTime.set(Calendar.MILLISECOND, 0)
                    editEventEndTime = editEventStartTime.clone() as Calendar
                    editEventEndTime.add(Calendar.MINUTE, editEventDraft!!.duration)
                    showEditEventDialog(
                        true,
                        editEventDraft!!.title,
                        editEventDraft!!.project,
                        editEventDraft!!.color
                    )
                }
                eventViews.add(eventView)

                // The day view needs the event time ranges in the start minute/end minute format,
                // so calculate those here
                val startMinute = 60 * event.hour + event.minute
                val endMinute = startMinute + event.duration
                eventTimeRanges.add(EventTimeRange(startMinute, endMinute))
            }
        }

        // Update the day view with the new events
        dayView.setEventViews(eventViews, eventTimeRanges)
    }

    private fun showEditEventDialog(
        eventExists: Boolean,
        eventTitle: String?,
        eventLocation: String?,
        @ColorRes eventColor: Int
    ) {
        val view = layoutInflater.inflate(R.layout.edit_event_dialog, content, false)
        val titleTextView = view.findViewById<TextView>(R.id.edit_event_title)
        val locationTextView = view.findViewById<TextView>(R.id.edit_event_location)
        val dateButton = view.findViewById<Button>(R.id.edit_event_date)
        val startTimeButton = view.findViewById<Button>(R.id.edit_event_start_time)
        val endTimeButton = view.findViewById<Button>(R.id.edit_event_end_time)
        val redRadioButton = view.findViewById<RadioButton>(R.id.edit_event_red)
        val blueRadioButton = view.findViewById<RadioButton>(R.id.edit_event_blue)
        val orangeRadioButton = view.findViewById<RadioButton>(R.id.edit_event_orange)
        val greenRadioButton = view.findViewById<RadioButton>(R.id.edit_event_green)
        val purpleRadioButton = view.findViewById<RadioButton>(R.id.edit_event_purple)

        titleTextView.text = eventTitle
        locationTextView.text = eventLocation
        dateButton.text = dateFormat.format(editEventDate.time)
        dateButton.setOnClickListener {
            val listener =
                OnDateSetListener { view, year, month, dayOfMonth ->
                    editEventDate[Calendar.YEAR] = year
                    editEventDate[Calendar.MONTH] = month
                    editEventDate[Calendar.DAY_OF_MONTH] = dayOfMonth
                    dateButton.text = dateFormat.format(editEventDate.time)
                }
            DatePickerDialog(
                requireContext(),
                listener,
                day[Calendar.YEAR],
                day[Calendar.MONTH],
                day[Calendar.DAY_OF_MONTH]
            ).show()
        }
        startTimeButton.text = timeFormat.format(editEventStartTime.time)
        startTimeButton.setOnClickListener {
            val listener =
                OnTimeSetListener { view, hourOfDay, minute ->
                    editEventStartTime[Calendar.HOUR_OF_DAY] = hourOfDay
                    editEventStartTime[Calendar.MINUTE] = minute
                    startTimeButton.text = timeFormat.format(editEventStartTime.time)
                    if (!editEventEndTime.after(editEventStartTime)) {
                        editEventEndTime = editEventStartTime.clone() as Calendar
                        editEventEndTime.add(Calendar.MINUTE, 30)
                        endTimeButton.text = timeFormat.format(editEventEndTime.time)
                    }
                }
            TimePickerDialog(
                context,
                listener,
                editEventStartTime[Calendar.HOUR_OF_DAY],
                editEventStartTime[Calendar.MINUTE],
                android.text.format.DateFormat.is24HourFormat(
                    context
                )
            ).show()
        }

        endTimeButton.text = timeFormat.format(editEventEndTime.time)
        endTimeButton.setOnClickListener {
            val listener =
                OnTimeSetListener { view, hourOfDay, minute ->
                    editEventEndTime[Calendar.HOUR_OF_DAY] = hourOfDay
                    editEventEndTime[Calendar.MINUTE] = minute
                    if (!editEventEndTime.after(editEventStartTime)) {
                        editEventEndTime = editEventStartTime.clone() as Calendar
                        editEventEndTime.add(Calendar.MINUTE, 30)
                    }
                    endTimeButton.text = timeFormat.format(editEventEndTime.time)
                }
            TimePickerDialog(
                context,
                listener,
                editEventEndTime[Calendar.HOUR_OF_DAY],
                editEventEndTime[Calendar.MINUTE],
                android.text.format.DateFormat.is24HourFormat(
                    context
                )
            ).show()
        }

        when (eventColor) {
            android.R.color.holo_blue_dark -> {
                blueRadioButton.isChecked = true
            }
            android.R.color.holo_orange_dark -> {
                orangeRadioButton.isChecked = true
            }
            android.R.color.holo_green_dark -> {
                greenRadioButton.isChecked = true
            }
            android.R.color.holo_purple -> {
                purpleRadioButton.isChecked = true
            }
            else -> {
                redRadioButton.isChecked = true
            }
        }

        val builder = AlertDialog.Builder(
            requireContext()
        )

        // If the event already exists, we are editing it, otherwise we are adding a new event
        builder.setTitle(if (eventExists) R.string.edit_event else R.string.add_event)

        // When the event changes are confirmed, read the new values from the dialog and then add
        // this event to the list
        builder.setPositiveButton(
            android.R.string.ok
        ) { dialog, which ->
            val title = titleTextView.text.toString()
            val location = locationTextView.text.toString()
            val hour = editEventStartTime[Calendar.HOUR_OF_DAY]
            val minute = editEventStartTime[Calendar.MINUTE]
            val duration =
                (editEventEndTime.timeInMillis - editEventStartTime.timeInMillis).toInt() / 60000
            @ColorRes val color: Int
            color = when {
                blueRadioButton.isChecked -> {
                    android.R.color.holo_blue_dark
                }
                orangeRadioButton.isChecked -> {
                    android.R.color.holo_orange_dark
                }
                greenRadioButton.isChecked -> {
                    android.R.color.holo_green_dark
                }
                purpleRadioButton.isChecked -> {
                    android.R.color.holo_purple
                }
                else -> {
                    android.R.color.holo_red_dark
                }
            }

            // region 임시
            val taskTime = editEventDraft!!.taskTime!!
            taskTime.task.content = title
//            taskTime.task.project = null
            taskTime.startDateTime = editEventStartTime.time
            taskTime.endDateTime = editEventStartTime.time


            // endregion

            calendarViewModel.add(
                editEventDate.timeInMillis,
                Event(title, location, hour, minute, duration, color, taskTime)
            )
            onEditEventDismiss(true)
        }
        builder.setNegativeButton(
            android.R.string.cancel
        ) { dialog, which -> onEditEventDismiss(false) }

        // If the event already exists, provide a delete option
        if (eventExists) {
            builder.setNeutralButton(
                R.string.edit_event_delete
            ) { dialog, which -> onEditEventDismiss(true) }
        }
        builder.setOnCancelListener { onEditEventDismiss(false) }
        builder.setView(view)
        builder.show()
    }

    private fun showScrollTargetDialog() {
        val view = layoutInflater.inflate(R.layout.scroll_target_dialog, content, false)
        val timeButton = view.findViewById<Button>(R.id.scroll_target_time)
        val firstEventTopButton = view.findViewById<Button>(R.id.scroll_target_first_event_top)
        val firstEventBottomButton =
            view.findViewById<Button>(R.id.scroll_target_first_event_bottom)
        val lastEventTopButton = view.findViewById<Button>(R.id.scroll_target_last_event_top)
        val lastEventBottomButton = view.findViewById<Button>(R.id.scroll_target_last_event_bottom)
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.scroll_to)
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.setView(view)
        val dialog = builder.show()
        timeButton.setOnClickListener {
            val listener =
                OnTimeSetListener { view, hourOfDay, minute ->
                    val top = dayView.getHourTop(hourOfDay)
                    val bottom = dayView.getHourBottom(hourOfDay)
                    val y = top + (bottom - top) * minute / 60
                    scrollView.smoothScrollTo(0, y)
                    dialog.dismiss()
                }
            TimePickerDialog(
                context, listener, 0, 0, android.text.format.DateFormat.is24HourFormat(
                    context
                )
            ).show()
        }
        firstEventTopButton.setOnClickListener {
            scrollView.smoothScrollTo(0, dayView.firstEventTop)
            dialog.dismiss()
        }
        firstEventBottomButton.setOnClickListener {
            scrollView.smoothScrollTo(0, dayView.firstEventBottom)
            dialog.dismiss()
        }
        lastEventTopButton.setOnClickListener {
            scrollView.smoothScrollTo(0, dayView.lastEventTop)
            dialog.dismiss()
        }
        lastEventBottomButton.setOnClickListener {
            scrollView.smoothScrollTo(0, dayView.lastEventBottom)
            dialog.dismiss()
        }
    }

    private fun onEditEventDismiss(modified: Boolean) {
        if (modified && editEventDraft != null) {
            val events: MutableList<Event> = calendarViewModel.allEvents.value!![day.timeInMillis]!!
            events.remove(editEventDraft!!)
        }
        editEventDraft = null
        onEventsChange()
    }

    private fun scrollToCurrentTime() {
//        val hourOfDay = calendar[Calendar.HOUR_OF_DAY]
//        val minute = calendar[Calendar.MINUTE]
        val hourOfDay = 9
        val minute = 0
        val top = dayView.getHourTop(hourOfDay)
        val bottom = dayView.getHourBottom(hourOfDay)
        val y = top + (bottom - top) * minute / 60 - 50
        scrollView.smoothScrollTo(0, y)
    }
}