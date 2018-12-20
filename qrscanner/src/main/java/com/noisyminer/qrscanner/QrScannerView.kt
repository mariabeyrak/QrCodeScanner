package com.noisyminer.qrscanner

import android.Manifest
import android.content.Context
import android.util.AttributeSet
import android.hardware.Camera
import android.widget.FrameLayout
import androidx.annotation.RequiresPermission
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.noisyminer.qrscanner.camera.CameraSource
import com.noisyminer.qrscanner.camera.CameraSourcePreview
import java.io.IOException


class QrScannerLayout @JvmOverloads constructor(context: Context, attrSet: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrSet, defStyleAttr) {

    var cameraSource: CameraSource? = null

    val preview = CameraSourcePreview(context, attrSet)
    val dimView = DimView(context)

    var callback: QrScannerTextListener? = null

    init {
        addView(preview)
        addView(dimView)
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    fun createCameraSource(applicationContext: Context, w: Int, h: Int) {

        val barcodeDetector = BarcodeDetector.Builder(context).build()

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
                val arr = detections?.detectedItems ?: return
                for (i in 0 until arr.size())
                    onProcess(arr.valueAt(i).rawValue ?: continue)
            }
        })

        cameraSource = CameraSource.Builder(applicationContext, barcodeDetector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(w, h)
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
}