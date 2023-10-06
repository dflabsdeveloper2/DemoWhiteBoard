package com.orbys.demowhiteboard.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.orbys.demowhiteboard.R
import com.orbys.demowhiteboard.data.api.model.YoutubeModel

class AdapterYoutube(private var listVideos: List<YoutubeModel>, private val onClick:(YoutubeModel)->Unit): RecyclerView.Adapter<YoutubeViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YoutubeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_google_youtube_image,parent,false)
        return YoutubeViewHolder(view)
    }

    override fun onBindViewHolder(holder: YoutubeViewHolder, position: Int) {
        val video = listVideos[position]
        holder.render(video,onClick)
    }

    override fun getItemCount() = listVideos.size

    fun updateList(list:List<YoutubeModel>){
        listVideos = list
        notifyDataSetChanged()
    }
}