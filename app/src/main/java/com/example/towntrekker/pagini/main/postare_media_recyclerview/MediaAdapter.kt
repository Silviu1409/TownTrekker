package com.example.towntrekker.pagini.main.postare_media_recyclerview

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.firebase.storage.StorageReference


class MediaAdapter(context: Context?, private val lista_media: ArrayList<StorageReference>) : RecyclerView.Adapter<MediaViewHolder>(){
    private val mainActivityContext = (context as ActivityMain)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.postare_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val fisier = lista_media[position]

        fisier.metadata.addOnSuccessListener { metadata ->
            if (metadata.contentType?.startsWith("image/") == true) {
                holder.imagine.visibility = View.VISIBLE
                holder.video.visibility = View.GONE

                Glide.with(holder.itemView)
                    .load(fisier)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            holder.imagine.setImageDrawable(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
            else if (metadata.contentType?.startsWith("video/") == true) {
                holder.imagine.visibility = View.GONE
                holder.video.visibility = View.VISIBLE

                val player = ExoPlayer.Builder(mainActivityContext).build()

                fisier.downloadUrl.addOnSuccessListener { uri ->
                    val mediaItem = MediaItem.fromUri(uri.toString())

                    player.setMediaItem(mediaItem)
                    player.prepare()

                    holder.video.player = player
                }
            }
        }

    }

    override fun getItemCount() = lista_media.size
}