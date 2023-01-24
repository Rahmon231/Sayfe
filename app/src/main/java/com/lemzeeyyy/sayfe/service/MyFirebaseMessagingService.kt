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
import com.lemzeeyyy.sayfe.service.AccessibilityKeyDetector.Companion.appTokenList

const val CHANNEL_ID = "notification_id"
const val CHANNEL_NAME = "notification channel name"
const val NOTIFICATION_TITLE = "Title"
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION = "Sayfe Description"
class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val outgoingAlertDb = Firebase.database
    private val fAuth = Firebase.auth
    private val myRef = outgoingAlertDb.getReference("IncomingAlerts")
    private var incomingDataList = mutableListOf<IncomingAlertData>()

    override fun onNewToken(token: String) {
        updateToken(token)
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("apptokencheck", "onMessageReceived: $appTokenList ")
        message.data["alertBody"]?.let { generateNotification(it,"Body") }
        val incomingAlertData = message.data["alertBody"]?.let {
            message.data["title"]?.let { it1 ->
                message.data["location"]?.let { it2 ->
                    message.data["senderName"]?.let { it3 ->
                        message.data["timestamp"]?.let { it4 ->
                            IncomingAlertData(
                                it3, it, it4, it1, it2
                            )
                        }
                    }
                }
            }
        }

        if (incomingAlertData != null) {
            incomingDataList.add(incomingAlertData)
        }
        Log.d("tiger", "onMessageReceived: $incomingDataList")
        saveIncomingAlertToDb(incomingDataList)
    }

    private fun updateToken(token: String) {
        val database = Firebase.firestore
        val collectionReference = database.collection("Users")
        var fAuth = Firebase.auth
        val user = fAuth.currentUser
        val currentUserId = user!!.uid
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

        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(0,builder.build())

    }


    private fun makeStatusNotification(message: String, context: Context) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val name = CHANNEL_NAME
            val description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

            notificationManager?.createNotificationChannel(channel)
        }


        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))


        NotificationManagerCompat.from(context).notify(123, builder.build())
    }

    private fun saveIncomingAlertToDb(incomingAlertDataList: MutableList<IncomingAlertData>){
        fAuth.currentUser?.uid?.let { myRef.child(it).setValue(incomingAlertDataList) }

    }


    }