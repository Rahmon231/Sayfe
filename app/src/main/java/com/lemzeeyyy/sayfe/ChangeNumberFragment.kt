package com.lemzeeyyy.sayfe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.lemzeeyyy.sayfe.databinding.FragmentChangeNumberBinding
import com.lemzeeyyy.sayfe.repository.SayfeRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ChangeNumberFragment : Fragment() {
    private lateinit var binding : FragmentChangeNumberBinding
    @Inject
    lateinit var repository: SayfeRepository
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentChangeNumberBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.savePhoneNum.setOnClickListener {
            val phoneString = binding.phoneNumberEtChange.text.toString()
            val countryCode = binding.countryCodeEtChange.selectedCountryCode.toString()
            val phoneNumber = "$countryCode$phoneString"
            viewLifecycleOwner.lifecycleScope.launch {
                if (repository.checkDuplicateRegisteredNumber(phoneNumber)){
                    Toast.makeText(requireContext(),"This number has already been registered by a user",Toast.LENGTH_SHORT).show()
                    return@launch
                }
                updatePhoneNumber(phoneNumber)
            }

        }

    }

    private fun updatePhoneNumber(phoneNumber: String){
        viewLifecycleOwner.lifecycleScope.launch {
            if (repository.changePhoneNumber(phoneNumber)){
                Toast.makeText(requireContext(),"Phone Number Updated Successfully",Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            }
        }

    }
}