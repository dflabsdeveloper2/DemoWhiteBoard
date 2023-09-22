package com.orbys.demowhiteboard.whiteboard

object WriteCommand {
    const val CLEAN = 0
    private const val DRAW_ACTION = 100
    const val DRAW_LINE_ACCELERATE = DRAW_ACTION + 1
    const val DRAW_LINE_SOFT = DRAW_ACTION + 2
    private const val ERASER_ACTION = 200
    const val ERASER_INVOKE = DRAW_ACTION + 1
    const val ERASER_SOFT = ERASER_ACTION + 2
    const val ERASER_ACCELERATE = ERASER_ACTION + 3
    const val ACCELERATE_ACTION = 300
    const val ACCELERATE_FINISH_REQUEST_RENDER = ACCELERATE_ACTION + 1
    private const val DEBUG = 100000
    const val DEBUG_LINE = DEBUG + 1
}