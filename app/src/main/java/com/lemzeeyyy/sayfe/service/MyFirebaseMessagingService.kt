package com.lemzeeyyy.sayfe.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.activities.MainActivity
import com.lemzeeyyy.sayfe.fragments.DashboardFragment
import com.lemzeeyyy.sayfe.model.IncomingAlertData
import com.lemzeeyyy.sayfe.model.OutgoingAlertData
import com.lemzeeyyy.sayfe.model.Users
import com.lemzeeyyy.sayfe.repository.SayfeRepository
import com.lemzeeyyy.sayfe.service.AccessibilityKeyDetector.Companion.alertTriggerId
import com.lemzeeyyy.sayfe.service.AccessibilityKeyDetector.Companion.appTokenList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

const val CHANNEL_ID = "notification_id"
const val CHANNEL_NAME = "notification channel name"
const val NOTIFICATION_TITLE = "Title"
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION = "Sayfe Description"
class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val incomingAlertDb = Firebase.database
    private val fAuth = Firebase.auth
    private val myRef = incomingAlertDb.getReference("IncomingAlerts")
    private var incomingDataList = mutableListOf<IncomingAlertData>()

    override fun onNewToken(token: String) {
        updateToken(token)
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("apptokencheck", "onMessageReceived: $appTokenList ")
        message.data["alertBody"]?.let { generateNotification("Body",it) }
        val incomingAlertData = message.data["alertBody"]?.let {body ->
            message.data["title"]?.let { title ->
                message.data["location"]?.let { locationUrl ->
                    message.data["senderName"]?.let { senderName ->
                        message.data["timestamp"]?.let { timeStamp ->
                            IncomingAlertData(
                                senderName, body, timeStamp, title, locationUrl
                            )
                        }
                    }
                }
            }
        }

        if (incomingAlertData != null) {
            incomingDataList.add(incomingAlertData)
        }
        Log.d("check Message", "onMessageReceived: $incomingDataList")
        saveIncomingAlertToDb(incomingDataList)
    }

    private fun updateToken(token: String) {
        val database = Firebase.firestore
        val collectionReference = database.collection("Users")
        var fAuth = Firebase.auth
        val user = fAuth.currentUser
        val currentUserId = user?.uid
        collectionReference.whereEqualTo("userid", currentUserId)
            .addSnapshotListener { value, error ->
                if (!value!!.isEmpty) {
                    for (snapshot: QueryDocumentSnapshot in value) {
                        val value = snapshot.toObject(Users::class.java)
                        value.appToken = token
                        collectionReference.document(snapshot.id)
                            .set(value)
                    }
                }
            }
    }

    private fun generateNotification(title : String, message : String){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("OPENEDBYNOTIFICATION",true)
        intent.action = "ACTIVITIES"
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_IMMUTABLE)

        //channel id, channel name
        val builder : NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setVibrate(longArrayOf(1000,1000,1000,1000))
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

  val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(0,builder.build())
    }


    private fun saveIncomingAlertToDb(incomingAlertDataList: MutableList<IncomingAlertData>){

        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            fAuth.currentUser?.uid?.let {
                incomingAlertDataList.addAll(SayfeRepository.getIncomingAlertList(it))
                myRef.child(it).setValue(incomingAlertDataList)
            }
        }

    }


    }