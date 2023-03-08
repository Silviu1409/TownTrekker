package com.example.licenta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.licenta.databinding.ActivityStartBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ActivityStart : AppCompatActivity() {
    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    //suprascriere a metodei ce este apelată în momentul în care utilizatorul apasă pe butonul "back" din bara de navigație
    override fun onBackPressed() {
        exitAplicatie()
    }

    //funcție ce afișează un AlertDialog, în care utilizatorul este întrebat dacă dorește să închidă aplicația
    private fun exitAplicatie() {
        MaterialAlertDialogBuilder(this)
            .setMessage("Vrei să închizi aplicația?")
            //finish închide activitatea curentă (ActivityMain)
            .setPositiveButton("Da") { _, _ -> finish() }
            .setNegativeButton("Nu", null)
            .show()
    }
}