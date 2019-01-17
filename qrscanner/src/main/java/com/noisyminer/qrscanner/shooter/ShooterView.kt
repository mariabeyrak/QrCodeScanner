package com.noisyminer.qrscanner

import android.content.Context
import android.util.AttributeSet
import android.view.View

abstract class ShooterView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {

    var onCollapseCallback: (() -> Unit)? = null
    var isCollapsing = false

    abstract fun expand()

    abstract fun collapse()
}