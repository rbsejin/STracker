package com.example.stracker.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.stracker.*
import com.example.stracker.databinding.FragmentAuthKeyVerificationBinding
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class AuthKeyVerificationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding: FragmentAuthKeyVerificationBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_auth_key_verification, container, false)

        val args = EmailVerificationFragmentArgs.fromBundle(requireArguments())
        val email: String = args.email

        binding.okButton.setOnClickListener {
            val key = binding.keyEdit.text.toString()
            if (key.isEmpty()) {
                Snackbar.make(binding.root, getString(R.string.empty_auth_key_message), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Timber.i("email: $email key: $key")

            val message = STrackerApi.retrofitService.codeConfirm(email, key)
            message.enqueue(object : Callback<ResponseDTO> {
                override fun onResponse(call: Call<ResponseDTO>, response: Response<ResponseDTO>) {
                    if (response.isSuccessful) {
                        Timber.i("Response: ${response.body()?.toString()}")

                        val responseDTO: ResponseDTO? = response.body()
                        val result = responseDTO?.result

                        if (result.equals("인증성공")) {
                            findNavController().navigate(
                                AuthKeyVerificationFragmentDirections.actionAuthKeyVerificationFragmentToPasswordResetFragment(
                                    email
                                )
                            )
                        } else {
                            Snackbar.make(binding.root, getString(R.string.auth_key_mismatch_message), Snackbar.LENGTH_SHORT).show()
                            Timber.i(result)
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

        binding.resendButton.setOnClickListener {
            val message = STrackerApi.retrofitService.emailConfirmAuthentication(email, true)
            message.enqueue(object : Callback<ResponseDTO> {
                override fun onResponse(call: Call<ResponseDTO>, response: Response<ResponseDTO>) {
                    if (response.isSuccessful) {
                        val responseDTO: ResponseDTO? = response.body()
                        val result = responseDTO?.result
                        Timber.i(result)

                        if (result.equals("이메일 전송 성공")) {
                            Snackbar.make(binding.root, getString(R.string.auth_key_resend_message), Snackbar.LENGTH_SHORT).show()
                            Timber.i("Response: ${response.body()?.toString()}")
                        } else {
                            Timber.i("Failure: ${response.body()?.toString()}")
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