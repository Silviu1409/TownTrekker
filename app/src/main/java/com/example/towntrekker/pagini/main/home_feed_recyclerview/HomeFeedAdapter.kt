package com.example.towntrekker.pagini.main.home_feed_recyclerview

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
import com.example.towntrekker.pagini.main.postare_media_recyclerview.MediaAdapter
import com.google.firebase.storage.StorageReference
import kotlin.collections.ArrayList


class HomeFeedAdapter(context: Context?, private val lista_postari: ArrayList<Postare>) : RecyclerView.Adapter<HomeFeedViewHolder>(){
    private val mainActivityContext = (context as ActivityMain)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFeedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_feed_postare, parent, false)
        return HomeFeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeFeedViewHolder, position: Int) {
        val postare = lista_postari[position]
        val postareRef = mainActivityContext.getStorage().child("postari").child(postare.id)

        if (postare.iconUser){
            val userIconRef = postareRef.child("icon.jpg")

            Glide.with(mainActivityContext)
                .load(userIconRef)
                .override(35, 35)
                .centerCrop()
                .into(holder.iconUser)
        }

        holder.numeUser.text = postare.numeUser
        holder.numeLocatie.text = postare.numeLocatie
        holder.adresaLocatie.text = postare.adresaLocatie

        if (postare.id in mainActivityContext.getPostariApreciate()){
            holder.postareApreciere.setImageResource(R.drawable.icon_apreciere_plin)
        } else {
            holder.postareApreciere.setImageResource(R.drawable.icon_apreciere_gol)
        }

        holder.postareApreciere.setOnClickListener {
            if (postare.id in mainActivityContext.getPostariApreciate()) {
                holder.postareApreciere.setImageResource(R.drawable.icon_apreciere_gol)
                holder.nrAprecieri.text = numarAprecieriTransform(postare.aprecieri)

                mainActivityContext.getPostariApreciate().remove(postare.id)
                mainActivityContext.getSharedPrefsLiked().edit().putStringSet("postari", mainActivityContext.getPostariApreciate()).apply()
            } else {
                holder.postareApreciere.setImageResource(R.drawable.icon_apreciere_plin)
                holder.nrAprecieri.text = numarAprecieriTransform(postare.aprecieri + 1)

                Log.d(mainActivityContext.getTag(), mainActivityContext.getPostariApreciate().toString())
                mainActivityContext.getPostariApreciate().add(postare.id)
                mainActivityContext.getSharedPrefsLiked().edit().putStringSet("postari", mainActivityContext.getPostariApreciate()).apply()
            }
        }

        holder.nrAprecieri.text = numarAprecieriTransform(postare.aprecieri)

        if (postare.descriere.isNotEmpty()){
            holder.descriere.visibility = View.VISIBLE
            holder.descriere.text = mainActivityContext.getString(R.string.postare_descriere_tab).plus(postare.descriere)
        } else {
            holder.descriere.visibility = View.GONE
        }

        if (postare.media){
            postareRef.listAll()
                .addOnSuccessListener { res ->
                    // exclud poza de profil a user-ului care a făcut postarea
                    postare.lista_media = res.items.filter { it.name != "icon.jpg" }

                    Log.d(mainActivityContext.getTag(), "S-au preluat cu succes fișierele media.")

                    holder.media.adapter = MediaAdapter(mainActivityContext, postare.lista_media as ArrayList<StorageReference>)
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

        if (postare.comentarii.isEmpty()) {
            holder.adaugaComentariuLayout.visibility = View.VISIBLE
        } else {
            holder.comentarii.visibility = View.VISIBLE
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