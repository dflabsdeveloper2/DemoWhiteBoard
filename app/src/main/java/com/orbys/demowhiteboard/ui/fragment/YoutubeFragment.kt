package com.orbys.demowhiteboard.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.orbys.demowhiteboard.R
import com.orbys.demowhiteboard.core.Util
import com.orbys.demowhiteboard.core.hideKeyboard
import com.orbys.demowhiteboard.data.api.RetrofitClient
import com.orbys.demowhiteboard.data.api.model.YoutubeModel
import com.orbys.demowhiteboard.data.api.model.youtube.ListVideoModelApi
import com.orbys.demowhiteboard.databinding.FragmentYoutubeBinding
import com.orbys.demowhiteboard.ui.adapter.AdapterYoutube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class YoutubeFragment : Fragment() {

    private lateinit var binding: FragmentYoutubeBinding

    private lateinit var adapterYoutube: AdapterYoutube
    private lateinit var listVideos:MutableList<YoutubeModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentYoutubeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        initValues()
        initListenners()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapterYoutube = AdapterYoutube(listOf()){
            Toast.makeText(context, it.title,Toast.LENGTH_SHORT).show()
        }

        binding.rvYoutube.apply {
            layoutManager = GridLayoutManager(context,2)
            adapter = adapterYoutube
        }
    }

    private fun initValues() {
        listVideos = mutableListOf()
    }

    private fun initListenners() {
        binding.etSearchYoutube.setOnEditorActionListener { v, actionId, event ->
            // Handle the IME options
            when (actionId) {
                EditorInfo.IME_ACTION_GO -> {
                    v.hideKeyboard()
                    binding.pbLoading.isVisible = true
                    listVideos.clear()
                    // Do something when the user presses "Done"
                    val wordSearch = binding.etSearchYoutube.text.toString()
                    if (wordSearch.isNotBlank()) {
                        lifecycleScope.launch {
                            val apikey =
                                context?.resources?.getString(R.string.api_key_youtube).orEmpty()
                            val listVideosYoutube: ListVideoModelApi? = withContext(Dispatchers.IO) {
                                try {
                                    RetrofitClient.serviceYoutube.getListYoutubeVideos(
                                        key = apikey,
                                        wordSearch, maxResult = Util.maxResultsYoutube,
                                        type = Util.typeYoutube,
                                        part = Util.partYoutuve
                                    )
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        binding.pbLoading.isVisible = false
                                        Toast.makeText(context, "Error $e", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                    null
                                }
                            }

                            listVideosYoutube?.let {
                                if (it.items.isNotEmpty()) {
                                    listVideos.addAll(it.items.map { item ->
                                        YoutubeModel(
                                            item.id.videoId,
                                            item.snippet.thumbnails.medium.url,
                                            item.snippet.title
                                        )
                                    })
                                }
                            }

                            if(listVideos.isNotEmpty()){
                                adapterYoutube.updateList(listVideos)
                            }

                            binding.pbLoading.isVisible = false
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Debes introducir un valor para buscar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    true
                }

                else -> false
            }
        }
    }
}