package com.example.stracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import timber.log.Timber

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        setResult(RESULT_OK, intent)
        finish()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val emailPreference: Preference = findPreference("email") ?: return

            val sharedPreferences: SharedPreferences? =
                context?.getSharedPreferences(EXTRA_USER, Context.MODE_PRIVATE)
            val email = sharedPreferences?.getString(EXTRA_EMAIL, "")
            Timber.i("email: $email")

            emailPreference.summary = email
        }

        override fun onPreferenceTreeClick(preference: Preference?): Boolean {
            val settingsActivity = (activity as SettingsActivity)

            when (preference?.key) {
                "logout" -> {
                    val context: Context? = activity
                    val sharedPreferences: SharedPreferences? =
                        context?.getSharedPreferences(EXTRA_USER, Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor? = sharedPreferences?.edit()
                    editor?.remove(EXTRA_EMAIL)
                    editor?.commit()
                    settingsActivity.logout()
                }
            }

            return super.onPreferenceTreeClick(preference)
        }
    }
}