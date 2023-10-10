package com.orbys.demowhiteboard.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.util.Log
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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.DefaultPlayerUiController
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class YoutubeFragment : Fragment() {

    private lateinit var binding: FragmentYoutubeBinding

    private lateinit var adapterYoutube: AdapterYoutube
    private lateinit var listVideos: MutableList<YoutubeModel>
    private lateinit var activity: Activity
    private lateinit var myViewerYoutube: YouTubePlayerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentYoutubeBinding.inflate(inflater, container, false)
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
        adapterYoutube = AdapterYoutube(listOf()) {
            Toast.makeText(context, it.id, Toast.LENGTH_SHORT).show()
            //val myPlayer= activity.findViewById<MyYoutubePlayerView>(R.id.myContainerYoutube)
            Log.d("YOUTUBE", "id: ${it.id}")
            try {
                myViewerYoutube.apply {
                    isVisible = true

                    enableAutomaticInitialization = false
                    val listenner = object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            // using pre-made custom ui
                            val defaultPlayerUiController =
                                DefaultPlayerUiController(this@apply, youTubePlayer)
                            setCustomPlayerUi(defaultPlayerUiController.rootView)

                            Log.d("YOUTUBE", "id: ${it.id}")
                            youTubePlayer.cueVideo(it.id, 0f)
                        }
                    }

                    val options: IFramePlayerOptions =
                        IFramePlayerOptions.Builder().controls(1).build()

                    initialize(listenner,options)
                }
            } catch (e: Exception) {
                Log.d("YOUTUBE", "Error inicializar $e")
            }
        }

        binding.rvYoutube.apply {
            layoutManager = GridLayoutManager(context,2)
            adapter = adapterYoutube
        }
    }

    private fun initValues() {
        activity = requireActivity()
        listVideos = mutableListOf()
        myViewerYoutube = activity.findViewById(R.id.youtube_player_view)
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