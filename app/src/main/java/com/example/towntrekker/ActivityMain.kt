package com.example.towntrekker

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.towntrekker.databinding.ActivityMainBinding
import com.example.towntrekker.datatypes.Postare
import com.example.towntrekker.datatypes.Recomandare
import com.example.towntrekker.datatypes.User
import com.example.towntrekker.pagini.main.AdaugaPostare
import com.google.android.libraries.places.api.Places
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.Locale


class ActivityMain : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val tag = "test"    // tag folosit pentru testare in cazurile de debug/warning
    private val err = "err"     // tag folosit pentru testare pentru erori

    private var user: User? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var folderRef: StorageReference
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: StorageReference

    private lateinit var sharedPrefsUser: SharedPreferences   // fișierul din shared preferences ce conține date despre user

    private lateinit var userIconFile: File

    private var sharedPrefsLiked: SharedPreferences? = null
    private var postariApreciate = mutableSetOf<String>()

    var postari = MutableLiveData<List<Postare>>()
    var recomandari = MutableLiveData<List<Recomandare>>()


    // suprascriere a metodei ce este apelată în momentul în care utilizatorul apasă pe butonul "back" din bara de navigație
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            exitAplicatie()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = Firebase.storage.reference

        sharedPrefsUser = getSharedPreferences("user", Context.MODE_PRIVATE)

        if (sharedPrefsUser.contains("uid")) {
            user = User(uid = sharedPrefsUser.getString("uid", "")!!,
                email = sharedPrefsUser.getString("email", "")!!,
                alias = sharedPrefsUser.getString("alias", "")!!,
                urmareste = sharedPrefsUser.getStringSet("urmareste", setOf())!!.toList(),
                bio = sharedPrefsUser.getString("bio", "")!!,
                parola = sharedPrefsUser.getString("parola", "")!!)

            val postareRefDB = db.collection("useri").document(user!!.uid)
            postareRefDB.get()
                .addOnSuccessListener { doc ->
                    @Suppress("UNCHECKED_CAST")
                    user!!.urmaritori = doc.get("urmaritori") as? List<String> ?: listOf()
                }
                .addOnFailureListener { e ->
                    Log.e(err, "Eroare la preluare urmăritori: ${e.message}")
                }
        }
        else {
            @Suppress("DEPRECATION")
            user = intent.getSerializableExtra("user") as? User     // preiau datele despre utilizator

            sharedPrefsUser.edit().putString("uid", user!!.uid).apply()
            sharedPrefsUser.edit().putString("email", user!!.email).apply()
            sharedPrefsUser.edit().putString("alias", user!!.alias).apply()
            sharedPrefsUser.edit().putStringSet("urmareste", user!!.urmareste.toSet()).apply()

            if (user!!.bio.isNotEmpty()) {
                sharedPrefsUser.edit().putString("bio", user!!.bio).apply()
            }

            if (user!!.parola.isNotEmpty()) {
                sharedPrefsUser.edit().putString("parola", user!!.parola).apply()
            }
        }

        folderRef = Firebase.storage.reference.child("useri").child(user!!.uid)    // referință folder Firebase Storage creat pt. fiecare user

        // configurare navigare pentru secțiunea principală din cadrul aplicației
        val navView: BottomNavigationView = binding.navMeniu

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_postari, R.id.nav_exploreaza, R.id.nav_descopera, R.id.nav_cont
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        supportActionBar?.hide()    //ascundere ActionBar
        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)

        @Suppress("DEPRECATION")
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext,
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData.getString("com.google.android.geo.API_KEY")!!,
                Locale("ro", "RO")
            )
        }
        Places.createClient(this)

        // preluare poză profil user, dacă nu există deja în cache
        userIconFile = File(applicationContext.cacheDir, "icon.jpg")

        val imageRef = folderRef.child("icon.jpg")

        imageRef.metadata
            .addOnSuccessListener {  metadata ->

                if(userIconFile.exists() && userIconFile.length() == metadata.sizeBytes){
                    Log.d(tag, "Poza de profil este deja în cache!")
                } else {
                    Log.w(tag, "Poza de profil nu este în cache")

                    imageRef.getFile(userIconFile)
                        .addOnSuccessListener {
                            Log.d(tag, "Poza de profil salvată în cache!!")
                        }
                        .addOnFailureListener { e ->
                            Log.e(err, "Salvarea pozei de profil în cache a eșuat: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                if (e.message == "Object does not exist at location."){
                    Log.e(err, "User-ul nu are un icon setat: ${e.message}")
                }
                else {
                    Log.e(err, "Preluarea datelor despre icon a eșuat: ${e.message}")
                }
            }

        // preluare/creare fișier ce conține postările apreciate
        sharedPrefsLiked = getSharedPreferences("apreciat${user!!.uid}", Context.MODE_PRIVATE)

        if (sharedPrefsLiked!!.contains("postari")){
            postariApreciate = sharedPrefsLiked!!.getStringSet("postari", mutableSetOf()) ?: mutableSetOf()
            sharedPrefsLiked = null
        }

        CoroutineScope(Dispatchers.Main).launch {
            postari.value = preluarePostari()
            recomandari.value = preluareRecomandari()
        }
    }

    // funcție ce afișează un AlertDialog, în care utilizatorul este întrebat dacă dorește să închidă aplicația
    private fun exitAplicatie() {
        MaterialAlertDialogBuilder(this)
            .setMessage("Vrei să închizi aplicația?")
            // finishAffinity închide activitățile începute anterior (în acest caz ActivityAuth și ActivityMain)
            .setPositiveButton("Da") { _, _ -> finishAffinity() }
            .setNegativeButton("Nu", null)
            .show()
    }

    @Suppress("UNUSED_PARAMETER")
    fun adaugaPostare(view: View) {
        val dialog = AdaugaPostare()
        dialog.show(supportFragmentManager, "Adaugă postare")
    }

    fun getUser(): User? {
        return user
    }

    fun getTag(): String {
        return tag
    }

    fun getErrTag(): String {
        return err
    }

    fun getFolderRef(): StorageReference {
        return folderRef
    }

    fun getDB(): FirebaseFirestore {
        return db
    }

    fun getAuth(): FirebaseAuth {
        return auth
    }

    fun getSharedPrefsUser(): SharedPreferences {
        return sharedPrefsUser
    }

    fun getStorage(): StorageReference {
        return storage
    }

    fun getUserIconFile(): File {
        return userIconFile
    }

    fun deleteUserIconFile() {
        userIconFile.delete()
    }

    // funcție pentru ascunderea tastaturii
    fun ascundereTastatura() {
        if(currentFocus != null) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    // funcție ce șterge datele legate de Shared Preferences pentru utilizatorul curent
    fun stergeSharedPrefsUser(){
        sharedPrefsUser.edit().clear().apply()
    }

    fun esteInPostariApreciate(idPostare: String): Boolean {
        return idPostare in postariApreciate
    }

    fun editPostariApreciate(idPostare: String, actiune: String) {
        sharedPrefsLiked = getSharedPreferences("apreciat${user!!.uid}", Context.MODE_PRIVATE)
        val postareRefDB = db.collection("postari").document(idPostare)

        if (actiune == "adaugare"){
            postariApreciate.add(idPostare)

            postareRefDB.get()
                .addOnSuccessListener { doc ->
                    val aprecieri = doc.getDouble("aprecieri")!!.toInt() + 1

                    postareRefDB.update("aprecieri", aprecieri)
                        .addOnSuccessListener {
                            Log.d(tag, "Postare apreciată cu succes!")
                            Toast.makeText(this, "Postare apreciată cu succes!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e(err, "Eroare la aprecierea postării: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(err, "Eroare la aprecierea postării: ${e.message}")
                }
        }
        else if (actiune == "stergere"){
            postariApreciate.remove(idPostare)

            postareRefDB.get()
                .addOnSuccessListener { doc ->
                    val aprecieri = doc.getDouble("aprecieri")!!.toInt() - 1

                    postareRefDB.update("aprecieri", aprecieri)
                        .addOnSuccessListener {
                            Log.d(tag, "Eliminare apreciere postare făcută cu succes!")
                            Toast.makeText(this, "Eliminare apreciere postare făcută cu succes!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e(err, "Eroare la eliminarea aprecierii postării: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(err, "Eroare la eliminarea aprecierii postării: ${e.message}")
                }
        }

        sharedPrefsLiked!!.edit().remove("postari").apply()
        sharedPrefsLiked!!.edit().putStringSet("postari", postariApreciate.toSet()).apply()
        sharedPrefsLiked = null
    }

    private suspend fun preluarePostari(): List<Postare> {
        val snapshot = db.collection("postari").get().await()
        val listaPostari = mutableListOf<Postare>()

        snapshot.documents.map { doc ->
            @Suppress("UNCHECKED_CAST")
            val pairData = doc.get("comentarii") as? List<HashMap<String, String>> ?: listOf()
            val pairs = pairData.map { hashMap -> Pair(hashMap["first"] ?: "", hashMap["second"] ?: "") }

            val postare = Postare(doc.id,
                doc.getString("user") ?: "",
                doc.getString("numeUser") ?: "",
                doc.getString("numeLocatie") ?: "",
                doc.getString("adresaLocatie") ?: "",
                doc.getString("tipLocatie")?.lowercase() ?: "",
                doc.getString("categorieLocatie")?.lowercase() ?: "",
                (doc.getDouble("aprecieri") ?: 0).toInt(),
                doc.getString("descriere") ?: "",
                pairs,
                doc.getBoolean("media") ?: false,
                doc.getBoolean("iconUser") ?: false)

            listaPostari.add(postare)
        }

        return listaPostari
    }

    private suspend fun preluareRecomandari(): List<Recomandare> {
        val snapshot = db.collection("recomandari").get().await()
        val listaRecomandari = mutableListOf<Recomandare>()

        snapshot.documents.map { doc ->
            val recomandare = Recomandare(doc.id,
                doc.getString("logo") ?: "",
                doc.getString("nume") ?: "",
                doc.getString("adresa") ?: "",
                doc.getString("rating") ?: "",
                doc.getString("tip")?.lowercase() ?: "",
                doc.getString("categorie")?.lowercase() ?: "",
                doc.getString("descriere") ?: "",
                doc.getString("geolocatie") ?: "")

            listaRecomandari.add(recomandare)
        }

        return listaRecomandari
    }
}