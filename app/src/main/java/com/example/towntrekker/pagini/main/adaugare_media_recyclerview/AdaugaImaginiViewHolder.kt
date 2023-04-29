package com.example.towntrekker.pagini.main.adaugare_media_recyclerview

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.towntrekker.R


class AdaugaImaginiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val media: ImageView = itemView.findViewById(R.id.media)
    val stergere: ImageButton = itemView.findViewById(R.id.stergere)
}