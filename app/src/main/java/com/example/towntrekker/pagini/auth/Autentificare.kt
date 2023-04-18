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
import com.example.towntrekker.databinding.PaginaAutentificareBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore


class Autentificare : Fragment() {
    private var _binding: PaginaAutentificareBinding? = null
    private val binding get() = _binding!!

    private lateinit var authActivityContext: ActivityAuth


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PaginaAutentificareBinding.inflate(inflater, container, false)

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


        binding.AuthButon.setOnClickListener {
            val email = binding.AuthEmail.text.toString()
            val parola = binding.AuthParola.text.toString()
            val alias = binding.AuthAlias.text.toString()

            authActivityContext.ascundereTastatura()

            if (email == "" || parola == "" || alias == ""){
                Log.w(authActivityContext.getTag(), "Camp gol!")
                Toast.makeText(activity, "Câmp email/parolă/alias gol!", Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }

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
                                        Log.d(authActivityContext.getTag(), "Înregistrare făcută cu succes")
                                        Toast.makeText(activity, "Autentificare făcută cu succes", Toast.LENGTH_SHORT).show()

                                        // curăț câmpurile completate anterior
                                        binding.AuthEmail.text.clear()
                                        binding.AuthParola.text.clear()
                                        binding.AuthAlias.text.clear()
                                    }
                                    .addOnFailureListener { e ->
                                        // în cazul unei erori, este returnat un warning cu mesajul de mai jos
                                        Log.e(authActivityContext.getErrTag(), "Eroare la preluarea documentelor: ${e.message}")
                                        Toast.makeText(activity, "Autentificare eșuată", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Log.e(authActivityContext.getErrTag(), "Userul nu a fost creat: ${task.exception}")
                                Toast.makeText(activity, "Autentificare eșuată", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (!conectatInternet) {
                                // user-ul nu are conexiune la internet
                                Log.e(authActivityContext.getErrTag(), "Lipsă coneziune internet: ${task.exception}")
                                Toast.makeText(activity, "Nu ești conectat la internet!", Toast.LENGTH_LONG).show()
                            } else {
                                // autentificarea nu s-a facut cu succes
                                try {
                                    throw task.exception!!
                                } catch (e: FirebaseAuthWeakPasswordException) {
                                    Log.e(authActivityContext.getErrTag(), "Parola este slabă: ${e.message}")
                                    Toast.makeText(activity, "Parola este slabă.\n Încearcă o altă parolă!", Toast.LENGTH_SHORT).show()
                                } catch (e: FirebaseAuthInvalidCredentialsException) {
                                    Log.e(authActivityContext.getErrTag(), "Format email greșit: ${e.message}")
                                    Toast.makeText(activity, "Formatul email-ului este greșit!", Toast.LENGTH_SHORT).show()
                                } catch (e: FirebaseAuthUserCollisionException) {
                                    Log.e(authActivityContext.getErrTag(), "Cont deja existent: ${e.message}")
                                    Toast.makeText(activity, "Un cont există deja pentru această adresă de email!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e(authActivityContext.getErrTag(), "Eroare la autentificare: ${e.message}")
                                    Toast.makeText(activity, "Autentificare eșuată! Mai incearcă.", Toast.LENGTH_SHORT).show()
                                }
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