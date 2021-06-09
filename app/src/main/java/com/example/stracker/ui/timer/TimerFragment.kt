package com.example.stracker.ui.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.stracker.R
import com.example.stracker.databinding.FragmentAuthKeyVerificationBinding
import com.example.stracker.databinding.FragmentTimerBinding

class TimerFragment : Fragment() {

    private lateinit var timerViewModel: TimerViewModel

    private var isWorking = false

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        timerViewModel =
                ViewModelProvider(this).get(TimerViewModel::class.java)

        val binding: FragmentTimerBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_timer, container, false)

        binding.timerButton.setOnClickListener {
            // 타이머 상태 판단
            if (isWorking) {
                // 타이머를 중단한다.
                binding.timerButton.setImageResource(R.drawable.ic_start)
            } else {
                // 타이머를 실행한다.
                binding.timerButton.setImageResource(R.drawable.ic_stop)


            }

            isWorking = !isWorking
        }

        return binding.root
    }
}