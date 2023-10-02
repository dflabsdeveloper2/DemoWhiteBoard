package com.orbys.demowhiteboard.ui.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.orbys.demowhiteboard.R
import com.orbys.demowhiteboard.databinding.ItemFileBinding
import java.io.File

class FilesViewHolder(view: View):ViewHolder(view) {

    private val binding = ItemFileBinding.bind(view)

    fun render(listFiles: List<File>, selected: Int, onClick: (Int) -> Unit) {

        binding.tvNameFile.text = listFiles[selected].name
        if(listFiles[selected].isFile){
            binding.ivImage.setImageDrawable(ContextCompat.getDrawable(itemView.context,R.drawable.doc))
        }else{
            binding.ivImage.setImageDrawable(ContextCompat.getDrawable(itemView.context,R.drawable.folder))
        }

        itemView.setOnClickListener {
            onClick(selected)
        }
    }
}