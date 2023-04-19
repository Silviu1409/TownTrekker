package com.example.towntrekker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.towntrekker.databinding.ActivityAuthBinding
import com.example.towntrekker.datatypes.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream


class ActivityAuth : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    private val tag = "test"    // tag folosit pentru testare, in cazurile de debug/warning
    private val err = "err"     // tag folosit pentru testare, la erori

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: StorageReference

    private var user: FirebaseUser? = null  // date despre user-ul logat, dacă acesta există

    private var logatAnterior = true        // utilizatorul s-a mai logat anterior cu contul Google în această aplicație ?
    private var delogare: Boolean = false   // utilizatorul ajunge aici prin delogare ?

    private lateinit var sharedPreferencesUser: SharedPreferences   // fișierul din shared preferences ce conține date despre user


    // launcher pentru intent-ul de logare, folosind contul Google
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(res.data)

            try {
                val cont = task.getResult(ApiException::class.java)!!
                val email = cont.email.toString()

                // verific dacă user-ul s-a mai logat cu această adresă de mail de la Google
                auth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener { task2 ->
                        if (task2.isSuccessful) {
                            val metodeLogare = task2.result?.signInMethods

                            if (metodeLogare.isNullOrEmpty()) {
                                Log.d(tag, "User-ul nu s-a mai logat!")
                                logatAnterior = false
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(err, "Eroare la preluarea metodelor de logare: ${e.message}")
                    }

                authGoogle(cont.idToken!!)
            } catch (e: ApiException) {
                Log.e(err, "Eroare logare: ${e.message}")

                Toast.makeText(this, "Logare eșuată!", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = Firebase.storage.reference
        sharedPreferencesUser = getSharedPreferences("user", Context.MODE_PRIVATE)

        creareGoogleSignInClient()

        user = auth.currentUser

        val aux = intent.getBooleanExtra("logout", false)

        if (aux){
            delogare = true
        }
    }

    fun logareGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    // funcție în care se realizează autentificarea, folosind servicii Google
    private fun authGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    if (!logatAnterior){
                        val date = HashMap<String, Any>()
                        date["email"] = user?.email.toString()
                        date["alias"] = user?.displayName.toString()

                        if (user != null) {
                            db.collection("useri")
                                .document(user.uid)
                                .set(date)
                                .addOnSuccessListener {
                                    val glide = Glide.with(this)

                                    val requestBuilder = glide.asBitmap()
                                        .load(user.photoUrl.toString())
                                        .apply(RequestOptions().override(75, 75))
                                    requestBuilder.into(object : CustomTarget<Bitmap>() {
                                        override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: Transition<in Bitmap>?
                                        ) {
                                            val folderRef = storage.child(user.uid)
                                            val imageRef = folderRef.child("icon.jpg")

                                            val baos = ByteArrayOutputStream()
                                            resource.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                                            val data = baos.toByteArray()

                                            val uploadTask = imageRef.putBytes(data)
                                            uploadTask
                                                .addOnSuccessListener {
                                                    Log.d(tag, "User înregistrat cu succes!")

                                                    // schimbă activitatea și transmite datele despre user ca parametru
                                                    val userNou = User(user.uid, date["email"].toString(), date["alias"].toString())
                                                    val intent = Intent(this@ActivityAuth, ActivityMain::class.java)
                                                    intent.putExtra("user", userNou)
                                                    startActivity(intent)
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e(err, "Eroare creare folder user: ${e.message}")
                                                    Toast.makeText(this@ActivityAuth, "Nu s-a putut crea folder!", Toast.LENGTH_SHORT).show()
                                                }
                                        }

                                        override fun onLoadCleared(placeholder: Drawable?) {
                                            Log.w(tag, "Cancelled load")
                                        }
                                    })


                                }
                                .addOnFailureListener { e ->
                                    Log.e(err, "Eroare preluare document: ${e.message}")

                                    Toast.makeText(this, "Înregistrare eșuată. Mai încearcă!", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Log.e(err, "User-ul nu a fost înregistrat: ${task.exception}")

                            Toast.makeText(this, "Înregistrare eșuată. Mai încearcă!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        if (user != null) {
                            db.collection("useri")
                                .document(user.uid)
                                .get()
                                .addOnSuccessListener { doc ->
                                    if (doc != null && doc.exists()) {
                                        val dateUser = User(user.uid,
                                            "" + doc.getString("email"),
                                            "" + doc.getString("alias"))

                                        val intent = Intent(this, ActivityMain::class.java)
                                        intent.putExtra("user", dateUser)
                                        startActivity(intent)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(err, "Eroare preluare document: ${e.message}")

                                    Toast.makeText(this, "Logare eșuată. Mai încearcă!", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Log.e(err, "User-ul nu există: ${task.exception}")

                            Toast.makeText(this, "Logare eșuată. Mai încearcă!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e(err, "Eroare logare: ${task.exception}")

                    Toast.makeText(this, "Logare eșuată. Mai încearcă!", Toast.LENGTH_LONG).show()
                }
            }
    }

    // funcție ce crează un Google Sign-In Client, pentru ca utilizatorul să se poată loga folosind contul Google
    private fun creareGoogleSignInClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()
    }

    // suprascriere a metodei ce este apelată în momentul în care utilizatorul apasă pe butonul "back" din bara de navigație
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            exitAplicatie()
        }
    }

    // funcție ce afișează un AlertDialog, în care utilizatorul este întrebat dacă dorește să închidă aplicația
    private fun exitAplicatie() {
        MaterialAlertDialogBuilder(this)
            .setMessage("Vrei să închizi aplicația?")
            .setPositiveButton("Da") { _, _ -> finish() }   // închide activitatea curentă (ActivityMain)
            .setNegativeButton("Nu", null)
            .show()
    }

    // funcție pentru ascunderea tastaturii
    fun ascundereTastatura() {
        if(currentFocus != null) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    fun getTag(): String {
        return tag
    }

    fun getErrTag(): String {
        return err
    }

    fun getUser(): FirebaseUser? {
        return user
    }

    fun getDB(): FirebaseFirestore {
        return db
    }

    fun getDelogare(): Boolean {
        return delogare
    }

    fun getSharedPreferencesUser(): SharedPreferences {
        return sharedPreferencesUser
    }
}