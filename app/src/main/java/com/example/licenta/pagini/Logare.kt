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
import com.example.licenta.databinding.PaginaLogareBinding
import com.example.licenta.datatypes.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Logare : Fragment() {
    private var _binding: PaginaLogareBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PaginaLogareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // preia informații despre starea de conectivitate a user-ului la rețea
        val conectivitate = (requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo

        binding.LoginButon.setOnClickListener {
            val email = binding.LoginEmail.text.toString()
            val parola = binding.LoginParola.text.toString()

            (activity as ActivityStart).ascundereTastatura()

            requireActivity().let { activity ->
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, parola)
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) { // logarea s-a făcut cu succes
                            val user = FirebaseAuth.getInstance().currentUser
                            val db = FirebaseFirestore.getInstance()

                            if (user != null) {
                                db.collection("useri")
                                    .document(user.uid)
                                    .get()
                                    .addOnSuccessListener {document ->
                                        if (document != null && document.exists()) {
                                            // documentul cu userul există
                                            val date = User("" + document.getString("email"),
                                                            "" + document.getString("alias"))

                                            Log.w((activity as ActivityStart).tag, "Logare făcută cu succes")
                                            Toast.makeText(activity, "Logare făcută cu succes", Toast.LENGTH_SHORT).show()

                                            // schimbă activitatea și transmite datele despre user ca parametru
                                            val intent = Intent(activity, ActivityMain::class.java)
                                            intent.putExtra("user", date)
                                            startActivity(intent)
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        // în cazul unei erori, este returnat un warning cu mesajul de mai jos
                                        Log.w((activity as ActivityStart).tag, "Eroare la preluarea documentelor", exception)
                                        Toast.makeText(activity, "Logare eșuată", Toast.LENGTH_SHORT).show()
                                    }

                            } else {
                                // user-ul nu există
                                Log.w((activity as ActivityStart).tag, "Userul nu există", task.exception)
                                Toast.makeText(activity, "Logare eșuată", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (conectivitate == null || !conectivitate.isConnected) {
                                // user-ul nu are conexiune la internet
                                Log.w((activity as ActivityStart).tag, "Lipsă conexiune internet", task.exception)
                                Toast.makeText(activity, "Lipsă conexiune internet", Toast.LENGTH_LONG).show()
                            } else {
                                // autentificarea nu s-a făcut cu succes
                                Log.w((activity as ActivityStart).tag, "Eroare la logare", task.exception)
                                Toast.makeText(activity, "Logare eșuată", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
        }

        binding.LoginToAuth.setOnClickListener{
            findNavController().navigate(R.id.action_login_to_auth)
        }

        binding.LoginToReset.setOnClickListener{
            findNavController().navigate(R.id.action_login_to_reset)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}