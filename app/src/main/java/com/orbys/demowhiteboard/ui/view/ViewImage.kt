package com.orbys.demowhiteboard.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.orbys.demowhiteboard.R

class ViewImage: ConstraintLayout/*, View.OnTouchListener*/ {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    // Parámetros para el widget flotante
    private val windowManager: WindowManager
    private var initialX: Float = 0f
    private var initialY: Float = 0f

    init {
        View.inflate(context, R.layout.view_image, this)
        //setOnTouchListener(this)
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    /*// Movilidad del widget flotante y evento de clic
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = event.rawX
                initialY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - initialX
                val deltaY = event.rawY - initialY

                val layoutParams = layoutParams as WindowManager.LayoutParams
                layoutParams.x = (layoutParams.x + deltaX).toInt()
                layoutParams.y = (layoutParams.y + deltaY).toInt()

                windowManager.updateViewLayout(this, layoutParams)

                initialX = event.rawX
                initialY = event.rawY
            }
        }
        return true
    }*/

    fun destroy() {
        // Remueve la vista del WindowManager
        windowManager.removeView(this)
    }
}
