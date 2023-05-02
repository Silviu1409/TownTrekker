@file:Suppress("DEPRECATION")
package com.example.towntrekker.pagini.main.postare_media_recyclerview

import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.towntrekker.R
import com.google.android.exoplayer2.ui.PlayerView


class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val media: LinearLayoutCompat = itemView.findViewById(R.id.media)
    val imagine: ImageView = itemView.findViewById(R.id.imagine)
    val video: PlayerView = itemView.findViewById(R.id.video)

    init {
        video.visibility = View.INVISIBLE
        imagine.visibility = View.VISIBLE

        video.setShowNextButton(false)
        video.setShowPreviousButton(false)
    }
}