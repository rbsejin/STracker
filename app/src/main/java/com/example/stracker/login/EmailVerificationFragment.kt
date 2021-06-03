package com.example.stracker.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.stracker.R
import com.example.stracker.ResponseDTO
import com.example.stracker.STrackerApi
import com.example.stracker.databinding.FragmentEmailVerificationBinding
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class EmailVerificationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentEmailVerificationBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_email_verification, container, false)

        binding.okButton.setOnClickListener { view: View ->
            val email = binding.emailEdit.text.toString()
            if (email.isEmpty()) {
                Snackbar.make(binding.root, getString(R.string.empty_email_message), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val message = STrackerApi.retrofitService.emailConfirmAuthentication(email)
            message.enqueue(object : Callback<ResponseDTO> {
                override fun onResponse(call: Call<ResponseDTO>, response: Response<ResponseDTO>) {
                    if (response.isSuccessful) {
                        Timber.i("Response: ${response.body()?.toString()}")

                        val responseDTO: ResponseDTO? = response.body()
                        val result = responseDTO?.result ?: ""
                        Timber.i(result)

                        if (result.equals("이메일 전송 성공")) {
                            view.findNavController().navigate(
                                EmailVerificationFragmentDirections.actionEmailVerificationFragmentToAuthKeyVerificationFragment(
                                    email
                                )
                            )
                        } else {
                            Snackbar.make(binding.root, result, Snackbar.LENGTH_SHORT).show()
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
