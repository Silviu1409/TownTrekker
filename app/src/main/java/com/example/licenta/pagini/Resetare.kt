package com.example.licenta.pagini

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
import com.example.licenta.ActivityStart
import com.example.licenta.R
import com.example.licenta.databinding.PaginaResetareBinding
import com.google.firebase.auth.FirebaseAuth

class Resetare : Fragment(){
    private var _binding: PaginaResetareBinding? = null

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

        // preia informații despre starea de conectivitate a user-ului la rețea
        val conectivitate = (requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo

        binding.ResetareButon.setOnClickListener {
            val email = binding.ResetareEmail.text.toString()

            (activity as ActivityStart).ascundereTastatura()

            requireActivity().let { activity ->
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {    // autentificarea s-a facut cu succes
                            // curăț câmpul pentru email
                            binding.ResetareEmail.text.clear()

                            // s-a trimis mail pentru resetarea parolei
                            Log.w((activity as ActivityStart).tag, "Link trimis pe email!")
                            Toast.makeText(activity, "Link trimis pe email!", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            if (conectivitate == null || !conectivitate.isConnected) {
                                // user-ul nu are conexiune la internet
                                Log.w((activity as ActivityStart).tag, "Lipsă conexiune internet", task.exception)
                                Toast.makeText(activity, "Lipsă conexiune internet", Toast.LENGTH_LONG).show()
                            } else {
                                // autentificarea nu s-a facut cu succes
                                Log.w((activity as ActivityStart).tag, "Eroare la resetare", task.exception)
                                Toast.makeText(activity, "Resetare eșuată.", Toast.LENGTH_SHORT).show()
                            }
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