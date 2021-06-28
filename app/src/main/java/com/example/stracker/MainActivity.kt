package com.example.stracker

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ReportFragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.stracker.databinding.ActivityMainBinding
import com.example.stracker.login.EXTRA_EMAIL
import com.example.stracker.login.EXTRA_USER
import com.example.stracker.login.LoginActivity
import com.example.stracker.login.REQ_EXIT
import com.example.stracker.ui.reports.CalendarDialogFragment
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val sharedPreferences: SharedPreferences = getSharedPreferences(EXTRA_USER, MODE_PRIVATE)
        email = sharedPreferences.getString(EXTRA_EMAIL, "")
        Timber.i("email: $email")

        if (email.isNullOrEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_timer, R.id.navigation_calendar, R.id.navigation_reports
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.setting -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivityForResult(intent, REQ_EXIT)
            }
        }

        return super.onOptionsItemSelected(item)
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