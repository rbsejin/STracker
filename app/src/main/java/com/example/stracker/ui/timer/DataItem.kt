package com.example.stracker.ui.timer

import java.util.*

sealed class DataItem {
    data class ChildItem(val child: Child) : DataItem()

    class HeaderItem(val header: Header) : DataItem() {
        val childList = mutableListOf<ChildItem>()
    }

    data class DateHeaderItem(val dateHeader: DateHeader) : DataItem()
}

data class Header(
    var isOpen: Boolean,
    val content: String,
    val projectName: String,
    var time: String,
    var childCount: Int,
    val date: String,
    var second: Long = 0
)

data class Child(val content: String, val projectName: String, val time: String)
data class DateHeader(val date: Date, var time: String)