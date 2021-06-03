package com.example.stracker.login

import android.content.Intent
import android.content.SharedPreferences
import android.net.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.example.stracker.MainActivity
import com.example.stracker.R
import com.example.stracker.ResponseDTO
import com.example.stracker.STrackerApi
import com.example.stracker.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

const val EXTRA_USER = "com.example.stracker.USER"
const val EXTRA_EMAIL = "com.example.stracker.EMAIL"
const val REQ_EXIT = 1

class LoginActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_login
        )

        binding.loginButton.setOnClickListener {
            val email: String = binding.emailEdit.text.toString()
            val password: String = binding.passwordEdit.text.toString()

            if (email.isEmpty()) {
                // 아이디를 입력해주세요.
                    Snackbar.make(binding.root, getString(R.string.empty_email_message), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                // 비밀번호를 입력해주세요.
                Snackbar.make(binding.root, getString(R.string.empty_password_massage), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Timber.i("email: $email password: $password")
            val message = STrackerApi.retrofitService.login(email, password)
            message.enqueue(object : Callback<ResponseDTO> {
                override fun onResponse(call: Call<ResponseDTO>, response: Response<ResponseDTO>) {
                    if (response.isSuccessful) {
                        Timber.i("Response: ${response.body()?.toString()}")

                        val responseDTO: ResponseDTO? = response.body()
                        val result = responseDTO?.result

                        if (result.equals(email)) {
                            // 이메일 저장
                            val sharedPreferences: SharedPreferences =
                                getSharedPreferences(EXTRA_USER, MODE_PRIVATE)
                            val editor: SharedPreferences.Editor = sharedPreferences.edit()
                            editor.putString(EXTRA_EMAIL, email)
                            editor.commit()

                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Snackbar.make(binding.root, getString(R.string.email_password_mismatch_message), Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        Timber.i("Response: ${response.body()?.toString()}")
                    }
                }

                override fun onFailure(call: Call<ResponseDTO>, t: Throwable) {
                    Snackbar.make(binding.root, getString(R.string.server_connection_error_message), Snackbar.LENGTH_SHORT).show()
                    Timber.i("Failure: ${t.message}")
                }
            })
        }

        binding.signUpButton.setOnClickListener {
            startActivityForResult(Intent(this, SignUpActivity::class.java), REQ_EXIT)
        }

        binding.passwordFindButton.setOnClickListener {
            startActivityForResult(Intent(this, FindPasswordActivity::class.java), REQ_EXIT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_EXIT -> {
                if (resultCode == RESULT_OK) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}