package com.example.stracker

import java.text.SimpleDateFormat

fun secondToHourMinSecond(time: Long): String {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val hour = time / 3600
    val min = (time % 3600) / 60
    val second = time % 60
    return String.format("%d:%02d:%02d", hour, min, second)
}