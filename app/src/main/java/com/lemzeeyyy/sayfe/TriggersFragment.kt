package com.lemzeeyyy.sayfe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import com.lemzeeyyy.sayfe.database.SharedPrefs
import com.lemzeeyyy.sayfe.databinding.FragmentTriggersBinding


class TriggersFragment : Fragment() {
    lateinit var binding : FragmentTriggersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentTriggersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SharedPrefs.init(requireContext())
        binding.triggersRadioGroup.setOnCheckedChangeListener { group, checkedId ->
           if (binding.shakeTrigger.id == checkedId){
               SharedPrefs.putBoolean("shake",true)
           }
            else if (binding.screenTapTrigger.id == checkedId){
               SharedPrefs.putBoolean("tap",true)
           }else{
               SharedPrefs.putBoolean("volume",true)
           }
        }
    }

}