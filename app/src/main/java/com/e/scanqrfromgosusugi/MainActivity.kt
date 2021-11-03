package com.e.scanqrfromgosusugi

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.e.scanqrfromgosusugi.ui.main.ScanQrFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.INTERNET)
            Log.d("permissions", shouldShowRequestPermissionRationale(Manifest.permission.CAMERA).toString())
            ActivityCompat.requestPermissions(this, permissions,100)
        }
    }
}