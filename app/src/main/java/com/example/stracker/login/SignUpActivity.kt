package com.example.stracker.login

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.*
import android.widget.*
import androidx.databinding.DataBindingUtil
import com.example.stracker.MainActivity
import com.example.stracker.R
import com.example.stracker.ResponseDTO
import com.example.stracker.STrackerApi
import com.example.stracker.databinding.ActivitySignUpBinding
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class SignUpActivity : AppCompatActivity() {
    private var isAuthenticated: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivitySignUpBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        val checkBox1: CheckBox = findViewById(R.id.terms_of_service_check)
        val checkBox2: CheckBox = findViewById(R.id.privacy_policy_check)

        binding.signUpButton.setOnClickListener {
            val email: String = binding.emailEdit.text.toString()
            val password: String = binding.passwordEdit.text.toString()
            val passwordConfirm: String = binding.passwordConfirmEdit.text.toString()

            if (!isAuthenticated) {
                Snackbar.make(binding.root, getString(R.string.email_auth_confirm_message), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener;
            }

            if (password.isEmpty()) {
                Snackbar.make(binding.root, getString(R.string.password_empty_message), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener;
            }

            if (password.length < 10) {
                Snackbar.make(binding.root, getString(R.string.password_length_message), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener;
            }

            if (!password.equals(passwordConfirm)) {
                Snackbar.make(binding.root, getString(R.string.password_mismatch_message), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener;
            }

            if (!checkBox1.isChecked) {
                Snackbar.make(binding.root, getString(R.string.terms_of_service_agree_message), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener;
            }

            if (!checkBox2.isChecked) {
                Snackbar.make(binding.root, getString(R.string.pravicy_policy_agree_message), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener;
            }

            Timber.i("email: $email password: $password")
            val message = STrackerApi.retrofitService.signUp(email, password)
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

                            val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                            setResult(RESULT_OK, intent)
                            finish()
                        } else {
                            binding.passwordConfirmAlert.text = result
                        }
                    } else {
                        Timber.i("Failure: ${response.body()?.toString()}")
                    }
                }

                override fun onFailure(call: Call<ResponseDTO>, t: Throwable) {
                    binding.passwordConfirmAlert.text = "서버접속 실패"
                    Toast.makeText(this@SignUpActivity, "서버접속실패", Toast.LENGTH_SHORT).show()
                    Timber.i("Failure: ${t.message}")
                }
            })
        }

        binding.emailEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.emailConfirmButton.isEnabled =
                    binding.emailEdit.text.toString().isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        binding.passwordEdit.setOnFocusChangeListener { v, hasFocus ->
            if (binding.passwordEdit.text.length >= 10) {
                binding.passwordAlertText.setTextColor(Color.GREEN)
            } else {
                binding.passwordAlertText.setTextColor(Color.RED)
            }
        }

        binding.passwordEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.passwordEdit.text.length >= 10) {
                    binding.passwordAlertText.setTextColor(Color.GREEN)
                } else {
                    binding.passwordAlertText.setTextColor(Color.RED)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.emailConfirmButton.setOnClickListener { view: View ->
            if (binding.emailConfirmButton.text.equals(getString(R.string.another_email))) {
                binding.emailEdit.setText("")
                binding.emailEdit.isEnabled = true
                binding.emailConfirmButton.text = getString(R.string.email_confirm)
                binding.authKeyEdit.visibility = GONE
                binding.authKeyConfirmButton.visibility = GONE
                binding.authKeyEdit.setText("")
                isAuthenticated = false
                return@setOnClickListener
            }

            val email = binding.emailEdit.text.toString()
            if (!email.contains("@") || !email.contains(".")) {
                Snackbar.make(
                    binding.root,
                    "이메일 형식이 올바르지 않습니다. 확인 후 다시 시도해주세요",
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener;
            }

            val message = STrackerApi.retrofitService.emailConfirm(email)
            message.enqueue(object : Callback<ResponseDTO> {
                override fun onResponse(call: Call<ResponseDTO>, response: Response<ResponseDTO>) {
                    if (response.isSuccessful) {
                        Timber.i("Response: ${response.body()?.toString()}")

                        val responseDTO: ResponseDTO? = response.body()
                        val result = responseDTO?.result
                        Timber.i(result)

                        if (result.equals("이미 회원가입된 이메일입니다. 입력한 이메일을 확인해주세요.")) {
                            Snackbar.make(
                                binding.root,
                                "이미 회원가입된 이메일입니다. 입력한 이메일을 확인해주세요.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            return
                        }

                        if (result.equals("인증번호 발송 성공")) {
                            Snackbar.make(binding.root, getString(R.string.auth_key_send_message), Snackbar.LENGTH_SHORT)
                                .show()

                            binding.authKeyEdit.visibility = VISIBLE
                            binding.authKeyEdit.isEnabled = true

                            binding.authKeyConfirmButton.visibility = VISIBLE
                            binding.authKeyConfirmButton.isEnabled = true
                        } else {
                            Timber.i("인증번호 발송 실패")
                        }
                    } else {
                        Timber.i("Failure: ${response.body()?.toString()}")
                    }
                }

                override fun onFailure(call: Call<ResponseDTO>, t: Throwable) {
                    Toast.makeText(this@SignUpActivity, "서버접속실패", Toast.LENGTH_SHORT).show()
                    Timber.i("Failure: ${t.message}")
                }
            })
        }

        binding.authKeyConfirmButton.setOnClickListener { view: View ->
            val email = binding.emailEdit.text.toString()
            val key = binding.authKeyEdit.text.toString()
            Timber.i("email: $email key: $key")

            val message = STrackerApi.retrofitService.codeConfirm(email, key)
            message.enqueue(object : Callback<ResponseDTO> {
                override fun onResponse(call: Call<ResponseDTO>, response: Response<ResponseDTO>) {
                    if (response.isSuccessful) {
                        Timber.i("Response: ${response.body()?.toString()}")

                        val responseDTO: ResponseDTO? = response.body()
                        val result = responseDTO?.result

                        if (result.equals("인증성공")) {
                            binding.emailEdit.isEnabled = false
                            binding.authKeyEdit.isEnabled = false
                            binding.authKeyConfirmButton.isEnabled = false
                            binding.emailConfirmButton.text = getString(R.string.another_email)
                            isAuthenticated = true
                            Snackbar.make(binding.root, getString(R.string.auth_complete_message), Snackbar.LENGTH_SHORT)
                                .show()
                        } else {
                            Snackbar.make(binding.root, getString(R.string.auth_key_mismatch_message), Snackbar.LENGTH_SHORT)
                                .show()
                            Timber.i(result)
                        }
                    } else {
                        Timber.i("Failure: ${response.body()?.toString()}")
                    }
                }

                override fun onFailure(call: Call<ResponseDTO>, t: Throwable) {
                    Toast.makeText(this@SignUpActivity, "서버접속실패", Toast.LENGTH_SHORT).show()
                    Timber.i("Failure: ${t.message}")
                }
            })
        }

        binding.termsOfServiceButton.setOnClickListener {
            val dialog = TermsOfServiceDialogFragment()
            dialog.show(supportFragmentManager, "TermsOfServiceDialogFragment")
        }

        binding.privacyPolicyButton.setOnClickListener {
            val dialog = PravityPolicyDialogFragment()
            dialog.show(supportFragmentManager, "PrivacyPolicyDialogFragment")
        }
    }
}