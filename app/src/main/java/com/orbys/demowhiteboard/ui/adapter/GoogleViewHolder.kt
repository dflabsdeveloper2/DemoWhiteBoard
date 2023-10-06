package com.orbys.demowhiteboard.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.orbys.demowhiteboard.data.api.model.ImageModel
import com.orbys.demowhiteboard.databinding.ItemGoogleYoutubeImageBinding

class GoogleViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val binding = ItemGoogleYoutubeImageBinding.bind(view)

    fun render(image: ImageModel, onClick: (ImageModel) -> Unit) {
        binding.ivImage.load(image.url)
        itemView.setOnClickListener {
            onClick(image)
        }
    }
}