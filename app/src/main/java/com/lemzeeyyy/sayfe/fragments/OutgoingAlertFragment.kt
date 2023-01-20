package com.lemzeeyyy.sayfe.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.NotificationBodyClickListener
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.adapters.OutgoingAlertsRecyclerAdapter
import com.lemzeeyyy.sayfe.databinding.FragmentOutgoingAlertBinding
import com.lemzeeyyy.sayfe.model.OutgoingAlertData
import com.lemzeeyyy.sayfe.service.AccessibilityKeyDetector
import com.lemzeeyyy.sayfe.service.AccessibilityKeyDetector.Companion.alertTriggerId
import com.lemzeeyyy.sayfe.viewmodels.MainActivityViewModel


class OutgoingAlertFragment : Fragment(), NotificationBodyClickListener {
    private lateinit var binding : FragmentOutgoingAlertBinding
    private lateinit var outgoingAlertsRecyclerAdapter: OutgoingAlertsRecyclerAdapter
    private val outgoingAlertDb = Firebase.database
    private val fAuth = Firebase.auth
    private val myRef = outgoingAlertDb.getReference("OutgoingAlerts")
    private lateinit var notificationBodyListener : NotificationBodyClickListener
    private val viewModel: MainActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       binding = FragmentOutgoingAlertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notificationBodyListener = this
        val currentUserId = fAuth.currentUser?.uid
        outgoingAlertsRecyclerAdapter = OutgoingAlertsRecyclerAdapter(notificationBodyListener)
        binding.outgoingRecycler.adapter = outgoingAlertsRecyclerAdapter

//        myRef.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//
//                snapshot.children.forEach {
//
//                    val outgoingDataList = it.getValue<MutableList<OutgoingAlertData>>()!!
//
//                    Log.d("Trigger Id", "onDataChange: $alertTriggerId")
//                    if(currentUserId == alertTriggerId){
//                        outgoingAlertsRecyclerAdapter.updateDataList(outgoingDataList)
//                    }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.d("TAG", "onCancelled: ${error.message.toString()} ")
//            }
//        }
//        )
        viewModel.getOutgoingAlertList()
        viewModel.outgoingAlertListLiveData.observe(viewLifecycleOwner){
            Log.d("TAG", "onViewCreated: $alertTriggerId ")
            if(currentUserId == alertTriggerId){
                if (it != null) {
                    outgoingAlertsRecyclerAdapter.updateDataList(it)
                }
            }
        }
    }

    override fun onNotificationBodyClick(view: View,alertBody : String) {
        val action = ActivitiesFragmentDirections.actionIncomimgAlertFragmentToWebViewFragment(alertBody)

        Log.d("ALertBodyCheck", "onNotificationBodyClick: ${alertBody.toString()}")
        findNavController().navigate(action)
    }

}