package com.noisyminer.qrscanner

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class DimView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {

    val paint: Paint = Paint().apply {
        color = Color.WHITE
        setAlpha(0.4F)
    }

    fun collapse() {

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.clipPath(calcPath(), Region.Op.DIFFERENCE)
        canvas?.drawRect(0F, 0F, width.toFloat(), height.toFloat(), paint)
    }

    private fun calcPath() = Path().apply {
        val h = height * 0.4F
        val w = width * 0.75F
        val d = min(h, w)
        val hP = (height - d) / 2
        val wP = (width - d) / 2

        moveTo(wP, hP)
        lineTo(width - wP, hP)
        lineTo(width - wP, height - hP)
        lineTo(wP, height - hP)
        lineTo(wP, hP)
    }
}