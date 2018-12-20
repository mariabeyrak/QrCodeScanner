package com.noisyminer.qrcodescanner

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.noisyminer.qrscanner.QrScannerTextListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val DISPLAY_WIDTH: Int = Resources.getSystem().displayMetrics.widthPixels
    val DISPLAY_HEIGHT: Int = Resources.getSystem().displayMetrics.heightPixels

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        } else scanner.createCameraSource(applicationContext, DISPLAY_WIDTH, DISPLAY_HEIGHT)

        scanner.callback = object : QrScannerTextListener {
            override fun onText(raw: String) {
                Log.d("mytg", raw)
            }
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    override fun onResume() {
        super.onResume()
        scanner?.startCameraSource()
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissions.indexOfFirst { it == Manifest.permission.CAMERA }.also {
            if (grantResults[it] == PackageManager.PERMISSION_GRANTED) {
                scanner.createCameraSource(applicationContext, DISPLAY_WIDTH, DISPLAY_HEIGHT)
            }
        }
    }
}
