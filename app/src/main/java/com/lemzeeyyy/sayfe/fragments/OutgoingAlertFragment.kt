package com.lemzeeyyy.sayfe.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.adapters.OutgoingAlertsRecyclerAdapter
import com.lemzeeyyy.sayfe.databinding.FragmentOutgoingAlertBinding
import com.lemzeeyyy.sayfe.service.AccessibilityKeyDetector.Companion.outgoingDataList

class OutgoingAlertFragment : Fragment() {
    private lateinit var binding : FragmentOutgoingAlertBinding
    private lateinit var outgoingAlertsRecyclerAdapter: OutgoingAlertsRecyclerAdapter

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
        outgoingAlertsRecyclerAdapter = OutgoingAlertsRecyclerAdapter()
        binding.outgoingRecycler.adapter = outgoingAlertsRecyclerAdapter
        outgoingAlertsRecyclerAdapter.updateDataList(outgoingDataList)
        Log.d("OUTDATA", "onViewCreated: ${outgoingDataList.toString()}")

    }

}