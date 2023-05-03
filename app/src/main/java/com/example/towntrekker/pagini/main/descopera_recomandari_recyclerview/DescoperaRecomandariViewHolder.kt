package com.example.towntrekker.pagini.main.descopera_recomandari_recyclerview

import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.towntrekker.R
import com.google.android.gms.maps.MapView


class DescoperaRecomandariViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val logoLocatieCard: CardView = itemView.findViewById(R.id.logo_locatie_card)
    val logoLocatie: ImageView = itemView.findViewById(R.id.logo_locatie)
    val iconNumeLocatie : ImageView = itemView.findViewById(R.id.icon_nume_locatie)
    val numeLocatie: AppCompatTextView = itemView.findViewById(R.id.nume_locatie)
    val adresaLocatie: AppCompatTextView = itemView.findViewById(R.id.adresa_locatie)
    val descriereLocatie: AppCompatTextView = itemView.findViewById(R.id.descriere_locatie)
    val hartaLocatie: MapView = itemView.findViewById(R.id.harta_locatie)
}