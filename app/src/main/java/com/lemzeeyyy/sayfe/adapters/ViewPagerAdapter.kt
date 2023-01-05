package com.lemzeeyyy.sayfe.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lemzeeyyy.sayfe.fragments.IncomingAlertFragment
import com.lemzeeyyy.sayfe.fragments.OutgoingAlertFragment

class ViewPagerAdapter(fragment : Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2;
    }

    override fun createFragment(position: Int): Fragment {
        when(position){
            0 -> return IncomingAlertFragment()
            1 -> return OutgoingAlertFragment()
            else -> return IncomingAlertFragment()
        }
    }
}