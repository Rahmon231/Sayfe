package com.lemzeeyyy.sayfe.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.navigation.NavController
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

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}