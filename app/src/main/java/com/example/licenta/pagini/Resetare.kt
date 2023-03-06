package com.example.licenta.pagini

import android.content.Context
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
import com.example.licenta.databinding.PaginaResetareBinding
import com.google.firebase.auth.FirebaseAuth

class Resetare : Fragment(){
    private var _binding: PaginaResetareBinding? = null
    private val tag = "test"    //tag folosit pentru testare

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PaginaResetareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ResetareButon.setOnClickListener {
            val email = binding.ResetareEmail.text.toString()

            requireActivity().let { activity ->
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {    // autentificarea s-a facut cu succes
                            // s-a trimis mail pentru resetarea parolei
                            Log.w(tag, "Link trimis pe email!")
                            Toast.makeText(activity, "Link trimis pe email!", Toast.LENGTH_SHORT).show()

                            // curăț câmpul pentru email
                            binding.ResetareEmail.text.clear()

                            // ascund tastatura
                            val view = activity.currentFocus
                            if(view != null) {
                                val inputMethodManager = activity.getSystemService(
                                    Context.INPUT_METHOD_SERVICE
                                ) as InputMethodManager
                                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                            }
                        }
                        else {
                            // autentificarea nu s-a facut cu succes
                            Log.w(tag, "Eroare la resetare", task.exception)
                            Toast.makeText(activity, "Resetare eșuată.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        binding.ResetToAuth.setOnClickListener{
            findNavController().navigate(R.id.action_reset_to_auth)
        }

        binding.ResetToLogin.setOnClickListener{
            findNavController().navigate(R.id.action_reset_to_login)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}