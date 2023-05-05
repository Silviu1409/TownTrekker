package com.example.towntrekker.pagini.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.databinding.PaginaDescoperaBinding
import com.example.towntrekker.datatypes.Recomandare
import com.example.towntrekker.pagini.main.descopera_recomandari_recyclerview.DescoperaRecomandariAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class Descopera : Fragment() {
    private var _binding: PaginaDescoperaBinding? = null

    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DescoperaRecomandariAdapter

    private lateinit var mainActivityContext: ActivityMain


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = PaginaDescoperaBinding.inflate(inflater, container, false)

        mainActivityContext = context as ActivityMain

        recyclerView = binding.descoperaRecomandari
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = DescoperaRecomandariAdapter(context, arrayListOf())
        recyclerView.adapter = adapter

        CoroutineScope(Dispatchers.Main).launch {
            val listaRecomandari = preluareRecomandari()

            adapter = DescoperaRecomandariAdapter(context, listaRecomandari)

            recyclerView.adapter = adapter
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun preluareRecomandari(): List<Recomandare> {
        val snapshot = mainActivityContext.getDB().collection("recomandari").get().await()
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