package com.example.towntrekker.pagini.main

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.R
import com.example.towntrekker.pagini.main.vizualizare_urmareste_urmaritori_recyclerview.VizualizareUrmaresteUrmaritoriAdapter


class VizualizareUrmaritori: DialogFragment() {
    private lateinit var recyclerView: RecyclerView
    lateinit var adapter: VizualizareUrmaresteUrmaritoriAdapter

    private lateinit var refUser: String

    private lateinit var mainActivityContext: ActivityMain


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val view = layoutInflater.inflate(R.layout.vizualizare_urmareste_urmaritori, null, false)

        val sharedPreferences = requireContext().getSharedPreferences("vizUser", Context.MODE_PRIVATE)
        refUser = sharedPreferences?.getString("refUser", "") ?: ""

        mainActivityContext = requireActivity() as ActivityMain

        recyclerView = view.findViewById(R.id.recyclerview_urmareste_urmaritori)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = VizualizareUrmaresteUrmaritoriAdapter(context, listOf())
        recyclerView.adapter = adapter

        if (refUser == mainActivityContext.getUser()!!.uid){
            adapter = VizualizareUrmaresteUrmaritoriAdapter(context, mainActivityContext.getUser()!!.urmaritori)
            recyclerView.adapter = adapter
        }
        else {
            mainActivityContext.getDB().collection("useri").document(refUser).get()
                .addOnSuccessListener { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val listaUrmaritori = doc.get("urmaritori") as? List<String> ?: listOf()
                    adapter = VizualizareUrmaresteUrmaritoriAdapter(context, listaUrmaritori)
                    recyclerView.adapter = adapter
                }
        }

        builder.setView(view)

        return builder.create()
    }

    override fun onResume() {
        super.onResume()

        val width = (resources.displayMetrics.widthPixels * 0.7).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.75).toInt()
        dialog!!.window!!.setLayout(width, height)
    }
}