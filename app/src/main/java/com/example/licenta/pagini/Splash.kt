package com.example.licenta.pagini

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.licenta.ActivityMain
import com.example.licenta.ActivityStart
import com.example.licenta.R
import com.example.licenta.databinding.PaginaSplashBinding
import com.example.licenta.datatypes.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Splash : Fragment() {
    private var _binding: PaginaSplashBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PaginaSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // preia informații despre starea de conectivitate a user-ului la rețea
        val conectivitate = (requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo

        // preia informații despre user-ul logat (dacă acesta există)
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("useri")
                .document(user.uid)
                .get()
                .addOnSuccessListener {document ->
                    if (document != null && document.exists()) {
                        // documentul cu detalii despre user există
                        val data = User("" + document.getString("email"),
                            "" + document.getString("alias"))

                        // schimbă activitatea și transmite datele despre user ca parametru (logare automată)
                        val intent = Intent(activity, ActivityMain::class.java)
                        intent.putExtra("user", data)
                        startActivity(intent)
                    }
                }
                .addOnFailureListener { exception ->
                    // în cazul unei erori, este returnat un warning cu mesajul de mai jos
                    Log.w((activity as ActivityStart).tag, "Eroare la preluarea documentelor", exception)

                    // schimbă fragmentul către cel de login
                    findNavController().navigate(R.id.action_splash_to_login)
                }
        } else {
            if (conectivitate == null || !conectivitate.isConnected) {
                // dacă user-ul nu este conectat la internet, afișează un mesaj pe ecran
                Toast.makeText(activity, "Pentru a te putea loga/înregistra, trebuie să te conectezi la internet", Toast.LENGTH_LONG).show()
            }
            Log.d((activity as ActivityStart).tag, "Nu avem user logat")

            // schimbă fragmentul către cel de login
            findNavController().navigate(R.id.action_splash_to_login)
        }
    }
}