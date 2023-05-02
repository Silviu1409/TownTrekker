package com.example.towntrekker.pagini.auth

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.ActivityAuth
import com.example.towntrekker.R
import com.example.towntrekker.databinding.PaginaLogareBinding
import com.example.towntrekker.datatypes.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore


class Logare : Fragment() {
    private var _binding: PaginaLogareBinding? = null
    private val binding get() = _binding!!

    private lateinit var authActivityContext: ActivityAuth

    private var parolaAfisata: Boolean = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = PaginaLogareBinding.inflate(inflater, container, false)

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


        binding.LoginGoogle.setOnClickListener {
            authActivityContext.logareGoogle()
        }

        binding.LoginParolaToggle.setOnClickListener {
            if (!parolaAfisata){
                binding.LoginParola.transformationMethod = null
                parolaAfisata = true
                binding.LoginParolaToggle.setImageResource(R.drawable.icon_vis_off)
            }
            else {
                binding.LoginParola.transformationMethod = PasswordTransformationMethod()
                parolaAfisata = false
                binding.LoginParolaToggle.setImageResource(R.drawable.icon_vis_on)
            }
        }

        binding.LoginButon.setOnClickListener {
            val email = binding.LoginEmail.text.toString()
            val parola = binding.LoginParola.text.toString()

            authActivityContext.ascundereTastatura()

            if (email == "" || parola == ""){
                Log.w(authActivityContext.getTag(), "Camp gol!")
                Toast.makeText(activity, "Câmp email/parolă gol!", Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }

            requireActivity().let { activity ->
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, parola)
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {    // logarea s-a făcut cu succes
                            val user = FirebaseAuth.getInstance().currentUser
                            val db = FirebaseFirestore.getInstance()

                            if (user != null) {
                                db.collection("useri")
                                    .document(user.uid)
                                    .get()
                                    .addOnSuccessListener {document ->
                                        if (document != null && document.exists()) {
                                            // documentul cu userul există
                                            val date = User(user.uid,
                                                            "" + document.getString("email"),
                                                            "" + document.getString("alias"),
                                                            "" + document.getString("bio"),
                                                            "" + document.getString("parola"))

                                            Log.d(authActivityContext.getTag(), "Logare făcută cu succes")
                                            Toast.makeText(activity, "Logare făcută cu succes", Toast.LENGTH_SHORT).show()

                                            // schimbă activitatea și transmite datele despre user ca parametru
                                            val intent = Intent(activity, ActivityMain::class.java)
                                            intent.putExtra("user", date)
                                            startActivity(intent)
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        // în cazul unei erori, este returnat un warning cu mesajul de mai jos
                                        Log.e(authActivityContext.getErrTag(), "Eroare la preluarea documentelo: ${e.message}")
                                        Toast.makeText(activity, "Logare eșuată", Toast.LENGTH_SHORT).show()
                                    }

                            } else {
                                // user-ul nu există
                                Log.e(authActivityContext.getErrTag(), "Userul nu există: ${task.exception}")
                                Toast.makeText(activity, "Logare eșuată", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (!conectatInternet) {
                                // user-ul nu are conexiune la internet
                                Log.e(authActivityContext.getErrTag(), "Lipsă coneziune internet: ${task.exception}")
                                Toast.makeText(activity, "Nu ești conectat la internet!", Toast.LENGTH_LONG).show()
                            } else {
                                // logarea nu s-a facut cu succes

                                try {
                                    throw task.exception!!
                                } catch (e: FirebaseAuthInvalidUserException) {
                                    Log.e(authActivityContext.getErrTag(), "Contul nu există: ${e.message}")
                                    Toast.makeText(activity, "Acest cont nu există!", Toast.LENGTH_SHORT).show()
                                } catch (e: FirebaseAuthInvalidCredentialsException) {
                                    Log.e(authActivityContext.getErrTag(), "Parolă greșită: ${e.message}")
                                    Toast.makeText(activity, "Parola este greșită!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e(authActivityContext.getErrTag(), "Eroare la logare: ${e.message}")
                                    Toast.makeText(activity, "Logare eșuată! Mai incearcă.", Toast.LENGTH_SHORT).show()
                                }
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