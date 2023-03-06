package com.example.licenta.pagini

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.licenta.ActivityMain
import com.example.licenta.R
import com.example.licenta.databinding.PaginaLogareBinding
import com.example.licenta.datatypes.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Logare : Fragment() {
    private var _binding: PaginaLogareBinding? = null
    private val tag = "test"    //tag folosit pentru testare

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

        binding.LoginButon.setOnClickListener {
            val email = binding.LoginEmail.text.toString()
            val parola = binding.LoginParola.text.toString()

            requireActivity().let { activity ->
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, parola)
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {
                            // logarea s-a facut cu succes
                            val user = FirebaseAuth.getInstance().currentUser

                            val db = FirebaseFirestore.getInstance()
                            if (user != null) {
                                Log.d("aux", user.uid)
                                db.collection("useri")
                                    .document(user.uid)
                                    .get()
                                    .addOnSuccessListener {document ->
                                        if (document != null && document.exists()) {
                                            // documentul cu userul exista
                                            val data = User("" + document.getString("email"),
                                                            "" + document.getString("alias"))

                                            Log.w(tag, "Logare făcută cu succes.")
                                            Toast.makeText(activity, "Logare făcută cu succes.", Toast.LENGTH_SHORT).show()


                                            val intent = Intent(activity, ActivityMain::class.java)
                                            intent.putExtra("user", data)
                                            startActivity(intent)
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        //în cazul unei erori, este returnat un warning cu mesajul de mai jos
                                        Log.w(tag, "Eroare la preluarea documentelor.", exception)
                                        Toast.makeText(activity, "Logare eșuată.", Toast.LENGTH_SHORT).show()
                                    }

                            } else {
                                Log.w(tag, "Userul nu are uid.", task.exception)
                                Toast.makeText(activity, "Logare eșuată.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // autentificarea nu s-a facut cu succes
                            Log.w(tag, "Eroare la logare", task.exception)
                            Toast.makeText(activity, "Logare eșuată.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        binding.LoginToAuth.setOnClickListener{
            findNavController().navigate(R.id.action_login_to_auth)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}