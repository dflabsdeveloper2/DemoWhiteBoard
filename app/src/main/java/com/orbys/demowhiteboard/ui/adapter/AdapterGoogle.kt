package com.orbys.demowhiteboard.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.orbys.demowhiteboard.R
import com.orbys.demowhiteboard.data.api.model.ImageModel

class AdapterGoogle(private var listImages: List<ImageModel>, private val onClick:(ImageModel)->Unit): RecyclerView.Adapter<GoogleViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoogleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_google_youtube_image,parent,false)
        return GoogleViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoogleViewHolder, position: Int) {
        val image = listImages[position]
        holder.render(image,onClick)
    }

    override fun getItemCount() = listImages.size

    fun updateList(list: List<ImageModel>){
        listImages = list
        notifyDataSetChanged()
    }
}