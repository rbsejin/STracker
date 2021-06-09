package com.example.stracker.ui.timer

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.stracker.R
import com.example.stracker.databinding.FragmentTimerBinding
import java.util.*

const val EXTRA_TIMER = "com.example.stracker.TIMER"

class TimerFragment : Fragment() {

    private lateinit var timerViewModel: TimerViewModel
    private lateinit var binding: FragmentTimerBinding
    private var timerTask: TimerTask? = null
    private val timer: Timer = Timer()
    private var startTime: Long = 0L

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        timerViewModel =
                ViewModelProvider(this).get(TimerViewModel::class.java)

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_timer, container, false)

        binding.timerButton.setOnClickListener {
            // 타이머 상태 판단
            if (startTime != 0L) {
                // 타이머를 중단한다.
                binding.timerButton.setImageResource(R.drawable.ic_start)
                stopTimerTask()
            } else {
                // 타이머를 실행한다.
                binding.timerButton.setImageResource(R.drawable.ic_stop)
                startTimerTask()
            }

            saveTime()
        }

        loadTime()

        if (startTime != 0L) {
            binding.timerButton.setImageResource(R.drawable.ic_stop)
            startTimerTask()
        }

        return binding.root
    }

    private fun startTimerTask() {
        if (startTime == 0L) {
            startTime = System.currentTimeMillis()

            saveTime()
        }

        timerTask = object : TimerTask() {
             override fun run() {
                val currentTime = System.currentTimeMillis()
                val time = (currentTime - startTime) / 1000

                binding.timeText.post {
                    val hour = time / 3600
                    val min = (time % 3600) / 60
                    val second = time % 60
                    binding.timeText.text = String.format("%d:%02d:%02d", hour, min, second)
                }
            }
        }

        timer.schedule(timerTask, 0, 1000)
    }

    private fun stopTimerTask() {
        timerTask?.let {
            binding.timeText.text = getString(R.string.default_time)
            timerTask!!.cancel()
            timerTask = null
        }

        startTime = 0L

        // 작업을 저장한다. (작업명, 걸린 시간)

    }

    private fun loadTime() {
        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences(EXTRA_TIMER, AppCompatActivity.MODE_PRIVATE)

        startTime = sharedPreferences.getLong(EXTRA_TIMER, 0L)
    }

    private fun saveTime() {
        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences(EXTRA_TIMER, AppCompatActivity.MODE_PRIVATE)

        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putLong(EXTRA_TIMER, startTime)
        editor.apply()
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }
}