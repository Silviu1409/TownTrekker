package com.example.towntrekker.pagini.main.home_feed_recyclerview

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.towntrekker.R


class HomeFeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val iconUser: ImageView = itemView.findViewById(R.id.icon_user)
    val numeUser: AppCompatTextView = itemView.findViewById(R.id.nume_user)
    val numeLocatie: AppCompatTextView = itemView.findViewById(R.id.nume_locatie)
    val adresaLocatie: AppCompatTextView = itemView.findViewById(R.id.adresa_locatie)
    val postareApreciere: ImageButton = itemView.findViewById(R.id.postare_apreciere)
    val nrAprecieri: AppCompatTextView = itemView.findViewById(R.id.nr_aprecieri)
    val descriere: AppCompatTextView = itemView.findViewById(R.id.descriere)
    val comentarii: AppCompatTextView = itemView.findViewById(R.id.comentarii)
    val adaugaComentariuLayout: RelativeLayout = itemView.findViewById(R.id.adauga_comentariu_layout)
    val adaugaComentariu: AppCompatEditText = itemView.findViewById(R.id.adaugare_comentariu)
    val comentariuTrimite: ImageButton = itemView.findViewById(R.id.comentariu_trimite)
    val mediaCard: CardView = itemView.findViewById(R.id.media_card)
    val navStanga: ImageButton = itemView.findViewById(R.id.left_nav)
    val navDreapta: ImageButton = itemView.findViewById(R.id.right_nav)

    val media: ViewPager2 = itemView.findViewById(R.id.media)
}