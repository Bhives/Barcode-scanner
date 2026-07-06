package com.reproflex3.rplink.presentation.scan

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class SquareView : View {

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = STROKE_WIDTH
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val x0 = width / 2
        val y0 = height / 2
        val dx = width / 3
        val dy = height / 5

        canvas.drawRoundRect(
            (x0 - dx).toFloat(),
            (y0 - dy).toFloat(),
            (x0 + dx).toFloat(),
            (y0 + dy).toFloat(),
            CORNER_VALUE,
            CORNER_VALUE,
            paint
        )
    }

    fun changeColor(color: Int) {
        paint.color = color
        post {
            requestLayout()
            invalidate()
        }
    }

    companion object {
        private const val STROKE_WIDTH = 8f
        private const val CORNER_VALUE = 16f
    }
}