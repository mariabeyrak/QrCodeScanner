package com.noisyminer.qrscanner

import android.Manifest
import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.hardware.Camera
import android.os.Handler
import android.widget.FrameLayout
import androidx.annotation.RequiresPermission
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.barcode.Barcode
import com.noisyminer.qrscanner.camera.CameraSource
import com.noisyminer.qrscanner.camera.CameraSourcePreview
import com.noisyminer.qrscanner.shooter.ShooterView
import java.io.IOException


class QrScannerLayout @JvmOverloads constructor(context: Context, attrSet: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrSet, defStyleAttr) {

    private val DISPLAY_WIDTH: Int = Resources.getSystem().displayMetrics.widthPixels
    private val DISPLAY_HEIGHT: Int = Resources.getSystem().displayMetrics.heightPixels

    private var cameraSource: CameraSource? = null

    private val preview = CameraSourcePreview(context, attrSet)

    private var processing = false
    private var lastText = ""

    private var shooterView: ShooterView? = null

    var callback: QrScannerTextListener? = null

    init {
        addView(preview)
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    fun createCameraSource(applicationContext: Context) {

        val barcodeDetector = BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build()

        barcodeDetector.setProcessor(MultiProcessor.Builder<Barcode>(MultiProcessor.Factory<Barcode> {
            object : Tracker<Barcode>() {
                override fun onNewItem(barcodes: Int, barcode: Barcode?) {
                    onDetect(barcode ?: return)
                }

                override fun onMissing(barcodes: Detector.Detections<Barcode>?) {
                    onDetect(barcodes?.detectedItems?.get(0) ?: return)
                }

                override fun onUpdate(barcodes: Detector.Detections<Barcode>?, barcode: Barcode?) {
                    onDetect(barcode ?: barcodes?.detectedItems?.get(0) ?: return)
                }

                override fun onDone() {}
            }
        }).build())

        cameraSource = CameraSource.Builder(applicationContext, barcodeDetector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
            .setRequestedFps(15.0f)
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

    private fun onDetect(barcode: Barcode) {
        Handler(context.mainLooper).post {
            onProcess(barcode.rawValue ?: return@post)
        }
    }

    private fun onProcess(raw: String) {
        if (raw == lastText || processing) return
        processing = true
        lastText = raw
        callback?.onText(raw)
        shooterView?.collapse()
    }

    fun setShooter(shooter: ShooterView) {
        shooterView = shooter
        addView(shooter)
    }

    fun hasCameraSource() = cameraSource != null

    fun setOnCollapseCallback(callback: () -> Unit) {
        shooterView?.let {
            if (!it.isCollapsing) callback()
            it.onCollapseCallback = if (!it.isCollapsing) null else callback
        } ?: run {
            callback()
        }
    }

    fun processed() {
        setOnCollapseCallback {
            processing = false
            shooterView?.expand()
        }
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