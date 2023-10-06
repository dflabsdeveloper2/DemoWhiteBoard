package com.orbys.demowhiteboard.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.orbys.demowhiteboard.data.api.model.YoutubeModel
import com.orbys.demowhiteboard.databinding.ItemGoogleYoutubeImageBinding

class YoutubeViewHolder(view:View):ViewHolder(view) {

    private val binding = ItemGoogleYoutubeImageBinding.bind(view)

    fun render(video: YoutubeModel, onClick: (YoutubeModel) -> Unit) {
        binding.ivImage.load(video.thumbnails)
        itemView.setOnClickListener {
            onClick(video)
        }
    }
}