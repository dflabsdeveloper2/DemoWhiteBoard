package com.orbys.demowhiteboard.ui.view

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.orbys.demowhiteboard.R

class ViewImage(context: Context) : ConstraintLayout(context) {
    var ivMainImage: ImageView
    init {
        View.inflate(context, R.layout.view_image, this)
        ivMainImage = findViewById(R.id.ivMainImage)
    }
}
