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
import com.example.towntrekker.pagini.main.postari_feed_recyclerview.PostariFeedAdapter


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

        val postariLiveData = mainActivityContext.postari
        postariLiveData.observe(viewLifecycleOwner) { listaPostari ->
            val listaPostariFiltrate = listaPostari.filter { it.user != mainActivityContext.getUser()!!.uid }

            adapter = PostariFeedAdapter(context, listaPostariFiltrate)

            if (adapter.itemCount == 0) {
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
}