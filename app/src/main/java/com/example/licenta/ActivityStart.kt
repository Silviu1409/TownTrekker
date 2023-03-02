package com.example.licenta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.licenta.databinding.ActivityStartBinding
import com.google.firebase.firestore.FirebaseFirestore

class ActivityStart : AppCompatActivity() {
    private lateinit var binding: ActivityStartBinding
    private val tag = "test"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.AuthButonLogare.setOnClickListener {

            val db = FirebaseFirestore.getInstance()

            db.collection("test")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        Log.d(tag, "${document.id} => ${document.data}")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(tag, "Error getting documents.", exception)
                }

            val intent = Intent(this, ActivityMain::class.java)

            startActivity(intent)
        }
    }

    override fun onBackPressed() {}
}