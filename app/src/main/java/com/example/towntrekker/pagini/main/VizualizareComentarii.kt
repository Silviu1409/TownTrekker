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
import com.example.towntrekker.R
import com.example.towntrekker.pagini.main.vizualizare_comentarii_recyclerview.ComentariiAdapter
import com.google.firebase.firestore.FirebaseFirestore


class VizualizareComentarii: DialogFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ComentariiAdapter

    private lateinit var comentarii: List<Pair<String, String>>
    private lateinit var refPostare: String
    private lateinit var aliasUser: String

    private var onDismissCallback: ((List<Pair<String, String>>) -> Unit)? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sharedPreferences = requireContext().getSharedPreferences("comentarii", Context.MODE_PRIVATE)

        comentarii = sharedPreferences?.getStringSet("comentarii", setOf())?.map { serializedPair ->
            val pairList = serializedPair.split(":")
            Pair(pairList[0], pairList[1])
        } ?: emptyList()

        refPostare = sharedPreferences?.getString("refPostare", "") ?: ""
        aliasUser = sharedPreferences?.getString("aliasUser", "") ?: ""

        val builder = AlertDialog.Builder(requireContext())

        val view = layoutInflater.inflate(R.layout.vizualizare_comentarii, null, false)

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
                        aux.add(Pair(aliasUser, adaugareComentariu.text.toString()))

                        comentarii = aux.toList()
                        postareRefDB.update("comentarii", comentarii)
                            .addOnSuccessListener {
                                adapter.adaugaItem(Pair(aliasUser, adaugareComentariu.text.toString()))
                                adaugareComentariu.text!!.clear()

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