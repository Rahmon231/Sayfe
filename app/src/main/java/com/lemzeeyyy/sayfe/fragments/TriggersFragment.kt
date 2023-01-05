package com.lemzeeyyy.sayfe.fragments

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

       if(SharedPrefs.getBoolean("shake",false)){
           binding.shakeTrigger.isChecked = true
           SharedPrefs.putBoolean("shake",true)
           SharedPrefs.putBoolean("volume",false)
           SharedPrefs.putBoolean("tap",false)

       }else if(SharedPrefs.getBoolean("tap",false)){
           binding.screenTapTrigger.isChecked = true
           SharedPrefs.putBoolean("tap",true)
           SharedPrefs.putBoolean("shake",false)
           SharedPrefs.putBoolean("volume",false)

       }else{
           binding.volumeTrigger.isChecked = true
           SharedPrefs.putBoolean("volume",true)
           SharedPrefs.putBoolean("shake",false)
           SharedPrefs.putBoolean("tap",false)
       }

        binding.triggersRadioGroup.setOnCheckedChangeListener { group, checkedId ->
           if (binding.shakeTrigger.id == checkedId){
               SharedPrefs.putBoolean("shake",true)
               SharedPrefs.putBoolean("volume",false)
               SharedPrefs.putBoolean("tap",false)

           }
            else if (binding.screenTapTrigger.id == checkedId){
               SharedPrefs.putBoolean("tap",true)
               SharedPrefs.putBoolean("shake",false)
               SharedPrefs.putBoolean("volume",false)
           }else if (binding.volumeTrigger.id == checkedId){
               SharedPrefs.putBoolean("volume",true)
               SharedPrefs.putBoolean("shake",false)
               SharedPrefs.putBoolean("tap",false)
           }
            else{
               SharedPrefs.putBoolean("volume",true)
               SharedPrefs.putBoolean("shake",false)
               SharedPrefs.putBoolean("tap",false)
           }
        }
    }

}