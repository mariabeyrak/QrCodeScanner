package com.noisyminer.qrcodescanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.noisyminer.qrscanner.shooter.RoundedRectShooter
import com.noisyminer.qrscanner.QrScannerTextListener
import com.noisyminer.qrscanner.shooter.RectShooter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var start = false
    var hasPerm = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanner.apply {
            setOnClickListener {
                scanner.processed()
            }
            viewTreeObserver.addOnGlobalLayoutListener {
                hasPerm = true
                tryToStart()
            }
            setShooter(RectShooter(context).apply {
                build()
            })
            callback = object : QrScannerTextListener {
                override fun onText(raw: String) {
                    Log.d("mytg", raw)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        start = false
        scanner.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        scanner.destroy()
    }

    private fun tryToStart() {
        if (!hasPerm || !start) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        } else {
            if (!scanner.hasCameraSource()) scanner.createCameraSource(applicationContext)
            scanner.startCameraSource()
        }
    }

    override fun onResume() {
        super.onResume()
        start = true
        tryToStart()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        tryToStart()
    }
}
