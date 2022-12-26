package com.lemzeeyyy.sayfe.activities

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.lemzeeyyy.sayfe.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
const val PERMISSION_REQUEST = 101
class SplashActivity : AppCompatActivity() {

    private val activityScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        requestPermission()


    }

    private fun requestPermission() {
        val readContactPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
        val writeContactPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
        val messagePermissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)

        if (readContactPermission == PackageManager.PERMISSION_GRANTED && writeContactPermission ==  PackageManager.PERMISSION_GRANTED
            && messagePermissionCheck == PackageManager.PERMISSION_GRANTED )
        {
            val intent: Intent = Intent(this@SplashActivity,MainActivity::class.java)
            startActivity(intent)

        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_CONTACTS, android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.SEND_SMS),
                PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent: Intent = Intent(this@SplashActivity,MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "You don't have required permission to use this message",
                    Toast.LENGTH_SHORT).show();
            }
        }
    }
}