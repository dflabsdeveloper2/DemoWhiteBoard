package com.orbys.demowhiteboard.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.orbys.demowhiteboard.R
import java.io.File

class AdapterFiles(private var listFiles: List<File>, private val onClick:(Int)->Unit): RecyclerView.Adapter<FilesViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file,parent,false)
        return FilesViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilesViewHolder, position: Int) {
        holder.render(listFiles,position,onClick)
    }

    override fun getItemCount() = listFiles.size

    fun updateList(list: List<File>){
        listFiles = list
        notifyDataSetChanged()
    }
}