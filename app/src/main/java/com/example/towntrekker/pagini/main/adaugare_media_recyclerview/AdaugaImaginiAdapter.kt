package com.example.towntrekker.pagini.main.adaugare_media_recyclerview

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.R
import com.example.towntrekker.pagini.main.AdaugaPostare
import java.io.InputStream
import java.util.*


class AdaugaImaginiAdapter(private val context: Context?, private val fragment: AdaugaPostare, private val files: ArrayList<Uri>) : RecyclerView.Adapter<AdaugaImaginiViewHolder>(){

    private val mainActivityContext = (context as ActivityMain)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdaugaImaginiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adauga_postare_media, parent, false)
        return AdaugaImaginiViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdaugaImaginiViewHolder, position: Int) {
        val uriFisier = files[position]
        val mimeType = mainActivityContext.contentResolver.getType(uriFisier)

        if (mimeType?.startsWith("image/") == true) {
            val inputStream: InputStream? = mainActivityContext.contentResolver.openInputStream(uriFisier)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            Glide.with(mainActivityContext)
                .load(bitmap)
                .override(80, 80)
                .centerCrop()
                .into(holder.media)
        }
        else if (mimeType?.startsWith("video/") == true){
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uriFisier)

            val bitmap = retriever.frameAtTime

            Glide.with(mainActivityContext)
                .load(bitmap)
                .override(80, 80)
                .centerCrop()
                .into(holder.media)
        }

        holder.stergere.setOnClickListener {
            files.remove(uriFisier)
            fragment.stergereDescFisier(uriFisier)
            notifyItemRemoved(position)

            if (files.size == 0){
                fragment.ascundePreview()
            }
        }
    }

    fun adaugaItem(itemNou: Uri){
        files.add(itemNou)
        notifyItemInserted(files.size - 1)
    }

    override fun getItemCount() = files.size
}