package com.example.towntrekker.pagini.main.vizualizare_comentarii_recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.towntrekker.R


class ComentariiAdapter(private var comentarii: List<Pair<String, String>>) : RecyclerView.Adapter<ComentariiViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentariiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comentariu, parent, false)
        return ComentariiViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComentariiViewHolder, position: Int) {
        val comentariu = comentarii[position]

        holder.aliasUser.text = comentariu.first
        holder.comentariuUser.text = comentariu.second
    }

    fun adaugaItem(comentariuNou: Pair<String, String>){
        val aux = comentarii.toMutableList()
        aux.add(comentariuNou)
        comentarii = aux.toList()

        notifyItemInserted(comentarii.size - 1)
    }

    override fun getItemCount() = comentarii.size
}