package com.example.licenta

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.licenta.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ActivityMain : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //configurare navigare pentru secțiunea principală din cadrul aplicației
        val navView: BottomNavigationView = binding.navMeniu

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_pag2, R.id.nav_pag3
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        supportActionBar?.hide()    //ascundere ActionBar
    }

    //suprascriere a metodei ce este apelată în momentul în care utilizatorul apasă pe butonul "back" din bara de navigație
    override fun onBackPressed() {
        exitAplicatie()
    }

    //funcție ce afișează un AlertDialog, în care utilizatorul este întrebat dacă dorește să închidă aplicația
    private fun exitAplicatie() {
        MaterialAlertDialogBuilder(this)
            .setMessage("Vrei să închizi aplicația?")
            //finishAffinity închide activitățile începute anterior (în acest caz ActivityStart și ActivityMain)
            .setPositiveButton("Da") { _, _ -> finishAffinity() }
            .setNegativeButton("Nu", null)
            .show()
    }
}