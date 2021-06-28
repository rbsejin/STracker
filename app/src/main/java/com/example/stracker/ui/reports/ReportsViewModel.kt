package com.example.stracker.ui.reports

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.BarEntry
import java.text.SimpleDateFormat
import java.util.*

class ReportsViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }
    val text: LiveData<String> = _text

    val startAndEndDate = MutableLiveData<Pair<Date, Date>>()

    // 날짜(레이블)
//    val labels = MutableLiveData<MutableList<String>>(
//        mutableListOf(
//            "06/14(월)",
//            "06/15(화)",
//            "06/16(수)",
//            "06/17(목)",
//            "06/18(금)",
//            "06/19(토)",
//            "06/20(일)"
//        )
//    )

//    fun changeDate(startDate: Date, endDate: Date) {
//        startAndEndDate.value = Pair(startDate, endDate)
//
//        labels.value!!.clear()
//
//        val calendar = Calendar.getInstance()
//
//        var x = 0f
//
//        while (calendar.time < endDate) {
//            val simpleDateFormat = SimpleDateFormat("MM/dd")
//            labels.value!!.add(simpleDateFormat.toString())
//            calendar.add(Calendar.DAY_OF_YEAR, 1)
//
////            entryList.value!!.add(BarEntry(x, 1f))
//            x += 1f
//        }
//    }
}