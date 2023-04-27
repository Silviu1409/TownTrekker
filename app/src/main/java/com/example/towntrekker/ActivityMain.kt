package com.example.towntrekker

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.towntrekker.databinding.ActivityMainBinding
import com.example.towntrekker.datatypes.User
import com.example.towntrekker.pagini.main.AdaugaPostare
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.Locale


class ActivityMain : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val tag = "test"    // tag folosit pentru testare in cazurile de debug/warning
    private val err = "err"     // tag folosit pentru testare pentru erori

    private var user: User? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var folderRef: StorageReference
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: StorageReference

    private lateinit var placesClient: PlacesClient

    private lateinit var sharedPrefsUser: SharedPreferences   // fișierul din shared preferences ce conține date despre user


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

        sharedPrefsUser = getSharedPreferences("user", Context.MODE_PRIVATE)

        if (sharedPrefsUser.contains("uid")) {
            user = User(sharedPrefsUser.getString("uid", "")!!,
                sharedPrefsUser.getString("email", "")!!,
                sharedPrefsUser.getString("alias", "")!!,
                sharedPrefsUser.getString("bio", "")!!,
                sharedPrefsUser.getString("parola", "")!!)
        }
        else {
            @Suppress("DEPRECATION")
            user = intent.getSerializableExtra("user") as? User     // preiau datele despre utilizator

            sharedPrefsUser.edit().putString("uid", user!!.uid).apply()
            sharedPrefsUser.edit().putString("alias", user!!.alias).apply()
            sharedPrefsUser.edit().putString("email", user!!.email).apply()
            sharedPrefsUser.edit().putString("bio", user!!.bio).apply()
            sharedPrefsUser.edit().putString("parola", user!!.parola).apply()
        }

        folderRef = Firebase.storage.reference.child(user!!.uid)    // referință folder Firebase Storage creat pt. fiecare user
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = Firebase.storage.reference

        // configurare navigare pentru secțiunea principală din cadrul aplicației
        val navView: BottomNavigationView = binding.navMeniu

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_explore, R.id.nav_mesaje, R.id.nav_cont
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        supportActionBar?.hide()    //ascundere ActionBar
        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)

        @Suppress("DEPRECATION")
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext,
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData.getString("com.google.android.geo.API_KEY")!!,
                Locale("ro", "RO")
            )
        }
        placesClient = Places.createClient(this)
    }

    // funcție ce afișează un AlertDialog, în care utilizatorul este întrebat dacă dorește să închidă aplicația
    private fun exitAplicatie() {
        MaterialAlertDialogBuilder(this)
            .setMessage("Vrei să închizi aplicația?")
            // finishAffinity închide activitățile începute anterior (în acest caz ActivityAuth și ActivityMain)
            .setPositiveButton("Da") { _, _ -> finishAffinity() }
            .setNegativeButton("Nu", null)
            .show()
    }

    @Suppress("UNUSED_PARAMETER")
    fun adaugaPostare(view: View) {
        val dialog = AdaugaPostare()
        dialog.show(supportFragmentManager, "Adaugă postare")
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

    fun getAuth(): FirebaseAuth {
        return auth
    }

    fun getSharedPrefsUser(): SharedPreferences {
        return sharedPrefsUser
    }

    fun getStorage(): StorageReference {
        return storage
    }

    // funcție pentru ascunderea tastaturii
    fun ascundereTastatura() {
        if(currentFocus != null) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    // funcție ce șterge datele legate de Shared Preferences pentru utilizatorul curent
    fun stergeSharedPrefsUser(){
        sharedPrefsUser.edit().clear().apply()
    }
}