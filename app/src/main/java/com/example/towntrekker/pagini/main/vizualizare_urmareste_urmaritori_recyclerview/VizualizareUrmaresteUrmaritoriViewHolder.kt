package com.example.towntrekker.pagini.main.vizualizare_urmareste_urmaritori_recyclerview

import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.towntrekker.R


class VizualizareUrmaresteUrmaritoriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val iconUser: ImageView = itemView.findViewById(R.id.icon_user)
    val numeUser: AppCompatTextView = itemView.findViewById(R.id.nume_user)
    val cardUser: CardView = itemView.findViewById(R.id.card_user)
}