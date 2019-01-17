package com.noisyminer.qrscanner.shooter

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.View

abstract class ShooterView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {

    var onCollapseCallback: (() -> Unit)? = null
    var isCollapsing = false
    var animator: Animator? = null

    abstract fun expand()

    abstract fun collapse()

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        animator = null
        onCollapseCallback = null
    }
}