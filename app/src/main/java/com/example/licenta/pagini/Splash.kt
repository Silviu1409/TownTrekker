package com.example.licenta.pagini

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.licenta.ActivityMain
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

        val user = FirebaseAuth.getInstance().currentUser
        Log.d("test", user.toString())
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

                        val intent = Intent(activity, ActivityMain::class.java)
                        intent.putExtra("user", data)
                        startActivity(intent)
                    }
                }
                .addOnFailureListener { exception ->
                    //în cazul unei erori, este returnat un warning cu mesajul de mai jos
                    Log.w("test", "Eroare la preluarea documentelor.", exception)

                    findNavController().navigate(R.id.action_splash_to_login)
                }
        } else {
            Log.d("test", "Nu avem user logat")

            findNavController().navigate(R.id.action_splash_to_login)
        }
    }
}