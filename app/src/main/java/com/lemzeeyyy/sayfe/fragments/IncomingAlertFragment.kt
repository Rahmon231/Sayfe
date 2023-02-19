package com.lemzeeyyy.sayfe.fragments

import android.os.Binder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.NotificationBodyClickListener
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.adapters.IncomingAlertsRecyclerAdapter
import com.lemzeeyyy.sayfe.adapters.OutgoingAlertsRecyclerAdapter
import com.lemzeeyyy.sayfe.databinding.FragmentIncomingAlertBinding
import com.lemzeeyyy.sayfe.model.IncomingAlertData
import com.lemzeeyyy.sayfe.model.OutgoingAlertData
import com.lemzeeyyy.sayfe.model.Users
import com.lemzeeyyy.sayfe.service.AccessibilityKeyDetector
import com.lemzeeyyy.sayfe.service.AccessibilityKeyDetector.Companion.alertTriggerId
import com.lemzeeyyy.sayfe.service.AccessibilityKeyDetector.Companion.appTokenList
import com.lemzeeyyy.sayfe.viewmodels.*


class IncomingAlertFragment : Fragment(),NotificationBodyClickListener {

    private lateinit var binding : FragmentIncomingAlertBinding
    private lateinit var incomingAlertsRecyclerAdapter: IncomingAlertsRecyclerAdapter
    private val incomingAlertDb = Firebase.database
    private var fAuth = Firebase.auth
    private val database = Firebase.firestore
    private val viewModel: MainActivityViewModel by activityViewModels()

    private val myRef = incomingAlertDb.getReference("IncomingAlerts")
    private lateinit var notificationBodyListener : NotificationBodyClickListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentIncomingAlertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fAuth = Firebase.auth
        val currentUser = fAuth.currentUser
        val currentUserId = currentUser?.uid
        notificationBodyListener = this
        incomingAlertsRecyclerAdapter = IncomingAlertsRecyclerAdapter(notificationBodyListener)
        binding.incomingRecycler.adapter = incomingAlertsRecyclerAdapter

        binding.reloadIncomingFailedState.setOnClickListener {
            Log.d("TAG", "reloadPage: Reloading.......")
        }

        viewModel.incomingDataList()
        viewModel.incomingDataStatus.observe(viewLifecycleOwner){
            updateViewForIncomingAlerts(it)
        }
        viewModel.incomingDataList()
        viewModel.incomingAlertListLiveData.observe(viewLifecycleOwner){incomingAlertList->
            if (incomingAlertList!=null){
                if (incomingAlertList.size == 0){
//                    binding.emptyIncomingAlert.visibility = View.VISIBLE
                    return@observe
                }
//                binding.emptyIncomingAlert.visibility = View.INVISIBLE
                incomingAlertsRecyclerAdapter.updateDataList(incomingAlertList)
            }

        }

//        myRef.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//               //Update recyclerView for only targeted appID
//                //update recycler view of devices with the uid in child of incoming alerts
//                snapshot.children.forEach {
//                    val incomingDataList = it.getValue<MutableList<IncomingAlertData>>()
//                    if (currentUserId == it.key){
//                        if (incomingDataList != null) {
//                            incomingAlertsRecyclerAdapter.updateDataList(incomingDataList)
//                        }
//                    }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(requireContext(),error.message.toString(),Toast.LENGTH_SHORT).show()
//            }
//        })
    }

    private fun updateViewForIncomingAlerts(contactState: Int?) {
        if (contactState == null)
            return

        when(contactState){
            BUSY -> {
                binding.emptyIncomingAlert.visibility = View.INVISIBLE
                binding.incomingRecycler.visibility = View.INVISIBLE
                binding.loadingIncoming.visibility = View.VISIBLE
                binding.incomingFailedImg.visibility = View.INVISIBLE
                binding.reloadIncomingFailedState.visibility =View.INVISIBLE
            }
            EMPTY ->{
                binding.emptyIncomingAlert.visibility = View.VISIBLE
                binding.incomingRecycler.visibility = View.INVISIBLE
                binding.loadingIncoming.visibility = View.INVISIBLE
                binding.incomingFailedImg.visibility = View.INVISIBLE
                binding.reloadIncomingFailedState.visibility =View.INVISIBLE
            }
            PASSED ->{
                binding.incomingRecycler.visibility = View.VISIBLE
                binding.emptyIncomingAlert.visibility = View.INVISIBLE
                binding.loadingIncoming.visibility = View.INVISIBLE
                binding.incomingFailedImg.visibility = View.INVISIBLE
                binding.reloadIncomingFailedState.visibility =View.INVISIBLE
            }
            FAILED ->{
                binding.incomingRecycler.visibility = View.INVISIBLE
                binding.emptyIncomingAlert.visibility = View.INVISIBLE
                binding.loadingIncoming.visibility = View.INVISIBLE
                binding.incomingFailedImg.visibility = View.VISIBLE
                binding.reloadIncomingFailedState.visibility =View.VISIBLE
            }
        }

    }

    private fun reloadPage(){

    }

    override fun onNotificationBodyClick(view: View, alertBody : String) {
        val action = ActivitiesFragmentDirections.actionIncomimgAlertFragmentToWebViewFragment(alertBody)
        findNavController().navigate(action)

    }

}