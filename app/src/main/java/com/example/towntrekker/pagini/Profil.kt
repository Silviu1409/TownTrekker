package com.example.towntrekker.pagini

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.ActivityStart
import com.example.towntrekker.R
import com.example.towntrekker.databinding.PaginaProfilBinding
import com.google.firebase.auth.FirebaseAuth

class Profil : Fragment() {
    private var _binding: PaginaProfilBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PaginaProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val act = activity as ActivityMain

        binding.emailProfil.text = getString(R.string.email_profil_user, act.getUser().email)
        binding.aliasProfil.text = getString(R.string.alias_profil_user, act.getUser().alias)

        binding.logoutProfil.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            Log.w(tag, "Delogare făcută cu succes.")
            Toast.makeText(activity, "Delogare făcută cu succes.", Toast.LENGTH_SHORT).show()


            val intent = Intent(activity, ActivityStart::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}