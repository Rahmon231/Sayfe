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
import com.lemzeeyyy.sayfe.viewmodels.*


class OutgoingAlertFragment : Fragment(), NotificationBodyClickListener {
    private lateinit var binding : FragmentOutgoingAlertBinding
    private lateinit var outgoingAlertsRecyclerAdapter: OutgoingAlertsRecyclerAdapter
    private val fAuth = Firebase.auth
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
        outgoingAlertsRecyclerAdapter = OutgoingAlertsRecyclerAdapter(notificationBodyListener)
        binding.outgoingRecycler.adapter = outgoingAlertsRecyclerAdapter

        viewModel.outgoingDataList()
        viewModel.outgoingDataStatus.observe(viewLifecycleOwner){
            updateViewForOutgoingState(it)
        }

        viewModel.outgoingAlertListLiveData.observe(viewLifecycleOwner){outgoingDataList->
            if (outgoingDataList != null) {
                if (outgoingDataList.size == 0 ){
                    //binding.emptyOutgoingAlert.visibility = View.VISIBLE
                    return@observe
                }
                outgoingAlertsRecyclerAdapter.updateDataList(outgoingDataList)
            }

        }

    }

    private fun updateViewForOutgoingState(contactState: Int?) {
            if (contactState == null)
                return

            when(contactState){
                BUSY -> {
                    binding.emptyOutgoingAlert.visibility = View.INVISIBLE
                    binding.outgoingRecycler.visibility = View.INVISIBLE
                    binding.loadingOutgoing.visibility = View.VISIBLE
                }
                EMPTY ->{
                    binding.emptyOutgoingAlert.visibility = View.VISIBLE
                    binding.outgoingRecycler.visibility = View.INVISIBLE
                    binding.loadingOutgoing.visibility = View.INVISIBLE
                }
                PASSED ->{
                    binding.outgoingRecycler.visibility = View.VISIBLE
                    binding.emptyOutgoingAlert.visibility = View.INVISIBLE
                    binding.loadingOutgoing.visibility = View.INVISIBLE
                }
                FAILED ->{
                    binding.emptyOutgoingAlert.visibility = View.INVISIBLE
                    binding.outgoingRecycler.visibility = View.INVISIBLE
                    binding.loadingOutgoing.visibility = View.INVISIBLE
                }
            }

    }

    override fun onNotificationBodyClick(view: View,alertBody : String) {
        val action = ActivitiesFragmentDirections.actionIncomimgAlertFragmentToWebViewFragment(alertBody)
        findNavController().navigate(action)
    }

}