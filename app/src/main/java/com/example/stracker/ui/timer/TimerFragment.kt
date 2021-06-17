package com.example.stracker.ui.timer

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.stracker.*
import com.example.stracker.databinding.FragmentTimerBinding
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

const val EXTRA_TIMER = "com.example.stracker.TIMER"

class TimerFragment : Fragment() {
    private lateinit var timerViewModel: TimerViewModel
    private lateinit var binding: FragmentTimerBinding
    private var timerTask: TimerTask? = null
    private val timer: Timer = Timer()
    private var startDateTime: Date? = null

    private lateinit var email: String

    private var taskDTOs: List<TaskDTO>? = null
    private var taskTimeDTOs: List<TaskTimeDTO>? = null


    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        timerViewModel =
            ViewModelProvider(this).get(TimerViewModel::class.java)

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_timer, container, false
        )

        binding.timerButton.setOnClickListener {
            // 타이머 상태 판단
            if (startDateTime != null) {
                // 타이머를 중단한다.
                binding.timerButton.setImageResource(R.drawable.ic_start)

                // 서버에 작업 중단 알림
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val startDateTimeString = simpleDateFormat.format(this.startDateTime!!)
                val endDateTimeString = simpleDateFormat.format(Calendar.getInstance().time)

                val message = STrackerApi.retrofitService.finishTask(
                    email,
                    binding.taskContentEdit.text.toString(),
                    startDateTimeString,
                    endDateTimeString
                )
                message.enqueue(object : Callback<ResponseDTO> {
                    override fun onResponse(call: Call<ResponseDTO>, response: Response<ResponseDTO>) {
                        if (response.isSuccessful) {
                            Timber.i("Response: ${response.body()?.toString()}")

                            val responseDTO: ResponseDTO? = response.body()
                            val result = responseDTO?.result

                            Timber.i("Success: $result")

                            taskDTOs = null
                            taskTimeDTOs = null

                            // 작업 리스트를 가져온다.
                            STrackerApi.retrofitService.getTasks(email)
                                .enqueue(object : Callback<List<TaskDTO>> {
                                    override fun onResponse(
                                        call: Call<List<TaskDTO>>,
                                        response: Response<List<TaskDTO>>
                                    ) {
                                        if (response.isSuccessful) {
                                            Timber.i("Response: ${response.body()?.toString()}")
                                            Timber.i(taskDTOs.toString())

                                            taskDTOs = response.body()
                                            if (taskTimeDTOs != null) {
                                                TaskManager.load(taskDTOs!!, taskTimeDTOs!!)

                                                // 리사이클러뷰
                                                val taskAdapter = binding.recyclerView.adapter as TaskAdapter
                                                taskAdapter.updateItems(TaskManager.tasks)
                                                taskAdapter.notifyDataSetChanged()
                                            }
                                        } else {
                                            Timber.i("Failure: ${response.body()?.toString()}")
                                        }
                                    }

                                    override fun onFailure(call: Call<List<TaskDTO>>, t: Throwable) {
                                        Timber.i("Failure: ${t.message}")
                                    }
                                })

                            STrackerApi.retrofitService.getTaskTimes(email)
                                .enqueue(object : Callback<List<TaskTimeDTO>> {
                                    override fun onResponse(
                                        call: Call<List<TaskTimeDTO>>,
                                        response: Response<List<TaskTimeDTO>>
                                    ) {
                                        if (response.isSuccessful) {
                                            Timber.i("Response: ${response.body()?.toString()}")
                                            Timber.i(taskTimeDTOs.toString())

                                            taskTimeDTOs = response.body()
                                            if (taskDTOs != null) {
                                                TaskManager.load(taskDTOs!!, taskTimeDTOs!!)

                                                // 리사이클러뷰
                                                val taskAdapter = binding.recyclerView.adapter as TaskAdapter
                                                taskAdapter.updateItems(TaskManager.tasks)
                                                taskAdapter.notifyDataSetChanged()
                                            }
                                        } else {
                                            Timber.i("Failure: ${response.body()?.toString()}")
                                        }
                                    }

                                    override fun onFailure(call: Call<List<TaskTimeDTO>>, t: Throwable) {
                                        Timber.i("Failure: ${t.message}")
                                    }
                                })
                        } else {
                            Timber.i("Failure: ${response.body()?.toString()}")
                        }
                    }

                    override fun onFailure(call: Call<ResponseDTO>, t: Throwable) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.server_connection_error_message),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        Timber.i("Failure: ${t.message}")
                    }
                })

                stopTimerTask()
            } else {
                // 타이머를 실행한다.
                binding.timerButton.setImageResource(R.drawable.ic_stop)
                startTimerTask()

                // 서버에 작업 시작 알림
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val startDateTimeString = simpleDateFormat.format(Calendar.getInstance().time)

                val message = STrackerApi.retrofitService.beginTask(
                    email,
                    binding.taskContentEdit.text.toString(),
                    startDateTimeString
                )
                message.enqueue(object : Callback<ResponseDTO> {
                    override fun onResponse(call: Call<ResponseDTO>, response: Response<ResponseDTO>) {
                        if (response.isSuccessful) {
                            Timber.i("Response: ${response.body()?.toString()}")

                            val responseDTO: ResponseDTO? = response.body()
                            val result = responseDTO?.result

                            Timber.i("Success: $result")
                        } else {
                            Timber.i("Failure: ${response.body()?.toString()}")
                        }
                    }

                    override fun onFailure(call: Call<ResponseDTO>, t: Throwable) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.server_connection_error_message),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        Timber.i("Failure: ${t.message}")
                    }
                })
            }

            saveTime()
        }

        email = (activity as MainActivity).email!!

        loadTime()

        if (startDateTime != null) {
            binding.timerButton.setImageResource(R.drawable.ic_stop)
            startTimerTask()
        }

        // 작업 리스트를 가져온다.
        STrackerApi.retrofitService.getTasks(email)
            .enqueue(object : Callback<List<TaskDTO>> {
                override fun onResponse(
                    call: Call<List<TaskDTO>>,
                    response: Response<List<TaskDTO>>
                ) {
                    if (response.isSuccessful) {
                        Timber.i("Response: ${response.body()?.toString()}")
                        Timber.i(taskDTOs.toString())

                        taskDTOs = response.body()
                        if (taskTimeDTOs != null) {
                            TaskManager.load(taskDTOs!!, taskTimeDTOs!!)
                            taskDTOs = null
                            taskTimeDTOs = null

                            // 리사이클러뷰
                            binding.recyclerView.adapter = TaskAdapter(TaskManager.tasks)
                        }
                    } else {
                        Timber.i("Failure: ${response.body()?.toString()}")
                    }
                }

                override fun onFailure(call: Call<List<TaskDTO>>, t: Throwable) {
                    Timber.i("Failure: ${t.message}")
                }
            })

        STrackerApi.retrofitService.getTaskTimes(email)
            .enqueue(object : Callback<List<TaskTimeDTO>> {
                override fun onResponse(
                    call: Call<List<TaskTimeDTO>>,
                    response: Response<List<TaskTimeDTO>>
                ) {
                    if (response.isSuccessful) {
                        Timber.i("Response: ${response.body()?.toString()}")
                        Timber.i(taskTimeDTOs.toString())

                        taskTimeDTOs = response.body()
                        if (taskDTOs != null) {
                            TaskManager.load(taskDTOs!!, taskTimeDTOs!!)
                            taskDTOs = null
                            taskTimeDTOs = null

                            // 리사이클러뷰
                            binding.recyclerView.adapter = TaskAdapter(TaskManager.tasks)
                        }
                    } else {
                        Timber.i("Failure: ${response.body()?.toString()}")
                    }
                }

                override fun onFailure(call: Call<List<TaskTimeDTO>>, t: Throwable) {
                    Timber.i("Failure: ${t.message}")
                }
            })

        return binding.root
    }

    private fun startTimerTask() {
        if (startDateTime == null) {
            startDateTime = Calendar.getInstance().time

            saveTime()
        }

        timerTask = object : TimerTask() {
            override fun run() {
                val currentTime = Calendar.getInstance().time
                val time = (currentTime.time - startDateTime!!.time) / 1000

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

        startDateTime = null

        // 작업을 저장한다. (작업명, 걸린 시간)
    }

    private fun loadTime() {
        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences(EXTRA_TIMER, AppCompatActivity.MODE_PRIVATE)

        val millis = sharedPreferences.getLong(EXTRA_TIMER, 0L)
        if (millis != 0L) {
            startDateTime = Date(millis)
        }
    }

    private fun saveTime() {
        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences(EXTRA_TIMER, AppCompatActivity.MODE_PRIVATE)

        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putLong(EXTRA_TIMER, startDateTime?.time ?: 0L)
        editor.apply()
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }
}