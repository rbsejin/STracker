package com.example.stracker.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.stracker.R
import com.example.stracker.ResponseDTO
import com.example.stracker.STrackerApi
import com.example.stracker.databinding.FragmentPasswordResetBinding
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PasswordResetFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPasswordResetBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_password_reset, container, false)

        binding.okButton.setOnClickListener {
            val args = EmailVerificationFragmentArgs.fromBundle(requireArguments())
            val email: String = args.email

            val password = binding.passwordEdit.text.toString()
            if (password.length <= 10) {
                Snackbar.make(binding.root, getString(R.string.password_length_message), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Timber.i("email: $email password: $password")
            val message = STrackerApi.retrofitService.changePassword(email, password)
            message.enqueue(object : Callback<ResponseDTO> {
                override fun onResponse(call: Call<ResponseDTO>, response: Response<ResponseDTO>) {
                    if (response.isSuccessful) {
                        Timber.i("Response: ${response.body()?.toString()}")

                        val responseDTO: ResponseDTO? = response.body()
                        val result = responseDTO?.result

                        if (result.equals("성공")) {
                            // 이메일 저장
                            val sharedPreferences: SharedPreferences? =
                                context?.getSharedPreferences(EXTRA_USER, AppCompatActivity.MODE_PRIVATE)
                            val editor: SharedPreferences.Editor? = sharedPreferences?.edit()
                            editor?.putString(EXTRA_EMAIL, email)
                            editor?.commit()

                            val intent = Intent(context, LoginActivity::class.java)
                            activity?.setResult(AppCompatActivity.RESULT_OK, intent)
                            activity?.finish()
                        } else {
                            Timber.i("Response: 실패")
                        }
                    } else {
                        Timber.i("Failure: ${response.body()?.toString()}")
                    }
                }

                override fun onFailure(call: Call<ResponseDTO>, t: Throwable) {
                    Snackbar.make(binding.root, getString(R.string.server_connection_error_message), Snackbar.LENGTH_SHORT).show()
                    Timber.i("Failure: ${t.message}")
                }
            })
        }

        return binding.root
    }
}