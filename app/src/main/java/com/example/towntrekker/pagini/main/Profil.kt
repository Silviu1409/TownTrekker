package com.example.towntrekker.pagini.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.ActivityAuth
import com.example.towntrekker.R
import com.example.towntrekker.databinding.PaginaProfilBinding
import com.google.firebase.auth.FirebaseAuth

class Profil : Fragment() {
    private var _binding: PaginaProfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainActivityContext: ActivityMain


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PaginaProfilBinding.inflate(inflater, container, false)

        mainActivityContext = activity as ActivityMain

        binding.emailProfil.text = getString(R.string.email_profil_user, mainActivityContext.getUser().email)
        binding.aliasProfil.text = getString(R.string.alias_profil_user, mainActivityContext.getUser().alias)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.logoutProfil.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            Log.d(mainActivityContext.getTag(), "Delogare făcută cu succes.")
            Toast.makeText(activity, "Delogare făcută cu succes!", Toast.LENGTH_SHORT).show()


            val intent = Intent(activity, ActivityAuth::class.java)
            intent.putExtra("logout", true)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}