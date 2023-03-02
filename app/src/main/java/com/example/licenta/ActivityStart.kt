package com.example.licenta

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.licenta.databinding.ActivityStartBinding

class ActivityStart : AppCompatActivity() {
    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.AuthButonLogare.setOnClickListener {
            val intent = Intent(this, ActivityMain::class.java)

            startActivity(intent)
        }
    }

    override fun onBackPressed() {}
}