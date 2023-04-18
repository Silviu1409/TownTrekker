package com.example.towntrekker

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.towntrekker.databinding.ActivityAuthBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class ActivityAuth : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private val tag = "test"    // tag folosit pentru testare in cazurile de debug//waning
    private val err = "err"    // tag folosit pentru testare pentru erori

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        user = auth.currentUser
    }

    // suprascriere a metodei ce este apelată în momentul în care utilizatorul apasă pe butonul "back" din bara de navigație
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            exitAplicatie()
        }
    }

    // funcție ce afișează un AlertDialog, în care utilizatorul este întrebat dacă dorește să închidă aplicația
    private fun exitAplicatie() {
        MaterialAlertDialogBuilder(this)
            .setMessage("Vrei să închizi aplicația?")
            .setPositiveButton("Da") { _, _ -> finish() }   // închide activitatea curentă (ActivityMain)
            .setNegativeButton("Nu", null)
            .show()
    }

    // funcție pentru ascundere tastaturii
    fun ascundereTastatura() {
        if(currentFocus != null) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    fun getTag(): String {
        return tag
    }

    fun getErrTag(): String {
        return err
    }

    fun getUser(): FirebaseUser? {
        return user
    }

    fun getDB(): FirebaseFirestore {
        return db
    }
}