package com.example.licenta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.licenta.databinding.ActivityStartBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore

class ActivityStart : AppCompatActivity() {
    private lateinit var binding: ActivityStartBinding
    private val tag = "test"    //tag folosit pentru testare

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.AuthButonLogare.setOnClickListener {
            //testez legătura cu baza de date, preluând date de la un tabel de test
            val db = FirebaseFirestore.getInstance()
            db.collection("test")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        //dacă se realizează conexiunea cu succes, output-ul se poate vedea în Logcat
                        Log.d(tag, "${document.id} => ${document.data}")
                    }
                }
                .addOnFailureListener { exception ->
                    //în cazul unei erori, este returnat un warning cu mesajul de mai jos
                    Log.w(tag, "Eroare la preluarea documentelor.", exception)
                }

            val intent = Intent(this, ActivityMain::class.java)
            startActivity(intent)
        }
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