package com.example.stracker.ui.reports

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ReportFragment
import androidx.lifecycle.ViewModelProvider
import com.applikeysolutions.cosmocalendar.selection.RangeSelectionManager
import com.applikeysolutions.cosmocalendar.utils.SelectionType
import com.applikeysolutions.cosmocalendar.view.CalendarView
import com.example.stracker.R
import timber.log.Timber
import java.util.*


class CalendarDialogFragment(val reportsFragment: ReportsFragment) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_calendar_dialog, null)

        val calendarView: CalendarView = view.findViewById(R.id.cosmo_calendar)
        val button: Button = view.findViewById(R.id.selectButton)

        //Set First day of the week
        calendarView.firstDayOfWeek = Calendar.MONDAY

        //Set Orientation 0 = Horizontal | 1 = Vertical
        calendarView.calendarOrientation = 0

        calendarView.weekendDays = hashSetOf(
            Calendar.SATURDAY.toLong(),
            Calendar.SUNDAY.toLong()
        )

        calendarView.selectionType = SelectionType.RANGE

        button.setOnClickListener {
        }

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater

            builder.setView(view)
                // Add action buttons
                .setPositiveButton("확인",
                    DialogInterface.OnClickListener { dialog, id ->
                        Timber.i("클릭을 했다!!!")
                        if (calendarView.selectionManager is RangeSelectionManager) {
                            Timber.i("들어왔다!!!")
                            val rangeSelectionManager =
                                calendarView.selectionManager as RangeSelectionManager
                            if (rangeSelectionManager.days != null) {
                                val startCalendar = rangeSelectionManager.days.first!!.calendar
                                val endCalendar = rangeSelectionManager.days.second!!.calendar

                                startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                                startCalendar.set(Calendar.MINUTE, 0)
                                startCalendar.set(Calendar.SECOND, 0)

                                endCalendar.add(Calendar.DAY_OF_MONTH, 1)
                                endCalendar.set(Calendar.HOUR_OF_DAY, 0)
                                endCalendar.set(Calendar.MINUTE, 0)
                                endCalendar.set(Calendar.SECOND, 0)

                                reportsFragment.onChangeDate(startCalendar.time, endCalendar.time)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Invalid Selection",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    })
                .setNegativeButton("취소",
                    DialogInterface.OnClickListener { dialog, id ->

                    })

            val alertDialog = builder.create()
            alertDialog.show() // show를 호출 안하면 positiveButton이 null이 됨.

            alertDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

