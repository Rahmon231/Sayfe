package com.lemzeeyyy.sayfe.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.database.SharedPrefs
import com.lemzeeyyy.sayfe.databinding.ActivityMainBinding
import com.lemzeeyyy.sayfe.fragments.DashboardFragment
import com.lemzeeyyy.sayfe.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val DOUBLE_CLICK_TIME_DELTA = 300
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

    private lateinit var navController: NavController

    private var lastClickTime: Long = 0

    private val viewModel: MainActivityViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navViewBtm

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home,R.id.nav_activities,R.id.nav_phonebook,R.id.nav_settings)
        )

        val fragmentContainerView =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = fragmentContainerView.navController

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            binding.navViewBtm.isVisible = appBarConfiguration.topLevelDestinations.contains(destination.id)
        }
        navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val clickTime = System.currentTimeMillis()
        if (keyCode.equals(KeyEvent.KEYCODE_VOLUME_UP)){
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                CoroutineScope(Dispatchers.Main).launch {
                   viewModel.updateShouldTriggerApp(true)
                }
            }
        }
        lastClickTime = clickTime

        return super.onKeyUp(keyCode, event)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if(intent?.action == "ACTIVITIES")
            navController.navigate(R.id.nav_activities)
    }
}