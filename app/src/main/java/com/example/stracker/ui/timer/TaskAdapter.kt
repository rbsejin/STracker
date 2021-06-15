package com.example.stracker.ui.timer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.stracker.Task
import com.example.stracker.TaskTime
import com.example.stracker.databinding.DateHeaderItemBinding
import com.example.stracker.databinding.TaskItemBinding
import com.example.stracker.databinding.TaskTimeItemBinding
import com.example.stracker.secondToHourMinSecond
import java.text.SimpleDateFormat
import java.util.*

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1
private const val ITEM_VIEW_TYPE_DATE = 2

class TaskAdapter(var tasks: MutableList<Task>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var itemList = mutableListOf<DataItem>()

    init {
        var dateHeaderItem: DataItem.DateHeaderItem? = null

        // 1. taskTimeList
        val taskTimeList = mutableListOf<TaskTime>()

        for (task in tasks) {
            taskTimeList.addAll(task.getTaskTimes())
        }

        taskTimeList.sortByDescending {
            it.startDateTime
        }

        var task: Task? = null
        var dateTime = ""
        var headerItem: DataItem.HeaderItem? = null

        for (taskTime in taskTimeList) {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd E", Locale.KOREAN)

            if (task != taskTime.task || (simpleDateFormat.format(taskTime.startDateTime) != dateTime)) {
                if (simpleDateFormat.format(taskTime.startDateTime) != dateTime) {
                    dateHeaderItem = DataItem.DateHeaderItem(DateHeader(taskTime.startDateTime, ""))
                    itemList.add(dateHeaderItem)
                }

                task = taskTime.task
                dateTime = simpleDateFormat.format(taskTime.startDateTime)

                var isFind = false

                for (item in itemList) {
                    when (item) {
                        is DataItem.HeaderItem -> {
                            if (item.header.content == taskTime.getContent() && item.header.date == dateTime) {
                                headerItem = item
                                isFind = true
                                break
                            }
                        }
                        is DataItem.ChildItem -> {

                        }
                        is DataItem.DateHeaderItem -> {

                        }
                    }
                }

                if (!isFind) {
                    headerItem =
                        DataItem.HeaderItem(
                            Header(
                                false,
                                taskTime.getContent(),
                                "",
                                "",
                                0,
                                dateTime
                            )
                        )
                    itemList.add(headerItem)
                }

            }

            headerItem?.childList?.add(
                DataItem.ChildItem(
                    Child(
                        taskTime.getContent(),
                        "",
                        taskTime.getTimeString()
                    )
                )
            )

            if (headerItem != null) {
                headerItem.header.childCount++
                headerItem.header.second += taskTime.getTime()
            }
        }
    }

    class HeaderViewHolder private constructor(val binding: TaskItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Header) {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

            binding.timeText.text = secondToHourMinSecond(item.second)
            binding.childCountText.text = item.childCount.toString()
            binding.taskContentText.text = if (item.content.isNotEmpty()) item.content else "제목 없음"
            binding.taskProjectNameText.text = item.projectName

//            binding.header = item
//            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TaskItemBinding.inflate(layoutInflater, parent, false)
                return HeaderViewHolder(binding)
            }
        }
    }

    class ViewHolder private constructor(private val binding: TaskTimeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Child) {
            binding.timeText.text = item.time
            binding.taskContentText.text = if (item.content.isNotEmpty()) item.content else "제목 없음"

            binding.taskProjectNameText.text = item.projectName
//            binding.task = item
//            binding.clickListener = clickListener
//            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TaskTimeItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    class DateHeaderViewHolder private constructor(private val binding: DateHeaderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DateHeader) {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd E", Locale.KOREAN)
            binding.dateText.text = simpleDateFormat.format(item.date)
            binding.timeText.text = item.time

//            binding.header = item
//            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): DateHeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DateHeaderItemBinding.inflate(layoutInflater, parent, false)
                return DateHeaderViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            ITEM_VIEW_TYPE_DATE -> DateHeaderViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val headerItem = itemList[position] as DataItem.HeaderItem
                holder.bind(headerItem.header)

                holder.binding.childCountText.setOnClickListener { view: View ->
                    if (headerItem.childList.size > 0) {
                        // open
                        itemList.addAll(position + 1, headerItem.childList)
                        headerItem.childList.clear()

                        headerItem.header.isOpen = true
                    } else {
                        // close
                        for (i in position + 1 until itemList.size) {
                            val item = itemList[i]
                            if (item is DataItem.ChildItem) {
                                headerItem.childList.add(item)
                            } else {
                                break
                            }
                        }

                        itemList.removeAll(headerItem.childList)

                        headerItem.header.isOpen = false
                    }

                    notifyDataSetChanged()
                }
            }
            is ViewHolder -> {
                val childItem: DataItem.ChildItem = itemList[position] as DataItem.ChildItem
                holder.bind(childItem.child)
            }
            is DateHeaderViewHolder -> {
                val dateHeaderItem = itemList[position] as DataItem.DateHeaderItem
                holder.bind(dateHeaderItem.dateHeader)
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position]) {
            is DataItem.HeaderItem -> ITEM_VIEW_TYPE_HEADER
            is DataItem.ChildItem -> ITEM_VIEW_TYPE_ITEM
            is DataItem.DateHeaderItem -> ITEM_VIEW_TYPE_DATE
        }
    }

    fun updateItems(tasks: MutableList<Task>) {
        this.tasks = tasks
        itemList.clear()

        var dateHeaderItem: DataItem.DateHeaderItem? = null

        // 1. taskTimeList
        val taskTimeList = mutableListOf<TaskTime>()

        for (task in tasks) {
            taskTimeList.addAll(task.getTaskTimes())
        }

        taskTimeList.sortByDescending {
            it.startDateTime
        }

        var task: Task? = null
        var dateTime = ""
        var headerItem: DataItem.HeaderItem? = null

        for (taskTime in taskTimeList) {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd E", Locale.KOREAN)

            if (task != taskTime.task || (simpleDateFormat.format(taskTime.startDateTime) != dateTime)) {
                if (simpleDateFormat.format(taskTime.startDateTime) != dateTime) {
                    dateHeaderItem = DataItem.DateHeaderItem(DateHeader(taskTime.startDateTime, ""))
                    itemList.add(dateHeaderItem)
                }

                task = taskTime.task
                dateTime = simpleDateFormat.format(taskTime.startDateTime)

                var isFind = false

                for (item in itemList) {
                    when (item) {
                        is DataItem.HeaderItem -> {
                            if (item.header.content == taskTime.getContent() && item.header.date == dateTime) {
                                headerItem = item
                                isFind = true
                                break
                            }
                        }
                        is DataItem.ChildItem -> {

                        }
                        is DataItem.DateHeaderItem -> {

                        }
                    }
                }

                if (!isFind) {
                    headerItem =
                        DataItem.HeaderItem(
                            Header(
                                false,
                                taskTime.getContent(),
                                "",
                                "",
                                0,
                                dateTime
                            )
                        )
                    itemList.add(headerItem)
                }
            }

            headerItem?.childList?.add(
                DataItem.ChildItem(
                    Child(
                        taskTime.getContent(),
                        "",
                        taskTime.getTimeString()
                    )
                )
            )

            if (headerItem != null) {
                headerItem.header.childCount++
                headerItem.header.second += taskTime.getTime()
            }
        }
    }
}
