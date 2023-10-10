package com.orbys.demowhiteboard.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.orbys.demowhiteboard.R
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class MyYoutubePlayerView : View, OnTouchListener {

    private var startX: Float = 0f
    private var startY: Float = 0f

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                // Mueve la vista
                val deltaX = event.x - startX
                val deltaY = event.y - startY

                v?.translationX?.plus(deltaX)
                v?.translationY?.plus(deltaY)

                // Mueve el youtube player
                val player = v?.findViewById<YouTubePlayerView>(R.id.youtube_player_view)
                player?.translationX?.plus(deltaX)
                player?.translationY?.plus(deltaX)
            }
        }

        return true
    }
}