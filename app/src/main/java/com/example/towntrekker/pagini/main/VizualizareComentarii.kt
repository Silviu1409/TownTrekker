package com.example.towntrekker.pagini.main

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.R
import com.example.towntrekker.pagini.main.vizualizare_comentarii_recyclerview.ComentariiAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class VizualizareComentarii: DialogFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ComentariiAdapter

    private lateinit var mainActivityContext: ActivityMain

    private lateinit var comentarii: List<Pair<String, String>>
    private lateinit var refPostare: String
    private lateinit var aliasUser: String
    private lateinit var categorieLocatie: String

    private var onDismissCallback: ((List<Pair<String, String>>) -> Unit)? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val view = layoutInflater.inflate(R.layout.vizualizare_comentarii, null, false)

        mainActivityContext = activity as ActivityMain

        val sharedPrefsComentarii = requireContext().getSharedPreferences("comentarii", Context.MODE_PRIVATE)

        comentarii = sharedPrefsComentarii?.getStringSet("comentarii", setOf())?.map { serializedPair ->
            val pairList = serializedPair.split(":")
            Pair(pairList[0], pairList[1])
        } ?: emptyList()

        refPostare = sharedPrefsComentarii?.getString("refPostare", "") ?: ""
        aliasUser = sharedPrefsComentarii?.getString("aliasUser", "") ?: ""
        categorieLocatie = sharedPrefsComentarii?.getString("categorieLocatie", "") ?: ""

        recyclerView = view.findViewById(R.id.rwcyclerview_comentarii)
        adapter = ComentariiAdapter(comentarii)
        recyclerView.adapter = adapter

        val adaugareComentariu = view.findViewById<AppCompatEditText>(R.id.adaugare_comentariu)
        val trimiteComentariu = view.findViewById<ImageButton>(R.id.comentariu_trimite)

        trimiteComentariu.setOnClickListener {
            if (adaugareComentariu.text.toString().isNotEmpty()) {
                val db = FirebaseFirestore.getInstance()
                val postareRefDB = db.collection("postari").document(refPostare)

                postareRefDB.get()
                    .addOnSuccessListener { doc ->
                        @Suppress("UNCHECKED_CAST")
                        val pairData = doc.get("comentarii") as? List<HashMap<String, String>> ?: listOf()
                        comentarii = pairData.map { hashMap -> Pair(hashMap["first"] ?: "", hashMap["second"] ?: "") }

                        val aux = comentarii.toMutableList()
                        aux.add(Pair(mainActivityContext.getUser()!!.alias, adaugareComentariu.text.toString()))

                        comentarii = aux.toList()
                        postareRefDB.update("comentarii", comentarii)
                            .addOnSuccessListener {
                                adapter.adaugaItem(Pair(mainActivityContext.getUser()!!.alias, adaugareComentariu.text.toString()))
                                adaugareComentariu.text!!.clear()

                                if (!mainActivityContext.postariInteractionateUser.containsKey(refPostare)){
                                    val dataActuala = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                                    mainActivityContext.postariInteractionateUser[refPostare] = Triple(categorieLocatie, 0, dataActuala)
                                }

                                // adaug 1 la sistemul de scor al postarilor de interes pentru fiecare comentariu
                                mainActivityContext.categoriiPostariInteresUser[categorieLocatie] = mainActivityContext.categoriiPostariInteresUser[categorieLocatie]!! + 1
                                mainActivityContext.sharedPrefsPostariInteres.edit().putInt(categorieLocatie, mainActivityContext.categoriiPostariInteresUser[categorieLocatie]!!).apply()

                                mainActivityContext.postariInteractionateUser[refPostare] = Triple(categorieLocatie, mainActivityContext.postariInteractionateUser[refPostare]!!.second + 1, mainActivityContext.postariInteractionateUser[refPostare]!!.third)

                                if (mainActivityContext.postariInteractionateUser.containsKey(refPostare)){
                                    if (mainActivityContext.postariInteractionateUser.size > mainActivityContext.getMumarMaximPostariInteractionate()) {
                                        // preiau id-ul celei mai vechi locatii
                                        val idCeaMaiVechePostare = mainActivityContext.preiaCeaMaiVechePostare()!!

                                        // iau detaliile acesteia
                                        val detaliiPostare = mainActivityContext.postariInteractionateUser[idCeaMaiVechePostare]

                                        // apoi elimin detaliile asociate acesteia si recalculez scorul
                                        mainActivityContext.postariInteractionateUser.remove(idCeaMaiVechePostare)
                                        mainActivityContext.sharedPrefsPostariInteres.edit().remove(idCeaMaiVechePostare).apply()

                                        mainActivityContext.categoriiPostariInteresUser[detaliiPostare!!.first] = mainActivityContext.categoriiPostariInteresUser[detaliiPostare.first]!! - detaliiPostare.second
                                        mainActivityContext.sharedPrefsPostariInteres.edit().putInt(detaliiPostare.first, mainActivityContext.categoriiPostariInteresUser[detaliiPostare.first]!!).apply()
                                    }

                                    val json = Gson().toJson(mainActivityContext.postariInteractionateUser[refPostare]!!.toList())
                                    mainActivityContext.sharedPrefsPostariInteres.edit().putString(refPostare, json).apply()
                                }

                                if (mainActivityContext.postariInteractionateUser.isNotEmpty()){
                                    mainActivityContext.procentPostariInteresUser = mainActivityContext.transformaCategoriiPostariInteresProcentual()
                                } else {
                                    mainActivityContext.procentPostariInteresUser = hashMapOf()
                                }

                                Log.d("testPostari", mainActivityContext.categoriiPostariInteresUser.toString())
                                Log.d("testPostari", mainActivityContext.procentPostariInteresUser.toString())
                                Log.d("testPostari", mainActivityContext.postariInteractionateUser.toString())

                                Log.d("test", "Comentariu adăugat cu succes!")
                                Toast.makeText(requireContext(), "Comentariu adăugat cu succes!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("err", "Eroare la adăugarea comentariului: ${e.message}")
                                Toast.makeText(requireContext(), "Eroare la adăugarea comentariului!", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("err", "Eroare la preluarea comentariilor: ${e.message}")
                        Toast.makeText(requireContext(), "Eroare la adăugarea comentariului!", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        builder.setView(view)

        return builder.create()
    }

    override fun onResume() {
        super.onResume()

        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.85).toInt()
        dialog!!.window!!.setLayout(width, height)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        onDismissCallback?.invoke(comentarii)
    }

    fun setOnDismissCallback(callback: (List<Pair<String, String>>) -> Unit) {
        onDismissCallback = callback
    }
}