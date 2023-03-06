package com.example.licenta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.licenta.databinding.ActivityStartBinding
import com.example.licenta.datatypes.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityStart : AppCompatActivity() {
    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("useri")
                .document(user.uid)
                .get()
                .addOnSuccessListener {document ->
                    if (document != null && document.exists()) {
                        // documentul cu userul exista
                        val data = User("" + document.getString("email"),
                            "" + document.getString("alias"))

                        val intent = Intent(this, ActivityMain::class.java)
                        intent.putExtra("user", data)
                        startActivity(intent)
                    }
                }
                .addOnFailureListener { exception ->
                    //în cazul unei erori, este returnat un warning cu mesajul de mai jos
                    Log.w("test", "Eroare la preluarea documentelor.", exception)
                    Toast.makeText(this, "Logare automată eșuată.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.d("test", "Nu avem user logat")
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