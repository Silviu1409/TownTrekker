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
import com.github.pemistahl.lingua.api.Language
import com.github.pemistahl.lingua.api.LanguageDetector
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder
import com.google.android.libraries.places.api.Places
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.LinkedList
import java.util.Locale
import java.util.Queue
import kotlin.math.roundToInt


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

    private lateinit var sharedPrefsLiked: SharedPreferences
    private var postariApreciate = mutableSetOf<String>()

    var postari = MutableLiveData<List<Postare>>()
    var recomandari = MutableLiveData<List<Recomandare>>()

    lateinit var sharedPrefsPostariInteres: SharedPreferences
    lateinit var categoriiPostariInteresUser: HashMap<String, Int>

    private val numarMaximPostariInteractionate = 10
    lateinit var postariInteractionateUser: HashMap<String, Triple<String, Int, String>>
    var procentPostariInteresUser: MutableMap<String, Double> = mutableMapOf()

    private var postariUrmareste = listOf<Postare>()
    var categoriiPostariUrmareste: HashMap<String, Double> = hashMapOf()    // lista cu categoriile de postari ale urmaritorilor

    var utilizatoriSferaInteres = MutableLiveData<MutableSet<String>>()
    private var postariUtilizatoriSferaInteres = listOf<Postare>()

    private val detectorLimba = LanguageDetectorBuilder
        .fromLanguages(Language.ENGLISH, Language.SPANISH, Language.RUSSIAN, Language.GERMAN, Language.FRENCH,
            Language.JAPANESE, Language.PORTUGUESE, Language.TURKISH, Language.ITALIAN, Language.ROMANIAN)
        .build()


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

                if (userIconFile.exists() && userIconFile.length() == metadata.sizeBytes){
                    Log.d(tag, "Poza de profil este deja în cache!")
                }
                else {
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
        sharedPrefsPostariInteres = getSharedPreferences("postariinteres${user!!.uid}", Context.MODE_PRIVATE)

        if (sharedPrefsLiked.contains("postari")){
            postariApreciate = sharedPrefsLiked.getStringSet("postari", mutableSetOf()) ?: mutableSetOf()
        }

        categoriiPostariInteresUser = hashMapOf("food and drink" to 0, "retail" to 0, "services" to 0, "entertainment" to 0, "outdoor" to 0, "other" to 0)
        postariInteractionateUser = hashMapOf()
        procentPostariInteresUser = hashMapOf()

        if (sharedPrefsPostariInteres.contains("food and drink")){
            categoriiPostariInteresUser = hashMapOf(
                "food and drink" to sharedPrefsPostariInteres.getInt("food and drink", 0),
                "retail" to sharedPrefsPostariInteres.getInt("retail", 0),
                "services" to sharedPrefsPostariInteres.getInt("services", 0),
                "entertainment" to sharedPrefsPostariInteres.getInt("entertainment", 0),
                "outdoor" to sharedPrefsPostariInteres.getInt("outdoor", 0),
                "other" to sharedPrefsPostariInteres.getInt("other", 0)
            )

            for (id in sharedPrefsPostariInteres.all.keys) {
                if (id !in listOf("food and drink", "retail", "services", "entertainment", "outdoor", "other")) {
                    val json = sharedPrefsPostariInteres.getString(id, "")
                    val type = object : TypeToken<List<String>>() {}.type
                    val valori = Gson().fromJson(json, type) ?: listOf<String>()

                    postariInteractionateUser[id] = Triple(valori[0], valori[1].toInt(), valori[2])
                }

                if(postariInteractionateUser.isNotEmpty()){
                    procentPostariInteresUser = transformaCategoriiPostariInteresProcentual()
                }
            }
        } else {
            sharedPrefsPostariInteres.edit().putInt("food and drink", 0).apply()
            sharedPrefsPostariInteres.edit().putInt("retail", 0).apply()
            sharedPrefsPostariInteres.edit().putInt("services", 0).apply()
            sharedPrefsPostariInteres.edit().putInt("entertainment", 0).apply()
            sharedPrefsPostariInteres.edit().putInt("outdoor", 0).apply()
            sharedPrefsPostariInteres.edit().putInt("other", 0).apply()
        }

        Log.d("testCategoriiInteres", categoriiPostariInteresUser.toString())
        Log.d("testPonderiCategoriiInteres", procentPostariInteresUser.toString())
        Log.d("testCategoriiInteractionate", postariInteractionateUser.toString())

        CoroutineScope(Dispatchers.Main).launch {
            postari.value = preluarePostari()
            recomandari.value = preluareRecomandari()
        }

        postari.observe(this) {
            preiaPostarileUrmaritorilor()
            preiaPostariUtilizatoriSferaInteres()
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

    fun getDetectorLimba(): LanguageDetector {
        return detectorLimba
    }

    fun deleteUserIconFile() {
        userIconFile.delete()
    }

    fun getMumarMaximPostariInteractionate(): Int {
        return numarMaximPostariInteractionate
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

        sharedPrefsLiked.edit().remove("postari").apply()
        sharedPrefsLiked.edit().putStringSet("postari", postariApreciate.toSet()).apply()
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
                doc.getBoolean("iconUser") ?: false,
                doc.getString("tipRecenzie") ?: "nedefinit",
                doc.getDouble("scorRecenzie") ?: (-1).toDouble())

            listaPostari.add(postare)
        }

        return listaPostari
    }

    private suspend fun preluareRecomandari(): List<Recomandare> {
        val sharedPrefsRecomandari = getSharedPreferences("recomandari", Context.MODE_PRIVATE)
        val dataModificare = sharedPrefsRecomandari.getString("dataModificare", "") ?: ""

        val listaRecomandari = mutableListOf<Recomandare>()

        var diferentaZile = 0

        val dataCurenta = LocalDate.now()
        val formatData = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        if (dataModificare.isNotEmpty()) {
            val dataDocument = LocalDate.parse(dataModificare, formatData)

            diferentaZile = Period.between(dataDocument, dataCurenta).days
        }

        Log.i(tag, "$diferentaZile zi/zile diferență")

        if (sharedPrefsRecomandari.all.keys.size < 2 || diferentaZile > 0) {
            listaRecomandari.clear()

            val snapshot = db.collection("recomandari").get().await()

            snapshot.documents.map { doc ->
                val recomandare = Recomandare(
                    doc.id,
                    doc.getString("logo") ?: "",
                    doc.getString("nume") ?: "",
                    doc.getString("adresa") ?: "",
                    doc.getString("rating") ?: "",
                    doc.getString("tip")?.lowercase() ?: "",
                    doc.getString("categorie")?.lowercase() ?: "",
                    doc.getString("descriere") ?: "",
                    doc.getString("geolocatie") ?: ""
                )

                listaRecomandari.add(recomandare)

                val campuri = listOf(
                    recomandare.logo,
                    recomandare.nume,
                    recomandare.adresa,
                    recomandare.rating,
                    recomandare.tip,
                    recomandare.categorie,
                    recomandare.descriere,
                    recomandare.geoLocatie
                )

                val json = Gson().toJson(campuri)
                sharedPrefsRecomandari.edit().putString(recomandare.id, json).apply()
            }

            sharedPrefsRecomandari.edit().putString("dataModificare", dataCurenta.format(formatData)).apply()
        }
        else {
            for (id in sharedPrefsRecomandari.all.keys) {
                if (id != "dataModificare") {
                    val json = sharedPrefsRecomandari.getString(id, "")
                    val type = object : TypeToken<List<String>>() {}.type
                    val valori = Gson().fromJson(json, type) ?: listOf<String>()

                    listaRecomandari.add(
                        Recomandare(
                            id,
                            valori[0],
                            valori[1],
                            valori[2],
                            valori[3],
                            valori[4],
                            valori[5],
                            valori[6],
                            valori[7]
                        )
                    )
                }
            }
        }

        val categorii = listaRecomandari.groupBy { it.categorie }.mapValues { it.value.size }
        val mesaj = StringBuilder("Total recomandări: ${listaRecomandari.size}")

        for ((categorie, numar) in categorii){
            mesaj.append("; $categorie: $numar")
        }

        Log.i(tag, mesaj.toString())

        return listaRecomandari
    }

    fun preiaCeaMaiVechePostare(): String? {
        var ceaMaiVecheData: LocalDateTime? = null
        var ceaMaiVecheCheie: String? = null

        for ((cheie, valoare) in postariInteractionateUser){
            val dataPostareCurenta = LocalDateTime.parse(valoare.third, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            if (ceaMaiVecheData == null || dataPostareCurenta.isBefore(ceaMaiVecheData)) {
                ceaMaiVecheData = dataPostareCurenta
                ceaMaiVecheCheie = cheie
            }
        }

        return ceaMaiVecheCheie
    }

    fun transformaCategoriiPostariInteresProcentual(): HashMap<String, Double> {
        val scoruriProcentuale = hashMapOf<String, Double>()
        val scorTotalPostari = categoriiPostariInteresUser.values.sum()

        // exprim scorurile din categorii procentual
        for ((categorie, scor) in  categoriiPostariInteresUser) {
            if (scor != 0) {
                val procent = scor / scorTotalPostari.toDouble()
                scoruriProcentuale[categorie] = procent
            }
        }

        return scoruriProcentuale
    }

    // funcție care va returna procentual categoriile de interes ale userilor pe care îi urmăresc
    private fun preiaCategoriiUrmareste(): HashMap<String, Double> {
        // fac un Map de tip cheie-valoare unde cheia este categoria si valoarea este numarul de postari din acea categorie
        val categoriiDistincte = postariUrmareste.groupBy { it.categorieLocatie }.mapValues { it.value.size }

        // apoi, vreau sa transform din cifre în valori procentuale
        val totalPostariUrmaritori = postariUrmareste.size

        val categoriiDistinceProcentual = hashMapOf<String, Double>()

        categoriiDistincte.forEach { (categorie, numar) ->
            val procent = (numar.toDouble() * 100.0).roundToInt() / (totalPostariUrmaritori * 100.0)
            categoriiDistinceProcentual[categorie] = procent
        }

        return categoriiDistinceProcentual
    }

    fun preiaPostarileUrmaritorilor() {
        postariUrmareste = postari.value!!.filter { it.user in user!!.urmareste }

        categoriiPostariUrmareste = preiaCategoriiUrmareste()

        Log.d("testRecomandariUrmareste", categoriiPostariUrmareste.toString())
    }

    fun preiaPostariUtilizatoriSferaInteres(adancimeMaxima: Int = 2) {
        val q: Queue<Pair<String, Int>> = LinkedList()
        q.add(Pair(user!!.uid, 0))
        val useriAdaugatiNoi = mutableSetOf<String>()

        while (q.isNotEmpty()) {
            val (utilizatorCurent, adancimeCurenta) = q.poll()!!

            if (adancimeCurenta > adancimeMaxima) {
                break
            }

            if (utilizatorCurent in useriAdaugatiNoi) {
                continue
            }

            useriAdaugatiNoi.add(utilizatorCurent)

            if (adancimeCurenta < adancimeMaxima) {
                val utilizatoriUrmariti = runBlocking { preiaUtilizatoriUrmariti(utilizatorCurent) }
                Log.d("testUtUrm", "$utilizatorCurent, $adancimeCurenta")
                Log.d("testUtUrm", utilizatoriUrmariti.toString())

                for (utilizatorUrmarit in utilizatoriUrmariti) {
                    q.add(Pair(utilizatorUrmarit, adancimeCurenta + 1))
                }
            }
        }

        useriAdaugatiNoi.remove(user!!.uid)

        utilizatoriSferaInteres.value = useriAdaugatiNoi
        preiaPostariUtilizatori(useriAdaugatiNoi)

        Log.d("testPostariPersonalizate", utilizatoriSferaInteres.value.toString())
        Log.d("testPostariPersonalizate", postariUtilizatoriSferaInteres.size.toString())
    }

    private suspend fun preiaUtilizatoriUrmariti(utilizator: String): List<String> = withContext(Dispatchers.IO) {
        if (utilizator == user!!.uid){
            return@withContext user!!.urmareste
        }

        val refDocUtilizator = db.collection("useri").document(utilizator)

        try {
            val docSnapshot = refDocUtilizator.get().await()

            @Suppress("UNCHECKED_CAST")
            return@withContext docSnapshot["urmareste"] as? List<String> ?: listOf<String>()
        }
        catch (e: Exception) {
            return@withContext emptyList()
        }
    }

    private fun preiaPostariUtilizatori(listaUtilizatori: MutableSet<String>) {
        postariUtilizatoriSferaInteres = postari.value!!.filter { it.user in listaUtilizatori }
    }
}