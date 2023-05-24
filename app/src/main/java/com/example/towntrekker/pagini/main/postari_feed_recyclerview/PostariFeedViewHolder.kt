package com.example.towntrekker.pagini.main.postari_feed_recyclerview

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.towntrekker.R


class PostariFeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val iconUser: ImageView = itemView.findViewById(R.id.icon_user)
    val iconUserCard: CardView = itemView.findViewById(R.id.icon_user_card)
    val numeLayout: LinearLayoutCompat = itemView.findViewById(R.id.user_layout)
    val numeUser: AppCompatTextView = itemView.findViewById(R.id.nume_user)
    val urmaresteLayout: LinearLayoutCompat = itemView.findViewById(R.id.user_urmareste_layout)
    val tipRecenzieIcon: ImageView = itemView.findViewById(R.id.tip_recenzie)
    val iconLocatie: ImageView = itemView.findViewById(R.id.icon_locatie)
    val numeLocatie: AppCompatTextView = itemView.findViewById(R.id.nume_locatie)
    val adresaLocatie: AppCompatTextView = itemView.findViewById(R.id.adresa_locatie)
    val postareApreciere: ImageButton = itemView.findViewById(R.id.postare_apreciere)
    val postareApreciereLayout: LinearLayoutCompat = itemView.findViewById(R.id.postare_apreciere_layout)
    val nrAprecieri: AppCompatTextView = itemView.findViewById(R.id.nr_aprecieri)
    val descriere: AppCompatTextView = itemView.findViewById(R.id.descriere)
    val comentarii: AppCompatTextView = itemView.findViewById(R.id.comentarii)
    val mediaCard: CardView = itemView.findViewById(R.id.media_card)
    val navStanga: ImageButton = itemView.findViewById(R.id.left_nav)
    val navDreapta: ImageButton = itemView.findViewById(R.id.right_nav)

    val media: ViewPager2 = itemView.findViewById(R.id.media)
}