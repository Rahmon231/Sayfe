package com.lemzeeyyy.sayfe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.lemzeeyyy.sayfe.databinding.FragmentAccountSettingsBinding

class AccountSettings : Fragment() {
    private lateinit var binding : FragmentAccountSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentAccountSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.changeNumberRel.setOnClickListener {
            findNavController().navigate(R.id.changeNumberFragment)
        }
        binding.deleteMyAcct.setOnClickListener {

        }



    }

}