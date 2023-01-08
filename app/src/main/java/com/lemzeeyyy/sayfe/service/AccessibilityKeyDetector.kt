package com.lemzeeyyy.sayfe.service

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.lemzeeyyy.sayfe.database.SharedPrefs
import com.lemzeeyyy.sayfe.model.GuardianData
import com.lemzeeyyy.sayfe.model.OutgoingAlertData
import com.lemzeeyyy.sayfe.model.RecipientContact
import com.lemzeeyyy.sayfe.model.Users
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

const val NOTIFICATION_URL = "https://fcm.googleapis.com/fcm/send"
const val WEB_KEY = "key =AAAAjSSVQd8:APA91bGXkadZl3RJX9wdwsKQVE723byXjRYU2fK29jJtyykUjxWzxF_3nivclwlGT4z4VcvgDFIZvCMtcTI13gNptncMJxLDi5HALsBbsLoLvJ8Ybw5mOroDPybZ-wAZaxTQx0CsgxdI"
private const val DOUBLE_CLICK_TIME_DELTA: Long = 300 //milliseconds
class AccessibilityKeyDetector : AccessibilityService(),LocationListener {
    private val TAG = "AccessKeyDetector"
    private val database = Firebase.firestore
    private val fAuth = Firebase.auth
    private val collectionReference = database.collection("Guardian Angels")
    private val usersCollection = database.collection("Users")
    private var guardianList = mutableListOf<RecipientContact>()

    private var latitude = 0.0
    private var longitude = 0.0
    private var locationUrl = ""
    var lastClickTime: Long = 0
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
                val clickTime = System.currentTimeMillis()
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    Toast.makeText(this,"Tapped twice",Toast.LENGTH_LONG).show()
                    if (SharedPrefs.getBoolean("volume",true) && fAuth.currentUser!=null){
                        getCurrentLocation()

//                        fAuth.currentUser?.uid?.let {
//                           appTokenList =  getAppTokens(it)
//                            Log.d("TOKENLIST", "onKeyEvent: $appTokenList")
//                            Log.d("TOKENLIST", "onKeyEvent: ${getAppTokens(it)}")
//                            // alertTriggerId = it
//                        }

                        fAuth.currentUser?.uid?.let {
                            getGuardianAngelsAppToken(it)
                           // alertTriggerId = it
                        }
                    }
                }
                lastClickTime = clickTime
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
        TODO("Not yet implemented")
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

    private fun getGuardianAngelsListFromDb(currentUserid: String) {

        collectionReference.document(currentUserid)
            .get()
            .addOnSuccessListener {

                val data = it.toObject(GuardianData::class.java)

                data?.let { guardianData ->
                    guardianList = guardianData.guardianInfo
                    guardianList.forEach {
                       sendSMSSOS(it.number)
                    }
                }
            }
            .addOnFailureListener {

            }
    }

    private fun getGuardianAngelsAppToken(currentUserid: String){
        alertTriggerId = currentUserid
        collectionReference.document(currentUserid)
            .get()
            .addOnSuccessListener {
                val data = it.toObject(GuardianData::class.java)
                data?.let { guardianData ->
                    guardianList = guardianData.guardianInfo
                    guardianList.forEach {recipientContact ->
                     usersCollection.whereEqualTo("phoneNumber",recipientContact.number).get()
                         .addOnSuccessListener {querySnapshot ->
                         querySnapshot.forEach {queryDocumentSnapshot ->
                             val users = queryDocumentSnapshot.toObject(Users::class.java)
                            val appToken = users.appToken
                             val userName = users.fullName
                             appTokenList.add(appToken)
                             Log.d("APPTOKEN", "getGuardianAngelsAppToken: ${appTokenList}")
                             var cityName: String = ""
                             val geoCoder = Geocoder(this, Locale.getDefault())
                             val address = geoCoder.getFromLocation(latitude,longitude,1)
                             if (address != null) {
                                 cityName = address[0].adminArea
                                 if (cityName == null){
                                     cityName = address[0].locality
                                     if (cityName == null){
                                         cityName = address[0].subAdminArea
                                     }
                                 }
                             }


                             val outgoingAlertData = OutgoingAlertData(userName,
                                 locationUrl,"6:00am","Sayfe SOS Alert",cityName)

                             outgoingDataList.add(outgoingAlertData)
                             saveOutgoingAlertToDb(currentUserid, outgoingDataList)
                             sendPushNotifier(users,outgoingAlertData)

                         }
                     }
                         .addOnFailureListener{exception ->
                             Log.d("APPTOKEN", "getGuardianAngelsAppToken: ${exception.toString()}")
                         }
                    }
                }
            }
            .addOnFailureListener {

            }
    }
    private fun sendSMSSOS(phoneNumber : String) {
        try {

            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= 23) {

                this.getSystemService(SmsManager::class.java)
            } else {

                SmsManager.getDefault()
            }

            smsManager.sendTextMessage(
                        phoneNumber,
                        null,
                        "SOS! \n$locationUrl",
                        null,
                        null
                    )

            Toast.makeText(this, "Message Sent", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun sendPushNotifier(users: Users, data: OutgoingAlertData) {

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

//        try {
//            val header = HashMap<String, String>()
//
//            header["Content-Type"] = "application/json"
//            header["Authorization"] = WEB_KEY
//            val pushNotifier : PushNotifierBody = PushNotifierBody(appToken,data)
//           CoroutineScope(Dispatchers.IO).launch {
//               val request = ApiService.retrofitApiService.sendMsgPush(header,pushNotifier)
//               Log.d(TAG, "sendPushNotifier: ${request.code()}")
//               Log.d(TAG, "sendPushNotifier: ${request.body()}")
//               Log.d(TAG, "sendPushNotifier: ${request.errorBody()}")
//           }
//
//
//        }catch (e: Exception){
//            Log.d(TAG, "sendPushNotifier: ${e.toString()}")
//        }
    }


    private fun getCurrentLocation(){
         var fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            Log.d(TAG, "getCurrentLocation: No permission enabled")
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val location: Location? = task.result
                if (location == null) {
//                    Toast.makeText(
//                       this,
//                        "Location is Null",
//                        Toast.LENGTH_SHORT
//                    ).show()
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

    private fun saveOutgoingAlertToDb(currentUserid: String, outgoingAlertDataList: MutableList<OutgoingAlertData>){
        myRef.child(currentUserid).setValue(outgoingAlertDataList)
    }

    override fun onLocationChanged(p0: Location) {
        latitude = p0.latitude
        longitude = p0.longitude
    }
}