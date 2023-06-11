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
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class PostariFeedAdapter(context: Context?, private val lista_postari: MutableList<Postare>) : RecyclerView.Adapter<PostariFeedViewHolder>(){

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
                Log.d("testUser", postare.user + ", " + mainActivityContext.getUser()!!.urmareste.toString())
                holder.urmaresteLayout.visibility = View.VISIBLE
            } else {
                holder.urmaresteLayout.visibility = View.GONE
            }

            if (postare.iconUser) {
                val userIconRef = postareRef.child("icon.jpg")

                Glide.with(mainActivityContext)
                    .load(userIconRef)
                    .override(35, 35)
                    .centerCrop()
                    .into(holder.iconUser)
            } else {
                holder.iconUser.setImageResource(R.drawable.icon_cont)
            }

            holder.iconUserCard.setOnClickListener {
                val docVizUser = mainActivityContext.getSharedPreferences("vizUser", Context.MODE_PRIVATE)

                docVizUser.edit().putString("refUser", postare.user).apply()

                val dialog = VizualizareDetaliiUser()
                dialog.show(mainActivityContext.supportFragmentManager, "Vizualizează detalii user")
            }
        }

        when(postare.tipLocatie.lowercase()){
            "bar", "night_club" -> holder.iconLocatie.setImageResource(R.drawable.icon_bar)
            "cafe" -> holder.iconLocatie.setImageResource(R.drawable.icon_cafe)
            "restaurant" -> holder.iconLocatie.setImageResource(R.drawable.icon_restaurant)
            "bakery" -> holder.iconLocatie.setImageResource(R.drawable.icon_bakery)

            "book_store", "clothing_store", "electronics_store", "jewelry_store",
            "shoe_store", "shopping_mall" -> holder.iconLocatie.setImageResource(R.drawable.icon_mall)
            "convenience_store" -> holder.iconLocatie.setImageResource(R.drawable.icon_store)
            "grocery_or_supermarket", "supermarket", "store" -> holder.iconLocatie.setImageResource(R.drawable.icon_supermarket)
            "pharmacy" -> holder.iconLocatie.setImageResource(R.drawable.icon_pharmacy)

            "lodging" -> holder.iconLocatie.setImageResource(R.drawable.icon_lodging)

            "amusement_park" -> holder.iconLocatie.setImageResource(R.drawable.icon_attraction)
            "tourist_attraction" -> holder.iconLocatie.setImageResource(R.drawable.icon_historic)
            "movie_theater" -> holder.iconLocatie.setImageResource(R.drawable.icon_movie)
            "museum" -> holder.iconLocatie.setImageResource(R.drawable.icon_museum)
            "theater" -> holder.iconLocatie.setImageResource(R.drawable.icon_theater)

            "campground" -> holder.iconLocatie.setImageResource(R.drawable.icon_camping)
            "park" -> holder.iconLocatie.setImageResource(R.drawable.icon_park)
            "stadium" -> holder.iconLocatie.setImageResource(R.drawable.icon_stadium)
            "zoo" -> holder.iconLocatie.setImageResource(R.drawable.icon_zoo)
        }

        when(postare.tipRecenzie.lowercase()){
            "negativ" -> holder.tipRecenzieIcon.setImageResource(R.drawable.icon_recenzie_negativ)
            "neutru" -> holder.tipRecenzieIcon.setImageResource(R.drawable.icon_recenzie_neutru)
            "pozitiv" -> holder.tipRecenzieIcon.setImageResource(R.drawable.icon_recenzie_pozitiv)
        }

        if (postare.tipRecenzie.lowercase() != "nedefinit"){
            holder.tipRecenzieIcon.visibility = View.VISIBLE
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
                if (!mainActivityContext.postariInteractionateUser.containsKey(postare.id)){
                    val dataActuala = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                    mainActivityContext.postariInteractionateUser[postare.id] = Triple(postare.categorieLocatie, 0, dataActuala)
                }

                if (mainActivityContext.esteInPostariApreciate(postare.id)) {
                    holder.postareApreciere.setImageResource(R.drawable.icon_apreciere_gol)
                    postare.aprecieri -= 1

                    mainActivityContext.postariInteractionateUser[postare.id] = Triple(postare.categorieLocatie, maxOf(mainActivityContext.postariInteractionateUser[postare.id]!!.second - 2, 0), mainActivityContext.postariInteractionateUser[postare.id]!!.third)
                    mainActivityContext.categoriiPostariInteresUser[postare.categorieLocatie] = maxOf(mainActivityContext.categoriiPostariInteresUser[postare.categorieLocatie]!! - 2, 0)

                    holder.nrAprecieri.text = numarAprecieriTransform(postare.aprecieri)

                    mainActivityContext.editPostariApreciate(postare.id, "stergere")
                }
                else {
                    holder.postareApreciere.setImageResource(R.drawable.icon_apreciere_plin)
                    postare.aprecieri += 1

                    mainActivityContext.categoriiPostariInteresUser[postare.categorieLocatie] = mainActivityContext.categoriiPostariInteresUser[postare.categorieLocatie]!! + 2
                    mainActivityContext.postariInteractionateUser[postare.id] = Triple(postare.categorieLocatie, mainActivityContext.postariInteractionateUser[postare.id]!!.second + 2, mainActivityContext.postariInteractionateUser[postare.id]!!.third)

                    holder.nrAprecieri.text = numarAprecieriTransform(postare.aprecieri)

                    mainActivityContext.editPostariApreciate(postare.id, "adaugare")
                }

                // adaug/scad 2 la sistemul de scor al postarilor de interes pentru fiecare apreciere/dezapreciere și modific în fișier
                mainActivityContext.sharedPrefsPostariInteres.edit().putInt(postare.categorieLocatie, mainActivityContext.categoriiPostariInteresUser[postare.categorieLocatie]!!).apply()

                if (mainActivityContext.postariInteractionateUser[postare.id]!!.second == 0){
                    mainActivityContext.postariInteractionateUser.remove(postare.id)
                    mainActivityContext.sharedPrefsPostariInteres.edit().remove(postare.id).apply()
                }

                if (mainActivityContext.postariInteractionateUser.containsKey(postare.id)){
                    if (mainActivityContext.postariInteractionateUser.size > mainActivityContext.getMumarMaximPostariInteractionate()) {
                        // preiau id-ul celei mai vechi locatii
                        val idCeaMaiVechePostare = mainActivityContext.preiaCeaMaiVechePostare()!!

                        // iau detaliile acesteia
                        val detaliiPostare = mainActivityContext.postariInteractionateUser[idCeaMaiVechePostare]

                        // apoi elimin detaliile asociate acesteia si recalculez scorul
                        mainActivityContext.postariInteractionateUser.remove(idCeaMaiVechePostare)
                        mainActivityContext.sharedPrefsPostariInteres.edit().remove(idCeaMaiVechePostare).apply()

                        mainActivityContext.categoriiPostariInteresUser[detaliiPostare!!.first] = mainActivityContext.categoriiPostariInteresUser[detaliiPostare.first]!! - detaliiPostare.second
                        mainActivityContext.sharedPrefsPostariInteres.edit().putInt(detaliiPostare.first, mainActivityContext.categoriiPostariInteresUser[detaliiPostare.first]!!).apply()
                    }

                    val json = Gson().toJson(mainActivityContext.postariInteractionateUser[postare.id]!!.toList())
                    mainActivityContext.sharedPrefsPostariInteres.edit().putString(postare.id, json).apply()
                }

                if (mainActivityContext.postariInteractionateUser.isNotEmpty()){
                    mainActivityContext.procentPostariInteresUser = mainActivityContext.transformaCategoriiPostariInteresProcentual()
                }
                else {
                    mainActivityContext.procentPostariInteresUser = hashMapOf()
                }

                Log.d("testPostari", mainActivityContext.categoriiPostariInteresUser.toString())
                Log.d("testPostari", mainActivityContext.procentPostariInteresUser.toString())
                Log.d("testPostari", mainActivityContext.postariInteractionateUser.toString())
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
        }
        else {
            holder.mediaCard.visibility = View.GONE
        }

        holder.comentarii.setOnClickListener {
            val docComentarii = mainActivityContext.getSharedPreferences("comentarii", Context.MODE_PRIVATE)

            val setPerechiComentarii = postare.comentarii.map { (key, value) -> "$key:$value" }.toSet()

            docComentarii.edit().putStringSet("comentarii", setPerechiComentarii).apply()
            docComentarii.edit().putString("refPostare", postare.id).apply()
            docComentarii.edit().putString("aliasUser", postare.numeUser).apply()
            docComentarii.edit().putString("categorieLocatie", postare.categorieLocatie).apply()

            val dialog = VizualizareComentarii()
            dialog.setOnDismissCallback { comentariiNoi ->
                postare.comentarii = comentariiNoi
            }
            dialog.show(mainActivityContext.supportFragmentManager, "Vizualizează comentarii")
        }
    }

    fun adaugaPostari(postariNoi: List<Postare>) {
        for (postare in postariNoi){
            lista_postari.add(postare)
            notifyItemInserted(lista_postari.size - 1)
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