package com.orbys.demowhiteboard.ui.youtube.model

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

data class YoutubeVideo(
    val id:String,
    var viewer: YouTubePlayerView?,
    var x: Float,
    var y: Float,
    var width: Int,
    var height: Int,
    var rotation: Float,
    var scaleX: Float,
    var scaleY: Float,
    var page:Int
)