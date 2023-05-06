package com.example.towntrekker.pagini.main.vizualizare_urmareste_urmaritori_recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.R
import com.example.towntrekker.pagini.main.VizualizareDetaliiUser


class VizualizareUrmaresteUrmaritoriAdapter(context: Context?, private val lista_useri: List<String>) : RecyclerView.Adapter<VizualizareUrmaresteUrmaritoriViewHolder>(){

    private val mainActivityContext = (context as ActivityMain)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VizualizareUrmaresteUrmaritoriViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.urmareste_urmaritori_user, parent, false)
        return VizualizareUrmaresteUrmaritoriViewHolder(view)
    }

    override fun onBindViewHolder(holder: VizualizareUrmaresteUrmaritoriViewHolder, position: Int) {
        val userRef = lista_useri[position]

        mainActivityContext.getDB().collection("useri").document(userRef).get()
            .addOnSuccessListener { doc ->
                holder.numeUser.text = doc.getString("alias") ?: ""

                val userIconRef = mainActivityContext.getStorage().child("useri").child(userRef).child("icon.jpg")

                userIconRef.metadata.addOnSuccessListener {
                    Glide.with(mainActivityContext)
                        .load(userIconRef)
                        .override(35, 35)
                        .centerCrop()
                        .into(holder.iconUser)
                }
            }

        holder.cardUser.setOnClickListener {
            val docVizUser = mainActivityContext.getSharedPreferences("vizUser", Context.MODE_PRIVATE)

            docVizUser.edit().putString("refUser", userRef).apply()

            val dialog = VizualizareDetaliiUser()
            dialog.show(mainActivityContext.supportFragmentManager, "Vizualizează detalii user")
        }
    }

    override fun getItemCount() = lista_useri.size
}