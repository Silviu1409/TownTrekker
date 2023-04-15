package com.example.towntrekker.pagini

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.towntrekker.ActivityStart
import com.example.towntrekker.R
import com.example.towntrekker.databinding.PaginaAutentificareBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Autentificare : Fragment() {
    private var _binding: PaginaAutentificareBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PaginaAutentificareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // preia informații despre starea de conectivitate a user-ului la rețea
        val conectivitate = (requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo

        binding.AuthButon.setOnClickListener {
            val email = binding.AuthEmail.text.toString()
            val parola = binding.AuthParola.text.toString()
            val alias = binding.AuthAlias.text.toString()

            (activity as ActivityStart).ascundereTastatura()

            requireActivity().let { activity ->
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, parola)
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {    // autentificarea s-a făcut cu succes
                            val db = FirebaseFirestore.getInstance()
                            val user = FirebaseAuth.getInstance().currentUser

                            val date = hashMapOf(
                                "email" to email,
                                "alias" to alias
                            )

                            if (user != null) {
                                db.collection("useri")
                                    .document(user.uid)
                                    .set(date)
                                    .addOnSuccessListener {
                                        // s-a creat un tabel pentru noul user
                                        Log.w((activity as ActivityStart).tag, "Înregistrare făcută cu succes")
                                        Toast.makeText(activity, "Autentificare făcută cu succes", Toast.LENGTH_SHORT).show()

                                        // curăț câmpurile completate anterior
                                        binding.AuthEmail.text.clear()
                                        binding.AuthParola.text.clear()
                                        binding.AuthAlias.text.clear()
                                    }
                                    .addOnFailureListener { exception ->
                                        // în cazul unei erori, este returnat un warning cu mesajul de mai jos
                                        Log.w((activity as ActivityStart).tag, "Eroare la preluarea documentelor", exception)
                                        Toast.makeText(activity, "Autentificare eșuată", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Log.w((activity as ActivityStart).tag, "Userul nu a fost creat", task.exception)
                                Toast.makeText(activity, "Autentificare eșuată", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (conectivitate == null || !conectivitate.isConnected) {
                                // user-ul nu are conexiune la internet
                                Log.w((activity as ActivityStart).tag, "Lipsă conexiune internet", task.exception)
                                Toast.makeText(activity, "Lipsă conexiune internet", Toast.LENGTH_LONG).show()
                            } else {
                                // autentificarea nu s-a făcut cu succes
                                Log.w((activity as ActivityStart).tag, "Eroare la autentificare", task.exception)
                                Toast.makeText(activity, "Autentificare eșuată", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
        }

        binding.AuthToLogin.setOnClickListener{
            findNavController().navigate(R.id.action_auth_to_login)
        }

        binding.AuthToReset.setOnClickListener{
            findNavController().navigate(R.id.action_auth_to_reset)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}