package com.example.towntrekker

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.towntrekker.databinding.ActivityMainBinding
import com.example.towntrekker.datatypes.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage


class ActivityMain : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val tag = "test"    // tag folosit pentru testare in cazurile de debug/warning
    private val err = "err"     // tag folosit pentru testare pentru erori

    private var user: User? = null

    private lateinit var folderRef: StorageReference
    private lateinit var db: FirebaseFirestore

    private lateinit var sharedPreferencesUser: SharedPreferences   // fișierul din shared preferences ce conține date despre user


    // suprascriere a metodei ce este apelată în momentul în care utilizatorul apasă pe butonul "back" din bara de navigație
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            exitAplicatie()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferencesUser = getSharedPreferences("user", Context.MODE_PRIVATE)

        if (sharedPreferencesUser.contains("uid")) {
            user = User(sharedPreferencesUser.getString("uid", "")!!,
                sharedPreferencesUser.getString("email", "")!!,
                sharedPreferencesUser.getString("alias", "")!!)
        }
        else {
            @Suppress("DEPRECATION")
            user = intent.getSerializableExtra("user") as? User     // preiau datele despre utilizator

            sharedPreferencesUser.edit().putString("uid", user!!.uid).apply()
            sharedPreferencesUser.edit().putString("alias", user!!.alias).apply()
            sharedPreferencesUser.edit().putString("email", user!!.email).apply()
        }

        folderRef = Firebase.storage.reference.child(user!!.uid)    // referință folder Firebase Storage creat pt. fiecare user
        db = FirebaseFirestore.getInstance()

        // configurare navigare pentru secțiunea principală din cadrul aplicației
        val navView: BottomNavigationView = binding.navMeniu

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_explore, R.id.nav_profil
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        supportActionBar?.hide()    //ascundere ActionBar
        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)
    }

    // funcție ce afișează un AlertDialog, în care utilizatorul este întrebat dacă dorește să închidă aplicația
    private fun exitAplicatie() {
        MaterialAlertDialogBuilder(this)
            .setMessage("Vrei să închizi aplicația?")
            .setPositiveButton("Da") { _, _ -> finishAffinity() }   // finishAffinity închide activitățile începute anterior (în acest caz ActivityAuth și ActivityMain)
            .setNegativeButton("Nu", null)
            .show()
    }

    fun getUser(): User? {
        return user
    }

    fun getTag(): String {
        return tag
    }

    fun getErrTag(): String {
        return err
    }

    fun getFolderRef(): StorageReference {
        return folderRef
    }

    fun getDB(): FirebaseFirestore {
        return db
    }

    // funcție pentru ascunderea tastaturii
    fun ascundereTastatura() {
        if(currentFocus != null) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    // funcție ce șterge datele legate de Shared Preferences pentru utilizatorul curent
    fun stergeSharedprefUser(){
        sharedPreferencesUser.edit().clear().apply()
    }
}