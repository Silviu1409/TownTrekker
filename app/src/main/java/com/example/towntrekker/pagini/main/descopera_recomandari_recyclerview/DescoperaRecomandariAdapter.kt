package com.example.towntrekker.pagini.main.descopera_recomandari_recyclerview

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.R
import com.example.towntrekker.datatypes.Recomandare
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class DescoperaRecomandariAdapter(context: Context?, private val lista_recomandari: List<Recomandare>) : RecyclerView.Adapter<DescoperaRecomandariViewHolder>(){
    private val mainActivityContext = (context as ActivityMain)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DescoperaRecomandariViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.descopera_recomandari_recomandare, parent, false)
        return DescoperaRecomandariViewHolder(view)
    }

    override fun onBindViewHolder(holder: DescoperaRecomandariViewHolder, position: Int) {
        val recomandare = lista_recomandari[position]

        if (recomandare.logoLocatie.isNotEmpty()){
            holder.logoLocatieCard.visibility = View.VISIBLE

            Glide.with(mainActivityContext)
                .load(recomandare.logoLocatie)
                .override(35, 35)
                .centerCrop()
                .into(holder.logoLocatie)
        } else {
            holder.logoLocatieCard.visibility = View.GONE
        }

        when(recomandare.tipLocatie.lowercase()){
            "bar", "night club" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_bar)
            "cafe" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_cafe)
            "restaurant" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_restaurant)
            "bakery" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_bakery)

            "books", "clothing", "electronics", "jewelry",
                "shoes", "shopping center/mall" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_mall)
            "convenience store" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_store)
            "grocery", "supermarket" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_supermarket)
            "pharmacy" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_pharmacy)

            "lodging" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_lodging)

            "golf" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_boating)
            "historic" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_historic)
            "movie" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_movie)
            "museum" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_museum)
            "theater" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_theater)

            "boating" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_boating)
            "camping" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_camping)
            "park" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_park)
            "stadium" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_stadium)
            "zoo" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_zoo)
        }

        holder.numeLocatie.text = recomandare.numeLocatie
        holder.adresaLocatie.text = recomandare.adresaLocatie

        if (recomandare.descriere.isNotEmpty()){
            holder.descriereLocatie.visibility = View.VISIBLE
            holder.descriereLocatie.text = mainActivityContext.getString(R.string.recomandare_descriere_tab).plus(recomandare.descriere)
        } else {
            holder.descriereLocatie.visibility = View.GONE
        }

        holder.hartaLocatie.onCreate(null)
        holder.hartaLocatie.getMapAsync { harta ->

            val latLng = recomandare.geoLocatie.split(", ")
            val geolocatie = LatLng(latLng[0].toDoubleOrNull() ?: 0.0, latLng[1].toDoubleOrNull() ?: 0.0)
            Log.d("locatie", geolocatie.toString())

            val markerLocatie = MarkerOptions().position(geolocatie).title(holder.numeLocatie.text.toString())
            harta.addMarker(markerLocatie)
            harta.moveCamera(CameraUpdateFactory.newLatLng(geolocatie))

            holder.hartaLocatie.visibility = View.VISIBLE
        }
    }

    override fun getItemCount() = lista_recomandari.size

    override fun onViewRecycled(holder: DescoperaRecomandariViewHolder) {
        super.onViewRecycled(holder)

        holder.hartaLocatie.onDestroy()
    }
}