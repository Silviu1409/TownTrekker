package com.example.towntrekker.pagini.main.vizualizare_comentarii_recyclerview

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.towntrekker.R


class ComentariiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val aliasUser: AppCompatTextView = itemView.findViewById(R.id.alias_user)
    val comentariuUser: AppCompatTextView = itemView.findViewById(R.id.comentariu)
}