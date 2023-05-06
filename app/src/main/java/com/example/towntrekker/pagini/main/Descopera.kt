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
import com.example.towntrekker.pagini.main.descopera_recomandari_recyclerview.DescoperaRecomandariAdapter


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

        val recomandariLiveData = mainActivityContext.recomandari
        recomandariLiveData.observe(viewLifecycleOwner) { listaRecomandari ->
            adapter = DescoperaRecomandariAdapter(context, listaRecomandari)

            recyclerView.adapter = adapter
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}