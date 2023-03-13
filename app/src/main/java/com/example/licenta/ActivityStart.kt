package com.example.licenta

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.example.licenta.databinding.ActivityStartBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ActivityStart : AppCompatActivity() {
    private lateinit var binding: ActivityStartBinding
    val tag = "test"    // tag folosit pentru testare

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    // suprascriere a metodei ce este apelată în momentul în care utilizatorul apasă pe butonul "back" din bara de navigație
    override fun onBackPressed() {
        exitAplicatie()
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
}