package com.lemzeeyyy.sayfe.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.viewmodels.MainActivityViewModel
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.activities.PERMISSION_REQUEST
import com.lemzeeyyy.sayfe.database.SharedPrefs
import com.lemzeeyyy.sayfe.databinding.FragmentDashboardBinding
import com.lemzeeyyy.sayfe.service.AccessibilityKeyDetector.Companion.outgoingDataList
import java.util.*
import kotlin.math.sqrt

const val LOCATION_PERMISSION = 2001

class DashboardFragment : Fragment() {
    private lateinit var binding: FragmentDashboardBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var fAuth: FirebaseAuth

    // private var imageUri : Uri = Uri.EMPTY
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var locationUrl: String = ""
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

        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.userNameHome.setText(outgoingDataList.toString())






        SharedPrefs.init(requireContext())

        if (SharedPrefs.getBoolean("shake", false)) {
            binding.triggerModeText.setText("Shake your phone to trigger Sayfe")
        } else if (SharedPrefs.getBoolean("tap", false)) {
            binding.triggerModeText.setText("Tap Screen twice to trigger sayfe")
        } else if (SharedPrefs.getBoolean("volume",false)){
            binding.triggerModeText.setText("Press volume up button twice to trigger sayfe")
        }
        else {
            binding.triggerModeText.setText("Press volume up button twice to trigger sayfe")
        }


        fAuth = Firebase.auth
        val user = fAuth.currentUser
        val currentUserId = user!!.uid

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        getCurrentLocation()


        viewModel.getImageUriFromDb(currentUserId)
        viewModel.userImageUri.observe(viewLifecycleOwner) {

            if (it.equals(Uri.EMPTY)) {
                binding.profileImage.setImageResource(R.drawable.profile_image)
            } else {
                Glide.with(requireActivity()).load(it).into(binding.profileImage)
            }

        }

        binding.alertBeneficiaryDashboard.setOnClickListener {
            findNavController().navigate(R.id.guardianAngelsFragment)

        }

        binding.triggersDashboard.setOnClickListener {
            findNavController().navigate(R.id.triggersFragment)
        }

        binding.sosTextsDashboard.setOnClickListener {
            findNavController().navigate(R.id.sosTextFragment)
        }


        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(
                sensorListener, sensorManager!!
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
            )

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH
    }



    private fun getCurrentLocation() {
        if (checkPermissions()) {
            if (locationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val location: Location? = task.result
                        if (location == null) {
//                            Toast.makeText(
//                                requireContext(),
//                                "Location is Null ke",
//                                Toast.LENGTH_SHORT
//                            ).show()
                        } else {
                            longitude = location.longitude
                            latitude = location.latitude
                            locationUrl =
                                "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"

                            Log.d("check locayu", "getCurrentLocation: $locationUrl ")
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            task.exception?.message.toString(),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            } else {
                //go to settings.....
                Toast.makeText(requireContext(), "Turn on location", Toast.LENGTH_SHORT).show()
                val locationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(locationIntent)
            }
        } else {
            //request permission
            requestPermission()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_REQUEST
        )
    }

    private fun locationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(requireContext(), "Granted", Toast.LENGTH_SHORT).show()
            getCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "Denied", Toast.LENGTH_SHORT).show()
        }
    }





     fun sendSMSSOS() {
        val currentUserId = fAuth.currentUser!!.uid
        try {

            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= 23) {

                requireActivity().getSystemService(SmsManager::class.java)
            } else {

                SmsManager.getDefault()
            }

            viewModel.getGuardianAngelsListFromDb(currentUserId)
            viewModel.guardianLiveData.observe(viewLifecycleOwner) {
                val dataList = it.guardianInfo

                dataList.forEach { recipientContact ->

                    val guardianPhone = recipientContact.number

                    smsManager.sendTextMessage(
                        guardianPhone,
                        null,
                        "SOS! \n$locationUrl",
                        null,
                        null
                    )
                }
            }

            Toast.makeText(requireContext(), "Message Sent", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_LONG)
                .show()
        }
    }

    fun triggerSayfe() {
        SharedPrefs.init(requireContext())
        val shakeTrigger = SharedPrefs.getBoolean("shake", false)
        val volumeTrigger = SharedPrefs.getBoolean("volume", false)
        val tapTrigger = SharedPrefs.getBoolean("tap", false)

        if (shakeTrigger) {
            sendSMSSOS()
            return
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
                Toast.makeText(requireContext(), "Shake event detected", Toast.LENGTH_SHORT).show()
                triggerSayfe()
                return
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

        }
    }

    override fun onResume() {
        sensorManager?.registerListener(
            sensorListener, sensorManager!!.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
            ), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        // fAuth = Firebase.auth
//        val user = fAuth.currentUser
//        val currentUserId = user!!.uid
//        viewModel.getImageUriFromDb(currentUserId)
//        viewModel.userImageUri.observe(viewLifecycleOwner){
//
//            if (it.equals(Uri.EMPTY)){
//                binding.profileImage.setImageResource(R.drawable.profile_image)
//            }else{
//                Glide.with(requireActivity()).load(it).into(binding.profileImage)
//            }
//
//        }
        super.onPause()
    }

}
