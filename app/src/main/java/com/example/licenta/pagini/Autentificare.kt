package com.example.licenta.pagini

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.licenta.R
import com.example.licenta.databinding.PaginaAutentificareBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Autentificare : Fragment() {
    private var _binding: PaginaAutentificareBinding? = null
    private val tag = "test"    //tag folosit pentru testare

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

        binding.AuthButon.setOnClickListener {
            val email = binding.AuthEmail.text.toString()
            val parola = binding.AuthParola.text.toString()
            val alias = binding.AuthAlias.text.toString()

            requireActivity().let { activity ->
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, parola)
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {    // autentificarea s-a facut cu succes
                            val db = FirebaseFirestore.getInstance()
                            val user = FirebaseAuth.getInstance().currentUser

                            val data = hashMapOf(
                                "email" to email,
                                "alias" to alias
                            )

                            if (user != null) {
                                db.collection("useri")
                                    .document(user.uid)
                                    .set(data)
                                    .addOnSuccessListener {
                                        // s-a creat un tabel pentru noul user
                                        Log.w(tag, "Înregistrare făcută cu succes.")
                                        Toast.makeText(activity, "Autentificare făcută cu succes.", Toast.LENGTH_SHORT).show()

                                        // curăț câmpurile completate anterior
                                        binding.AuthEmail.text.clear()
                                        binding.AuthParola.text.clear()
                                        binding.AuthAlias.text.clear()

                                        // ascund tastatura
                                        val view = activity.currentFocus
                                        if(view != null) {
                                            val inputMethodManager = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                                            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        //în cazul unei erori, este returnat un warning cu mesajul de mai jos
                                        Log.w(tag, "Eroare la preluarea documentelor.", exception)
                                        Toast.makeText(activity, "Autentificare eșuată.", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Log.w(tag, "Userul nu are uid.", task.exception)
                                Toast.makeText(activity, "Autentificare eșuată.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // autentificarea nu s-a facut cu succes
                            Log.w(tag, "Eroare la autentificare", task.exception)
                            Toast.makeText(activity, "Autentificare eșuată.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        binding.AuthToLogin.setOnClickListener{
            findNavController().navigate(R.id.action_auth_to_login)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}