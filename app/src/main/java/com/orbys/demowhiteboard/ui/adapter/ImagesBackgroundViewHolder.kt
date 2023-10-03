package com.orbys.demowhiteboard.ui.adapter

import android.graphics.BitmapFactory
import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.orbys.demowhiteboard.databinding.ItemImagebackgroundBinding
import java.io.File

class ImagesBackgroundViewHolder(view: View):ViewHolder(view) {

    private var binding = ItemImagebackgroundBinding.bind(view)

    fun render(file: File, onClicked: (File) -> Unit,){
        binding.ivImage.setImageBitmap(BitmapFactory.decodeFile(file.path))

        itemView.setOnClickListener {
            onClicked(file)
        }
    }
}
