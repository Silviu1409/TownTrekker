package com.example.towntrekker.pagini.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.databinding.PaginaPostariBinding
import com.example.towntrekker.datatypes.Postare
import com.example.towntrekker.pagini.main.postari_feed_recyclerview.PostariFeedAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class Postari : Fragment() {
    private var _binding: PaginaPostariBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostariFeedAdapter

    private lateinit var mainActivityContext: ActivityMain


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = PaginaPostariBinding.inflate(inflater, container, false)

        mainActivityContext = context as ActivityMain

        recyclerView = binding.postariFeed
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = PostariFeedAdapter(context, arrayListOf())
        recyclerView.adapter = adapter

        CoroutineScope(Dispatchers.Main).launch {
            val listaPostari = preluarePostari()

            adapter = PostariFeedAdapter(context, listaPostari)

            if  (adapter.itemCount == 0) {
                binding.postariNuExistaPostariFeed.visibility = View.VISIBLE
            } else {
                binding.postariFeed.visibility = View.VISIBLE
            }

            recyclerView.adapter = adapter
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun preluarePostari(): List<Postare> {
        val snapshot = mainActivityContext.getDB().collection("postari").get().await()
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
}