package com.example.towntrekker.pagini.main.postari_feed_recyclerview

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.R
import com.example.towntrekker.datatypes.Postare
import com.example.towntrekker.pagini.main.VizualizareComentarii
import com.example.towntrekker.pagini.main.VizualizareDetaliiUser
import com.example.towntrekker.pagini.main.postare_media_recyclerview.MediaAdapter


class PostariFeedAdapter(context: Context?, private val lista_postari: List<Postare>) : RecyclerView.Adapter<PostariFeedViewHolder>(){

    private val mainActivityContext = (context as ActivityMain)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostariFeedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.postari_feed_postare, parent, false)
        return PostariFeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostariFeedViewHolder, position: Int) {
        val postare = lista_postari[position]
        val postareRef = mainActivityContext.getStorage().child("postari").child(postare.id)

        if (postare.user != mainActivityContext.getUser()!!.uid) {
            holder.iconUserCard.visibility = View.VISIBLE
            holder.numeLayout.visibility = View.VISIBLE

            if (postare.user in mainActivityContext.getUser()!!.urmareste) {
                holder.urmaresteLayout.visibility = View.VISIBLE
            }

            if (postare.iconUser) {
                val userIconRef = postareRef.child("icon.jpg")

                Glide.with(mainActivityContext)
                    .load(userIconRef)
                    .override(35, 35)
                    .centerCrop()
                    .into(holder.iconUser)
            }

            holder.iconUserCard.setOnClickListener {
                val docVizUser = mainActivityContext.getSharedPreferences("vizUser", Context.MODE_PRIVATE)

                docVizUser.edit().putString("refUser", postare.user).apply()

                val dialog = VizualizareDetaliiUser()
                dialog.show(mainActivityContext.supportFragmentManager, "Vizualizează detalii user")
            }
        }

        when(postare.tipLocatie.lowercase()){
            "bar", "night club" -> holder.iconLocatie.setImageResource(R.drawable.icon_bar)
            "cafe" -> holder.iconLocatie.setImageResource(R.drawable.icon_cafe)
            "restaurant" -> holder.iconLocatie.setImageResource(R.drawable.icon_restaurant)
            "bakery" -> holder.iconLocatie.setImageResource(R.drawable.icon_bakery)

            "books", "clothing", "electronics", "jewelry",
            "shoes", "shopping center/mall" -> holder.iconLocatie.setImageResource(R.drawable.icon_mall)
            "convenience store" -> holder.iconLocatie.setImageResource(R.drawable.icon_store)
            "grocery", "supermarket" -> holder.iconLocatie.setImageResource(R.drawable.icon_supermarket)
            "pharmacy" -> holder.iconLocatie.setImageResource(R.drawable.icon_pharmacy)

            "lodging" -> holder.iconLocatie.setImageResource(R.drawable.icon_lodging)

            "golf" -> holder.iconLocatie.setImageResource(R.drawable.icon_golf)
            "historic" -> holder.iconLocatie.setImageResource(R.drawable.icon_historic)
            "movie" -> holder.iconLocatie.setImageResource(R.drawable.icon_movie)
            "museum" -> holder.iconLocatie.setImageResource(R.drawable.icon_museum)
            "theater" -> holder.iconLocatie.setImageResource(R.drawable.icon_theater)

            "boating" -> holder.iconLocatie.setImageResource(R.drawable.icon_boating)
            "camping" -> holder.iconLocatie.setImageResource(R.drawable.icon_camping)
            "park" -> holder.iconLocatie.setImageResource(R.drawable.icon_park)
            "stadium" -> holder.iconLocatie.setImageResource(R.drawable.icon_stadium)
            "zoo" -> holder.iconLocatie.setImageResource(R.drawable.icon_zoo)
        }

        holder.numeUser.text = postare.numeUser
        holder.numeLocatie.text = postare.numeLocatie
        holder.adresaLocatie.text = postare.adresaLocatie

        if (postare.user != mainActivityContext.getUser()!!.uid) {
            holder.postareApreciereLayout.visibility = View.VISIBLE

            if (mainActivityContext.esteInPostariApreciate(postare.id)) {
                holder.postareApreciere.setImageResource(R.drawable.icon_apreciere_plin)
            } else {
                holder.postareApreciere.setImageResource(R.drawable.icon_apreciere_gol)
            }

            holder.postareApreciere.setOnClickListener {
                if (mainActivityContext.esteInPostariApreciate(postare.id)) {
                    holder.postareApreciere.setImageResource(R.drawable.icon_apreciere_gol)
                    postare.aprecieri -= 1
                    holder.nrAprecieri.text = numarAprecieriTransform(postare.aprecieri)

                    mainActivityContext.editPostariApreciate(postare.id, "stergere")
                } else {
                    holder.postareApreciere.setImageResource(R.drawable.icon_apreciere_plin)
                    postare.aprecieri += 1
                    holder.nrAprecieri.text = numarAprecieriTransform(postare.aprecieri)

                    mainActivityContext.editPostariApreciate(postare.id, "adaugare")
                }
            }

            holder.nrAprecieri.text = numarAprecieriTransform(postare.aprecieri)

        }

        if (postare.descriere.isNotEmpty()){
            holder.descriere.visibility = View.VISIBLE
            holder.descriere.text = mainActivityContext.getString(R.string.postare_descriere_tab).plus(postare.descriere)
        }

        if (postare.media){
            postareRef.listAll()
                .addOnSuccessListener { res ->
                    // exclud poza de profil a user-ului care a făcut postarea
                    postare.lista_media = res.items.filter { it.name != "icon.jpg" }

                    Log.d(mainActivityContext.getTag(), "S-au preluat cu succes fișierele media.")

                    holder.media.adapter = MediaAdapter(mainActivityContext, postare.lista_media)
                    holder.mediaCard.visibility = View.VISIBLE
                }
                .addOnFailureListener { e ->
                    Log.e(mainActivityContext.getErrTag(), "Eroare la preluarea fișierelor media:${e.message}")
                }

            holder.navStanga.setOnClickListener {
                var tab = holder.media.currentItem

                if (tab > 0){
                    tab -= 1
                    holder.media.currentItem = tab
                }
            }

            holder.navDreapta.setOnClickListener {
                var tab = holder.media.currentItem

                if (tab < holder.media.adapter!!.itemCount - 1){
                    tab += 1
                    holder.media.currentItem = tab
                }
            }
        } else {
            holder.mediaCard.visibility = View.GONE
        }

        holder.comentarii.setOnClickListener {
            val docComentarii = mainActivityContext.getSharedPreferences("comentarii", Context.MODE_PRIVATE)

            val setPerechiComentarii = postare.comentarii.map { (key, value) -> "$key:$value" }.toSet()

            docComentarii.edit().putStringSet("comentarii", setPerechiComentarii).apply()
            docComentarii.edit().putString("refPostare", postare.id).apply()
            docComentarii.edit().putString("aliasUser", postare.numeUser).apply()

            val dialog = VizualizareComentarii()
            dialog.setOnDismissCallback { comentariiNoi ->
                postare.comentarii = comentariiNoi
            }
            dialog.show(mainActivityContext.supportFragmentManager, "Vizualizează comentarii")
        }
    }

    override fun getItemCount() = lista_postari.size

    private fun numarAprecieriTransform(numar: Int): String{
        return when {
            numar < 1000 -> numar.toString()
            numar < 1000000 -> String.format("%.1fK", numar / 1000)
            else -> String.format("%.1fM", numar / 1000000)
        }
    }
}