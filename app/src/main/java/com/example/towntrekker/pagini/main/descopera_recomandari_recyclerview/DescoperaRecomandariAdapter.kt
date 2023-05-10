package com.example.towntrekker.pagini.main.descopera_recomandari_recyclerview

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    private lateinit var context: Context


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DescoperaRecomandariViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.descopera_recomandari_recomandare, parent, false)
        context = view.context
        return DescoperaRecomandariViewHolder(view)
    }

    override fun onBindViewHolder(holder: DescoperaRecomandariViewHolder, position: Int) {
        val recomandare = lista_recomandari[position]

        when(recomandare.tip.lowercase()){
            "bar", "night_club" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_bar)
            "cafe" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_cafe)
            "restaurant" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_restaurant)
            "bakery" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_bakery)

            "book_store", "clothing_store", "electronics_store", "jewelry_store",
                "shoe_store", "shopping_mall" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_mall)
            "convenience_store" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_store)
            "grocery_or_supermarket", "supermarket", "store" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_supermarket)
            "pharmacy" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_pharmacy)

            "lodging" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_lodging)

            "amusement_park" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_attraction)
            "tourist_attraction" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_historic)
            "movie_theater" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_movie)
            "museum" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_museum)
            "theater" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_theater)

            "campground" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_camping)
            "park" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_park)
            "stadium" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_stadium)
            "zoo" -> holder.iconNumeLocatie.setImageResource(R.drawable.icon_zoo)
        }

        if (recomandare.logo.isNotEmpty()){
            Glide.with(mainActivityContext)
                .load(recomandare.logo)
                .override(45, 45)
                .fitCenter()
                .placeholder(holder.iconNumeLocatie.drawable)
                .into(holder.logoLocatie)
        } else {
            holder.logoLocatie.setImageDrawable(holder.iconNumeLocatie.drawable)
        }

        if (recomandare.rating.isEmpty() or (recomandare.rating == "0")) {
            holder.ratingLocatieCard.visibility = View.GONE

            if (recomandare.logo.isEmpty()){
                holder.logoLocatieCard.visibility = View.GONE
            }
        } else {
            holder.ratingLocatieCard.visibility = View.VISIBLE
            holder.ratingLocatie.text = recomandare.rating
        }

        holder.numeLocatie.text = recomandare.nume
        holder.adresaLocatie.text = recomandare.adresa

        if (recomandare.descriere.isNotEmpty()){
            holder.descriereLocatie.visibility = View.VISIBLE
            holder.descriereLocatie.text = mainActivityContext.getString(R.string.recomandare_descriere_tab).plus(recomandare.descriere)
        } else {
            holder.descriereLocatie.visibility = View.GONE
        }

        holder.hartaLocatie.onCreate(null)
        holder.hartaLocatie.visibility = View.VISIBLE
        holder.hartaLocatie.getMapAsync { harta ->
            harta.uiSettings.isMapToolbarEnabled = false

            val latLng = recomandare.geoLocatie.split(", ")
            Log.d("latlng", latLng.toString())
            val geolocatie = LatLng(latLng[0].toDoubleOrNull() ?: 0.0, latLng[1].toDoubleOrNull() ?: 0.0)

            val markerLocatie = MarkerOptions().position(geolocatie).title(holder.numeLocatie.text.toString())
            harta.addMarker(markerLocatie)
            harta.moveCamera(CameraUpdateFactory.newLatLng(geolocatie))

            holder.butonRuta.setOnClickListener {
                val intentNav = Intent(Intent.ACTION_VIEW,
                    Uri.parse("google.navigation:q=${geolocatie.latitude},${geolocatie.longitude}"))
                intentNav.setPackage("com.google.android.apps.maps")

                if (intentNav.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intentNav)
                } else {
                    Toast.makeText(context, "Google Maps nu este instalat.", Toast.LENGTH_SHORT).show()
                }
            }

            holder.butonMaps.setOnClickListener {
                val intentMaps = Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=${geolocatie.latitude},${geolocatie.longitude}"))
                intentMaps.setPackage("com.google.android.apps.maps")

                if (intentMaps.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intentMaps)
                } else {
                    Toast.makeText(context, "Google Maps nu este instalat.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun getItemCount() = lista_recomandari.size

    override fun onViewRecycled(holder: DescoperaRecomandariViewHolder) {
        super.onViewRecycled(holder)

        holder.hartaLocatie.onDestroy()
    }
}