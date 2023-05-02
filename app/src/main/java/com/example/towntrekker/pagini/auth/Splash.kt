package com.example.towntrekker.pagini.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.ActivityAuth
import com.example.towntrekker.R
import com.example.towntrekker.databinding.PaginaSplashBinding
import com.example.towntrekker.datatypes.User


class Splash : Fragment() {
    private var _binding: PaginaSplashBinding? = null
    private val binding get() = _binding!!

    private lateinit var authActivityContext: ActivityAuth


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = PaginaSplashBinding.inflate(inflater, container, false)

        authActivityContext = (activity as ActivityAuth)

        val motionLayout = binding.SplashScreen
        motionLayout.addTransitionListener(object: MotionLayout.TransitionListener{
            override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
                if (authActivityContext.getDelogare()){
                    findNavController().navigate(R.id.action_splash_to_login)
                }
            }

            override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float) {

            }

            override fun onTransitionTrigger(motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float) {

            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                if (authActivityContext.getUser() != null) {
                    if (authActivityContext.getSharedPreferencesUser().contains("uid")) {
                        val intent = Intent(activity, ActivityMain::class.java)
                        startActivity(intent)
                    }
                    else {
                        authActivityContext.getDB().collection("useri")
                            .document(authActivityContext.getUser()!!.uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val dateUser = User(
                                        authActivityContext.getUser()!!.uid,
                                        "" + document.getString("email"),
                                        "" + document.getString("alias")
                                    )

                                    val intent = Intent(activity, ActivityMain::class.java)
                                    intent.putExtra("user", dateUser)
                                    startActivity(intent)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e(authActivityContext.getErrTag(), "Eroare la preluarea documentelor: ${e.message}")
                                Toast.makeText(context, "Nu s-a putut efectua logarea.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                else {
                    findNavController().navigate(R.id.action_splash_to_login)
                }
            }
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}