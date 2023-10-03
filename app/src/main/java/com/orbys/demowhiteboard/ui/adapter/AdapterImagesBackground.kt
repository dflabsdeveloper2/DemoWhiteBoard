package com.orbys.demowhiteboard.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.orbys.demowhiteboard.R
import java.io.File

class AdapterImagesBackground(private var listImages:List<File>, private var onClicked:(File)->Unit): RecyclerView.Adapter<ImagesBackgroundViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesBackgroundViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_imagebackground,parent,false)
        return ImagesBackgroundViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImagesBackgroundViewHolder, position: Int) {
        holder.render(listImages[position],onClicked)
    }

    override fun getItemCount() = listImages.size
}