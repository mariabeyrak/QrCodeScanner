package com.noisyminer.qrscanner

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.hardware.Camera
import android.os.Handler
import android.util.Log
import android.widget.FrameLayout
import androidx.annotation.RequiresPermission
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.noisyminer.qrscanner.camera.CameraSource
import com.noisyminer.qrscanner.camera.CameraSourcePreview
import java.io.IOException


class QrScannerLayout @JvmOverloads constructor(context: Context, attrSet: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrSet, defStyleAttr) {

    val DISPLAY_WIDTH: Int = Resources.getSystem().displayMetrics.widthPixels
    val DISPLAY_HEIGHT: Int = Resources.getSystem().displayMetrics.heightPixels

    var cameraSource: CameraSource? = null

    val preview = CameraSourcePreview(context, attrSet)
    val dimView = DimView(context)

    var callback: QrScannerTextListener? = null

    var lastText = ""

    init {
        addView(preview)
        addView(dimView)
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    fun createCameraSource(applicationContext: Context) {

        val barcodeDetector = BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build()

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
                Handler(context.mainLooper).post {
                    val arr = detections?.detectedItems ?: return@post
                    for (i in 0 until arr.size())
                        onProcess(arr.valueAt(i).rawValue ?: continue)
                }
            }
        })

        cameraSource = CameraSource.Builder(applicationContext, barcodeDetector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
            .setRequestedFps(32.0f)
            .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
            .build()

        startCameraSource()
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    fun startCameraSource() {
        cameraSource?.let {
            try {
                preview.start(cameraSource)
            } catch (e: IOException) {
                cameraSource?.release()
                cameraSource = null
            }
        }
    }

    private fun onProcess(raw: String) {
        if (raw == lastText) return
        lastText = raw
        callback?.onText(raw)
        dimView.collapse()
    }

    fun pause() {
        preview.stop()
    }

    fun destroy() {
        preview.release()
        callback = null
    }
}

interface QrScannerTextListener {

    fun onText(raw: String)

    fun onError()
}