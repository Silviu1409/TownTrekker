package com.example.towntrekker.pagini.main

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.R
import com.example.towntrekker.pagini.main.postari_feed_recyclerview.PostariFeedAdapter


class VizualizarePostariUser: DialogFragment() {
    private lateinit var recyclerView: RecyclerView
    lateinit var adapter: PostariFeedAdapter

    private lateinit var refUser: String

    private lateinit var mainActivityContext: ActivityMain


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val view = layoutInflater.inflate(R.layout.vizualizare_postari_user, null, false)

        val sharedPreferences = requireContext().getSharedPreferences("vizUser", Context.MODE_PRIVATE)
        refUser = sharedPreferences?.getString("refUser", "") ?: ""

        mainActivityContext = requireActivity() as ActivityMain

        recyclerView = view.findViewById(R.id.recyclerview_postari_user)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PostariFeedAdapter(context, arrayListOf())
        recyclerView.adapter = adapter

        builder.setView(view)

        return builder.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.vizualizare_postari_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postariLiveData = mainActivityContext.postari
        postariLiveData.observe(viewLifecycleOwner) { listaPostari ->
            val postariUser = listaPostari.filter { it.user == refUser }

            adapter = PostariFeedAdapter(context, postariUser)

            recyclerView.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()

        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.95).toInt()
        dialog!!.window!!.setLayout(width, height)
    }
}