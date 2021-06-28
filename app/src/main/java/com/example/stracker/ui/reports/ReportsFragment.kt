package com.example.stracker.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.stracker.R
import com.example.stracker.TaskManager
import com.example.stracker.TaskTime
import com.example.stracker.databinding.FragmentReportsBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class ReportsFragment : Fragment() {

    private lateinit var reportsViewModel: ReportsViewModel
    private lateinit var binding: FragmentReportsBinding
    val labels: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        reportsViewModel =
            ViewModelProvider(this).get(ReportsViewModel::class.java)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reports, container, false)

        var calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -6)
        val startDate = calendar.time

        calendar.add(Calendar.DAY_OF_MONTH, 7)
        val endDate = calendar.time

        //
        // taskTime을 기간별로 그룹핑한다.
        var taskTimes = mutableListOf<TaskTime>()

        for (task in TaskManager.tasks) {
            taskTimes.addAll(task.getTaskTimes())
        }

        taskTimes = taskTimes.filter { taskTime ->
            taskTime.startDateTime >= startDate
                    && (taskTime.endDateTime?.compareTo(endDate) ?: 1) < 0
        } as MutableList

        taskTimes.sortBy { taskTime ->
            taskTime.startDateTime
        }

        for (taskTime in taskTimes) {

            Timber.i(taskTime.startDateTime.toString())
        }


        // BarEntry를 담는 리스트
        val entryList = mutableListOf<BarEntry>()

        calendar = Calendar.getInstance()
        calendar.time = startDate

        var x = 0f
        while (calendar.time < endDate) {
            val simpleDateFormat = SimpleDateFormat("MM/dd")
            labels.add(simpleDateFormat.format(calendar.time))

            var time = 0f
            for (taskTime in taskTimes) {
                val format = SimpleDateFormat("YYMMdd")
                val s = simpleDateFormat.format(taskTime.startDateTime)
                val c = simpleDateFormat.format(calendar.time)
                if (s.equals(c)) {
                    time += taskTime.getTime().toFloat()
                }
            }

            time /= 3600

            entryList.add(BarEntry(x, time))
            x += 1f

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        binding.barChart.xAxis.labelCount = labels.size

        //위에서 만든 BarEntry 리스트를 인자로 준다
        val barDataSet = BarDataSet(entryList, "업무시간")
        barDataSet.color = ColorTemplate.rgb("#ff7b22")
//        barDataSet.axisDependency = YAxis.AxisDependency.LEFT


        // , 구분으로 여러 BarDataSet을 줄 수 있습니다.
        val barData = BarData(barDataSet)
        //BarData에 추가된 모든 BarDataSet에 일괄 적용되는 값입니다.
        barData.barWidth = 0.75f

        binding.barChart.data = barData

        binding.barChart.apply {
            //터치, Pinch 상호작용
            setScaleEnabled(false)
            setPinchZoom(false)

            //Chart가 그려질때 애니메이션
            animateXY(0, 800)

            //Chart 밑에 description 표시 유무
            description = null

            //Legend는 차트의 범례를 의미합니다
            //범례가 표시될 위치를 설정
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT


            //차트의 좌, 우측 최소/최대값을 설정합니다.
            //차트 제일 밑이 0부터 시작하고 싶은 경우 설정합니다.
            axisLeft.axisMinimum = 0f
            axisRight.axisMinimum = 0f

            //기본적으로 차트 우측 축에도 데이터가 표시됩니다
            //이를 활성화/비활성화 하기 위함
            axisRight.setDrawLabels(false)

            //xAxis, yAxis 둘다 존재하여 따로 설정이 가능합니다
            //차트 내부에 Grid 표시 유무
            xAxis.setDrawGridLines(false)

            //x축 데이터 표시 위치
            xAxis.position = XAxis.XAxisPosition.BOTTOM

            //x축 데이터 갯수 설정
            xAxis.labelCount = entryList.size

            // x축에 String 설정
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value.toInt() < labels.size && labels.size <= 10) labels[value.toInt()] else ""
                }
            }

            invalidate()
        }

        binding.calendarButton.setOnClickListener {
            CalendarDialogFragment(this).also { dialog ->
                dialog.isCancelable = false
                dialog.show(childFragmentManager, "날짜선택")
            }
        }

        return binding.root
    }

    fun onChangeDate(startDate: Date, endDate: Date) {
//        binding.barChart.data.clearValues()
//        binding.barChart.data.notifyDataChanged()

        // taskTime을 기간별로 그룹핑한다.
        var taskTimes = mutableListOf<TaskTime>()

        for (task in TaskManager.tasks) {
            taskTimes.addAll(task.getTaskTimes())
        }

        taskTimes = taskTimes.filter { taskTime ->
            taskTime.startDateTime >= startDate
                    && (taskTime.endDateTime?.compareTo(endDate) ?: 1) < 0
        } as MutableList

        taskTimes.sortBy { taskTime ->
            taskTime.startDateTime
        }

        for (taskTime in taskTimes) {

            Timber.i(taskTime.startDateTime.toString())
        }


        // BarEntry를 담는 리스트
        val entryList = mutableListOf<BarEntry>()
        labels.clear()

        val calendar = Calendar.getInstance()
        calendar.time = startDate

        var x = 0f
        while (calendar.time < endDate) {
            val simpleDateFormat = SimpleDateFormat("MM/dd")
            labels.add(simpleDateFormat.format(calendar.time))

            var time = 0f
            for (taskTime in taskTimes) {
                val format = SimpleDateFormat("YYMMdd")
                val s = simpleDateFormat.format(taskTime.startDateTime)
                val c = simpleDateFormat.format(calendar.time)
                if (s.equals(c)) {
                    time += taskTime.getTime().toFloat()
                }
            }

            time /= 3600

            entryList.add(BarEntry(x, time))
            x += 1f

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }


        binding.barChart.xAxis.labelCount = labels.size

        //위에서 만든 BarEntry 리스트를 인자로 준다
        val barDataSet = BarDataSet(entryList, "업무시간")
        barDataSet.color = ColorTemplate.rgb("#ff7b22")
//        barDataSet.axisDependency = YAxis.AxisDependency.LEFT


        // , 구분으로 여러 BarDataSet을 줄 수 있습니다.
        val barData = BarData(barDataSet)
        //BarData에 추가된 모든 BarDataSet에 일괄 적용되는 값입니다.
        barData.barWidth = 0.75f

        binding.barChart.data = barData

        binding.barChart.apply {
            //터치, Pinch 상호작용
            setScaleEnabled(false)
            setPinchZoom(false)

            //Chart가 그려질때 애니메이션
            animateXY(0, 800)

            //Chart 밑에 description 표시 유무
            description = null

            //Legend는 차트의 범례를 의미합니다
            //범례가 표시될 위치를 설정
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT


            //차트의 좌, 우측 최소/최대값을 설정합니다.
            //차트 제일 밑이 0부터 시작하고 싶은 경우 설정합니다.
            axisLeft.axisMinimum = 0f
            axisRight.axisMinimum = 0f

            //기본적으로 차트 우측 축에도 데이터가 표시됩니다
            //이를 활성화/비활성화 하기 위함
            axisRight.setDrawLabels(false)

            //xAxis, yAxis 둘다 존재하여 따로 설정이 가능합니다
            //차트 내부에 Grid 표시 유무
            xAxis.setDrawGridLines(false)

            //x축 데이터 표시 위치
            xAxis.position = XAxis.XAxisPosition.BOTTOM

            //x축 데이터 갯수 설정
            xAxis.labelCount = entryList.size

            invalidate()
        }
    }
}