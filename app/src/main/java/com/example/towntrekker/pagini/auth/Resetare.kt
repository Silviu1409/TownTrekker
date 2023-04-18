package com.example.towntrekker.pagini.auth

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.towntrekker.ActivityAuth
import com.example.towntrekker.R
import com.example.towntrekker.databinding.PaginaResetareBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException


class Resetare : Fragment(){
    private var _binding: PaginaResetareBinding? = null
    private val binding get() = _binding!!

    private lateinit var authActivityContext: ActivityAuth


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PaginaResetareBinding.inflate(inflater, container, false)

        authActivityContext = (activity as ActivityAuth)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // preia informații despre starea de conectivitate a user-ului la rețea
        val managerConectivitate = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val retea = managerConectivitate.activeNetwork
        val capabilities = managerConectivitate.getNetworkCapabilities(retea)
        val conectatInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true


        binding.ResetareButon.setOnClickListener {
            val email = binding.ResetareEmail.text.toString()

            authActivityContext.ascundereTastatura()

            if (email == "" ){
                Log.w(authActivityContext.getTag(), "Camp gol!")
                Toast.makeText(activity, "Câmp email gol!", Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }

            requireActivity().let { activity ->
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {    // autentificarea s-a facut cu succes
                            // curăț câmpul pentru email
                            binding.ResetareEmail.text.clear()

                            // s-a trimis mail pentru resetarea parolei
                            Log.d(authActivityContext.getTag(), "Link trimis pe email!")
                            Toast.makeText(activity, "Link trimis pe email!", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            if (!conectatInternet) {
                                // user-ul nu are conexiune la internet
                                Log.e(authActivityContext.getErrTag(), "Lipsă coneziune internet: ${task.exception}")
                                Toast.makeText(activity, "Nu ești conectat la internet!", Toast.LENGTH_LONG).show()
                            } else {
                                // resetarea nu s-a facut cu succes
                                try {
                                    throw task.exception!!
                                } catch (e: FirebaseAuthInvalidUserException) {
                                    Log.e(authActivityContext.getErrTag(), "Contul nu există: ${e.message}")
                                    Toast.makeText(activity, "Acest cont nu există!", Toast.LENGTH_SHORT).show()
                                } catch (e: FirebaseAuthInvalidCredentialsException) {
                                    Log.e(authActivityContext.getErrTag(), "Email greșit: ${e.message}")
                                    Toast.makeText(activity, "Format email greșit!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e(authActivityContext.getErrTag(), "Eroare la resetare: ${e.message}")
                                    Toast.makeText(activity, "Resetare eșuată! Mai incearcă.", Toast.LENGTH_SHORT).show()
                                }
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