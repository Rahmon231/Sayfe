package com.lemzeeyyy.sayfe

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.databinding.FragmentDashboardBinding
import com.lemzeeyyy.sayfe.model.RecipientDataInfo
import java.util.*
import kotlin.math.sqrt

const val PERMISSION_REQUEST = 101

class DashboardFragment : Fragment() {
    private lateinit var binding : FragmentDashboardBinding
    private val database = Firebase.firestore
    private lateinit var fAuth: FirebaseAuth
    private val collectionReference = database.collection("Users")
    private val recipientCollectionReference = database.collection("RecipientsDatabase")
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    private var exitAppToastStillShowing = false

    private val exitAppTimer = object : CountDownTimer(2000, 1000) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
            exitAppToastStillShowing = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backPressedCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            exitApp()
        }
        backPressedCallback.isEnabled = true
    }

    private fun exitApp() {
        if (exitAppToastStillShowing) {
            requireActivity().finish()
            return
        }

        Toast.makeText(this.requireContext(), getString(R.string.exit), Toast.LENGTH_SHORT)
            .show()
        exitAppToastStillShowing = true
        exitAppTimer.start()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val navView: BottomNavigationView = binding.navViewBtm

        val navController = requireActivity().findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_activities, R.id.nav_phonebook,R.id.nav_settings
            )
        )
        // setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        fAuth = Firebase.auth
       // saveRecipientData()
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(sensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH
    }

    private fun saveRecipientData() {
        val user = fAuth.currentUser
        val currentUserId = user!!.uid

        val recipientDataInfo = RecipientDataInfo("123456","lemzyyy@gmail.com",currentUserId)

        recipientCollectionReference
            .document()
            .set(recipientDataInfo)
            .addOnSuccessListener {
                Toast.makeText(requireContext(),"Contact added successfully",Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(),it.message.toString(),Toast.LENGTH_LONG).show()
            }


    }

    private fun updateRecipientData() {
        val user = fAuth.currentUser
        val currentUserId = user!!.uid
        val recipientDataInfo = hashMapOf(
            "recipientPhoneNumber" to "",
            "recipientEmail" to ""
        )
        recipientCollectionReference.whereEqualTo("userid",currentUserId)
            .addSnapshotListener { value, error ->
                if(!value!!.isEmpty){
                    for( snapshot : QueryDocumentSnapshot in value){
                        val myObject = snapshot.toObject(RecipientDataInfo::class.java)
                        recipientCollectionReference.document(snapshot.id)
                            .update("recipientPhoneNumber","123")

                    }
                }
            }

    }

    private fun smsPermissionRequest() {
        val permissionCheck = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.SEND_SMS)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            sendSMSSOS()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.SEND_SMS),
                PERMISSION_REQUEST)
        }
    }


    private fun requestPermission() {
        val readContactPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS)
        val writeContactPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS)
        val messagePermissionCheck = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.SEND_SMS)

        if (readContactPermission == PackageManager.PERMISSION_GRANTED && writeContactPermission ==  PackageManager.PERMISSION_GRANTED
            && messagePermissionCheck == PackageManager.PERMISSION_GRANTED )
        {

        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.WRITE_CONTACTS, android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.SEND_SMS),
                PERMISSION_REQUEST)
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
               Toast.makeText(requireContext(),"Permission Granted successfully",Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "You don't have required permission to send a message",
                    Toast.LENGTH_SHORT).show();
            }
        }
    }



    private fun sendSMSSOS(){
        try {

            val smsManager: SmsManager = if (Build.VERSION.SDK_INT>=23) {
                //if SDK is greater that or equal to 23 then
                //this is how we will initialize the SmsManager
                requireActivity().getSystemService(SmsManager::class.java)
            } else{
                //if user's SDK is less than 23 then
                //SmsManager will be initialized like this
                SmsManager.getDefault()
            }

            smsManager.sendTextMessage("+2349035637518", null, "Sayfe", null, null)

            // on below line we are displaying a toast message for message send,
            Toast.makeText(requireContext(), "Message Sent", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            // on catch block we are displaying toast message for error.
            Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_LONG)
                .show()
        }
    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            // Fetching x,y,z values
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration

            // Getting current accelerations
            // with the help of fetched x,y,z values
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            if (acceleration > 12) {
                //smsPermissionRequest()
                Toast.makeText(requireContext(), "Shake event detected", Toast.LENGTH_SHORT).show()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

        }
    }

    override fun onResume() {
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requestPermission()
    }
}