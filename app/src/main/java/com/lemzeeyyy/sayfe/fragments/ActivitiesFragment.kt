package com.lemzeeyyy.sayfe.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.lemzeeyyy.sayfe.adapters.OutgoingAlertsRecyclerAdapter
import com.lemzeeyyy.sayfe.adapters.ViewPagerAdapter
import com.lemzeeyyy.sayfe.databinding.FragmentActivitiesBinding
import com.lemzeeyyy.sayfe.service.AccessibilityKeyDetector
import com.lemzeeyyy.sayfe.viewmodels.MainActivityViewModel


class ActivitiesFragment : Fragment() {
   private lateinit var binding : FragmentActivitiesBinding
    private val tab_items = arrayOf("Incoming Alerts", "Outgoing Alerts")
    private val viewModel: MainActivityViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.viewPager.adapter = ViewPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout,binding.viewPager){
                tab,position ->
            tab.text = tab_items[position]
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateShouldNavActivity(false)
    }
}
