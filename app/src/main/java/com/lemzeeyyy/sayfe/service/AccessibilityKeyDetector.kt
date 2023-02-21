package com.lemzeeyyy.sayfe.service

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.lemzeeyyy.sayfe.database.SharedPrefs
import com.lemzeeyyy.sayfe.model.*
import com.lemzeeyyy.sayfe.network.NetworkObject
import com.lemzeeyyy.sayfe.repository.SayfeRepository
import com.lemzeeyyy.sayfe.utils.Utilities.NOTIFICATION_URL
import com.lemzeeyyy.sayfe.utils.Utilities.WEB_KEY
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class AccessibilityKeyDetector : AccessibilityService(),LocationListener {
    private val TAG = "AccessKeyDetector"
    private var listItem = mutableListOf<Int>()
    private var latitude = 0.0
    private var longitude = 0.0
    private var counter = 0
    private var locationUrl = ""
    private val outgoingAlertDb = Firebase.database
    private val myRef = outgoingAlertDb.getReference("OutgoingAlerts")
    private var outgoingDataList = mutableListOf<OutgoingAlertData>()

    companion object{
         var appTokenList : MutableList<String> = mutableListOf()
        var alertTriggerId : String = ""
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        val fAuth = Firebase.auth
        when(event?.keyCode){
            KeyEvent.KEYCODE_VOLUME_UP ->
            {
                val timer = object: CountDownTimer(300, 300) {
                    override fun onTick(millisUntilFinished: Long) {
                        listItem.add(1)
                        if(listItem.size == 4){
                            Log.d("TAG", "onTick: ${listItem.size}")
                            alertTriggerId = fAuth.currentUser?.uid.toString()
                            Toast.makeText(this@AccessibilityKeyDetector,"The volume up was tapped twice",Toast.LENGTH_SHORT)
                                .show()
                            listItem.clear()
                            if (SharedPrefs.getBoolean("volume",true) && fAuth.currentUser!=null){
                                getCurrentLocation()
                                val scope = CoroutineScope(Job() + Dispatchers.Main)

                                scope.launch {
                                    SayfeRepository.getCurrentUid().let {
                                        val usersList = SayfeRepository.getRegisteredGuardianAngels(it)
                                        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                                        val currentDate = sdf.format(Date())
                                        val citName = getCityName(longitude,latitude)
                                        val senderNam = SayfeRepository.getNotificationSender(it)
                                        val sos = SayfeRepository.getUserSosText(it)
                                        val outgoingAlertData = OutgoingAlertData(senderNam,locationUrl,currentDate,"Sayfe SOS Alert",citName)
                                        outgoingDataList.add(outgoingAlertData)
                                        Log.d(TAG, "onTick: $citName")

                                        saveOutgoingAlertToDb(it, outgoingDataList)
                                        usersList.forEach { user->
                                            sendPushNotifier(user,outgoingAlertData)
                                        }
                                        Toast.makeText(this@AccessibilityKeyDetector,"Your recipients have been notified",Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                    sendSmsFromBackEnd("+447823927201","\n$locationUrl")
                                    Toast.makeText(this@AccessibilityKeyDetector,"Message Sent",Toast.LENGTH_SHORT)
                                        .show()
                                }

                            }
                            listItem.clear()
                        }
                    }

                    override fun onFinish() {
                        listItem.clear()
                    }
                }
                timer.start()

            }
        }

        return super.onKeyEvent(event)

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
       val eventType = event?.eventType

        when (eventType){
            AccessibilityEvent.TYPE_VIEW_CLICKED -> Log.d(TAG, "onAccessibilityEvent: A button Clicked")

            AccessibilityEvent.TYPE_VIEW_FOCUSED ->  Log.d(TAG, "onAccessibilityEvent: A button Clicked")
        }

    }

    override fun onInterrupt() {
        Log.e(TAG, "onInterrupt: something went wrong")
    }


    override fun onServiceConnected() {
        getCurrentLocation()
        Log.i(TAG,"Service connected");
        val tempInfo = serviceInfo
        tempInfo.flags = tempInfo.flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        tempInfo.flags = tempInfo.flags or AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        tempInfo.flags = tempInfo.flags or AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE

        serviceInfo = tempInfo


        super.onServiceConnected()
    }

    private fun sendSMSSOS(phoneNumber : String,body: String) {
        try {

            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= 23) {

                this.getSystemService(SmsManager::class.java)
            } else {

                SmsManager.getDefault()
            }

            smsManager.sendTextMessage(
                        phoneNumber,
                        null,
                        "$body \n$locationUrl",
                        null,
                        null
                    )

            Toast.makeText(this, "Message Sent", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_LONG)
                .show()
        }
    }

    private suspend fun sendSmsFromBackEnd(recipient: String, body: String){
        NetworkObject.retrofitService.sendsms(recipient, body)
    }

    private fun sendPushNotifier(users: Users, data: OutgoingAlertData) {
        Log.d(TAG, "sendPushNotifier: ${users.currentUserId}")

        val body = Gson().toJson(data)

        val jsonObj = JSONObject()
        jsonObj.put("to", users.appToken)
        //jsonObj.put("notification", jsonNotifier)
        jsonObj.put("data", JSONObject(body))
        jsonObj.put("title",data)
        val request = okhttp3.Request.Builder()
            .url(NOTIFICATION_URL)
            .addHeader("Content-Type", "application/json")
            .addHeader(
                "Authorization",
                WEB_KEY
            )
            .post(
                jsonObj.toString().toRequestBody(
                    "application/json; charset=utf-8".toMediaType()
                )
            ).build()

        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BASIC
        OkHttpClient.Builder().addInterceptor(logger)
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build().newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("CLOUD_MSG", "onFailure-- $e")
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d(
                        "CLOUD_MSG", "onResponse-- $response +++ " +
                                "${call.isExecuted()}====${response.isSuccessful}" +
                                "--${response.code}===${response.body.toString()}" +
                                "=${response.message}"
                    )
                }
            })
    }

    private fun getCurrentLocation(){
         val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationUrl = ""

            Log.d(TAG, "getCurrentLocation: No permission enabled")
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val location: Location? = task.result
                if (location == null) {
                    Toast.makeText(this@AccessibilityKeyDetector,"Location not found",Toast.LENGTH_SHORT).show()
                } else {
                    longitude = location.longitude
                    latitude = location.latitude
                    locationUrl =
                        "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"

                }
            } else {
                Toast.makeText(
                   this,
                    task.exception?.message.toString(),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun getCityName(lon : Double,lat : Double) : String{
        var cityName: String = ""
        val geoCoder = Geocoder(this, Locale.getDefault())
        val address = geoCoder.getFromLocation(lat,lon,1) ?: return ""
        try {
            cityName = address[0].adminArea
            if (cityName == null){
                cityName = address[0].locality
                if (cityName == null){
                    cityName = address[0].subAdminArea
                }
            }
        }catch (e:Exception){
            cityName = ""
            Log.d("City Name Exception", "getCityName: ${e.message}")
        }
        return cityName
    }

    private fun saveOutgoingAlertToDb(currentUserid: String, outgoingAlertDataList: MutableList<OutgoingAlertData>){
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            outgoingAlertDataList.addAll(SayfeRepository.getOutgoingAlertList(currentUserid))
           SayfeRepository.saveOutgoingData(currentUserid,outgoingAlertDataList)
        }

    }

    override fun onLocationChanged(p0: Location) {
        latitude = p0.latitude
        longitude = p0.longitude
    }
}