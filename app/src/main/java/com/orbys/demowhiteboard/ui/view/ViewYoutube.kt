package com.orbys.demowhiteboard.ui.view

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.orbys.demowhiteboard.R
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class ViewYoutube(context: Context) : ConstraintLayout(context) {
    private var ivMainYoutube: YouTubePlayerView

    init {
        View.inflate(context, R.layout.view_youtube, this)
        ivMainYoutube = findViewById(R.id.ivMainYoutube)
    }
}