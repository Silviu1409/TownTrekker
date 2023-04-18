package com.example.towntrekker

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.towntrekker.databinding.ActivityMainBinding
import com.example.towntrekker.datatypes.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ActivityMain : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //configurare navigare pentru secțiunea principală din cadrul aplicației
        val navView: BottomNavigationView = binding.navMeniu

        val navController = findNavController(R.id.activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_explore, R.id.nav_profil
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        supportActionBar?.hide()    //ascundere ActionBar

        user = intent.getSerializableExtra("user") as User
    }

    //suprascriere a metodei ce este apelată în momentul în care utilizatorul apasă pe butonul "back" din bara de navigație
    override fun onBackPressed() {
        exitAplicatie()
    }

    //funcție ce afișează un AlertDialog, în care utilizatorul este întrebat dacă dorește să închidă aplicația
    private fun exitAplicatie() {
        MaterialAlertDialogBuilder(this)
            .setMessage("Vrei să închizi aplicația?")
            //finishAffinity închide activitățile începute anterior (în acest caz ActivityAuth și ActivityMain)
            .setPositiveButton("Da") { _, _ -> finishAffinity() }
            .setNegativeButton("Nu", null)
            .show()
    }

    fun getUser(): User {
        return user
    }
}