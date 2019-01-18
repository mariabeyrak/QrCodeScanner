package com.noisyminer.qrscanner.shooter

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import kotlin.math.min

class RectShooter @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : ShooterView(context, attrs, defStyle) {

    companion object {

        const val DEFAULT_ALPHA = 0.55F
        const val DEFAULT_BACKGROUND_COLOR = Color.BLACK

        const val DEFAULT_CORNER_STROKE_WIDTH = 25F
        const val DEFAULT_CORNER_STROKE_ROUND = 10F

        const val DEFAULT_DURATION_COLLAPSE = 200L
        const val DEFAULT_DURATION_EXPAND = 400L

        const val DEFAULT_DIMEN_PERCENT_OF_HEIGHT = 0.4F
        const val DEFAULT_DIMEN_PERCENT_OF_WIDTH = 0.8F
        const val DEFAULT_CORNER_LEN_PERCENT_OF_DIMEN = 0.2F

        val DEFAULT_CORNER_COLOR = Color.rgb(111, 207, 154)
    }

    private var dimens = 0F
    private var heightPadding = 0F
    private var widthPadding = 0F
    private var cornerLen = 0F
    private var dist = 0F
    private var maxDist = 0F
    private var bounds = RectF()
    private var collapsed = false

    var backgrounAlpha = DEFAULT_ALPHA
    var backgroungColor = DEFAULT_BACKGROUND_COLOR
    var cornerStrokeWidth = DEFAULT_CORNER_STROKE_WIDTH
    var cornerStrokeRound = DEFAULT_CORNER_STROKE_ROUND
    var durationCollapse = DEFAULT_DURATION_COLLAPSE
    var durationExpand = DEFAULT_DURATION_EXPAND
    var dimenPercentOfWidth = DEFAULT_DIMEN_PERCENT_OF_WIDTH
    var dimenPercentOfHeight = DEFAULT_DIMEN_PERCENT_OF_HEIGHT
    var cornerLenPercentOfDimen = DEFAULT_CORNER_LEN_PERCENT_OF_DIMEN
    var cornerColor = DEFAULT_CORNER_COLOR

    private lateinit var backgroundPaint: Paint
    private lateinit var cornerPaint: Paint

    fun build() {
        backgroundPaint = Paint().apply {
            color = backgroungColor
            setAlpha(backgrounAlpha)
        }
        cornerPaint = Paint().apply {
            color = cornerColor
            strokeWidth = cornerStrokeWidth
            isDither = false
            style = Paint.Style.STROKE
            if (cornerStrokeRound > 0) {
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                pathEffect = CornerPathEffect(cornerStrokeRound)
            }
            isAntiAlias = true
        }
    }

    override fun collapse() {
        isCollapsing = true
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0F, maxDist).apply {
            addUpdateListener {
                (it.animatedValue as? Float)?.let {
                    dist = it
                    invalidate()
                }
            }
            duration = durationCollapse
            interpolator = DecelerateInterpolator()
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    collapsed = true
                    isCollapsing = false
                    onCollapseCallback?.invoke()
                    onCollapseCallback = null
                }

                override fun onAnimationCancel(animation: Animator?) {}

                override fun onAnimationStart(animation: Animator?) {}
            })
            start()
        }
    }

    override fun expand() {
        if (!collapsed) return
        collapsed = false
        animator?.cancel()
        animator = ValueAnimator.ofFloat(maxDist, 0F).apply {
            addUpdateListener {
                (it.animatedValue as? Float)?.let {
                    dist = it
                    invalidate()
                }
            }
            interpolator = DecelerateInterpolator()
            duration = durationExpand
            start()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val h = height * dimenPercentOfHeight
        val w = width * dimenPercentOfWidth
        dimens = min(h, w)
        cornerLen = dimens * cornerLenPercentOfDimen
        maxDist = dimens / 2 - cornerLen
        heightPadding = (height - dimens) / 2
        widthPadding = (width - dimens) / 2
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            val wP = widthPadding + dist
            val hP = heightPadding + dist

            clipPath(calcBackgroundPath(wP, hP), Region.Op.DIFFERENCE)

            drawRect(0F, 0F, width.toFloat(), height.toFloat(), backgroundPaint)
            drawPath(calcCorner(wP, hP, 0F), cornerPaint)
            drawPath(calcCorner(width - wP, hP, 90F), cornerPaint)
            drawPath(calcCorner(width - wP, height - hP, 180F), cornerPaint)
            drawPath(calcCorner(wP, height - hP, 270F), cornerPaint)
        }
    }

    private fun calcCorner(x: Float, y: Float, radius: Float) = Path().apply {
        moveTo(x, y)
        rLineTo(cornerLen, 0F)
        rLineTo(-cornerLen, 0F)
        rLineTo(0F, cornerLen)
        rLineTo(0F, -cornerLen)
        computeBounds(bounds, true)
        transform(Matrix().apply {
            postRotate(radius, x, y)
        })
    }

    private fun calcBackgroundPath(wP: Float, hP: Float) = Path().apply {
        moveTo(wP, hP)
        lineTo(width - wP, hP)
        lineTo(width - wP, height - hP)
        lineTo(wP, height - hP)
        lineTo(wP, hP)
    }
}