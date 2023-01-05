package com.lemzeeyyy.sayfe.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkAccessibilityPermission()

        val navView: BottomNavigationView = binding.navViewBtm

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,R.id.nav_activities,R.id.nav_phonebook,R.id.nav_settings
            )
        )

        val fragmentContainerView =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = fragmentContainerView.navController

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            binding.navViewBtm.isVisible = appBarConfiguration.topLevelDestinations.contains(destination.id)
        }
        navView.setupWithNavController(navController)

    }
    private fun checkAccessibilityPermission(): Boolean {
        var accessEnabled = 0
        try {
            accessEnabled =
                Settings.Secure.getInt(this.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }
        return if (accessEnabled == 0) {
            /** if not construct intent to request permission  */
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            /** request permission via start activity for result  */

            startActivity(intent)
            false
        } else {
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        Log.d("AccessKeyDetector","Key pressed");
        return super.onKeyDown(keyCode, event)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
//        if(intent?.action == "ACTIVITIES")
//            navController.navigate(R.id.nav_activities)
    }
}