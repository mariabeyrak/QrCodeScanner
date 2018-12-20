package com.noisyminer.qrscanner

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.min

class DimView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {

    companion object {

        const val RECT_ROUND = 50F
        const val ANIM_DURATION_OUTER = 50L
        const val ANIM_DURATION = 80L
        const val DEFAULT_ALPHA = 0.55F
    }

    var radius = RECT_ROUND
    var dimens = 0F
    var heightPadding = 0F
    var widthPadding = 0F
    var outState = true

    var animator: Animator? = null

    val paint: Paint = Paint().apply {
        color = Color.WHITE
        setAlpha(DEFAULT_ALPHA)
        isAntiAlias = true
    }

    fun collapse() {
        animator = ValueAnimator.ofFloat(RECT_ROUND, dimens / 2).apply {
            addUpdateListener {
                (it.animatedValue as? Float)?.let {
                    radius = it
                    invalidate()
                }
            }
            duration = ANIM_DURATION_OUTER
            interpolator = LinearInterpolator()
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    outState = false
                    animator = ValueAnimator.ofFloat(dimens / 2, 0F).apply {
                        addUpdateListener {
                            (it.animatedValue as? Float)?.let {
                                radius = it
                                invalidate()
                            }
                        }
                        interpolator = LinearInterpolator()
                        duration = ANIM_DURATION
                        start()
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {}

                override fun onAnimationStart(animation: Animator?) {}
            })
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        animator = null
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.clipPath(calcPath(), Region.Op.DIFFERENCE)
        canvas?.drawRect(0F, 0F, width.toFloat(), height.toFloat(), paint)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val h = height * 0.45F
        val w = width * 0.85F
        dimens = min(h, w)
        heightPadding = (height - dimens) / 2
        widthPadding = (width - dimens) / 2
    }

    private fun calcPath() = Path().apply {

        if (outState) {
            moveTo(widthPadding, heightPadding + radius)
            arcTo(RectF(widthPadding, heightPadding, widthPadding + radius * 2, heightPadding + radius * 2), 180F, 90F)
            lineTo(width - widthPadding - radius, heightPadding)
            arcTo(RectF(width - widthPadding - radius * 2, heightPadding, width - widthPadding, heightPadding + radius * 2), 270F, 90F)
            lineTo(width - widthPadding, height - heightPadding - radius)
            arcTo(RectF(width - widthPadding - radius * 2, height - heightPadding - radius * 2, width - widthPadding, height - heightPadding), 0F, 90F)
            lineTo(widthPadding + radius, height - heightPadding)
            arcTo(RectF(widthPadding, height - heightPadding - radius * 2, widthPadding + radius * 2, height - heightPadding), 90F, 90F)
            lineTo(widthPadding, heightPadding + radius)
        } else {
            addCircle(width / 2F, height / 2F, radius, Path.Direction.CW)
        }

    }
}