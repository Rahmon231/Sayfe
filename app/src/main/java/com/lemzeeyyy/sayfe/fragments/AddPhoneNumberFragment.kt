package com.lemzeeyyy.sayfe.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.databinding.FragmentAddPhoneNumberBinding
import com.lemzeeyyy.sayfe.repository.SayfeRepository
import com.lemzeeyyy.sayfe.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddPhoneNumberFragment : Fragment() {

    private lateinit var binding : FragmentAddPhoneNumberBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private var isDuplicate = false
    private var phoneNumber = ""
    private var countryCode = ""
    @Inject
    lateinit var repository: SayfeRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddPhoneNumberBinding.inflate(inflater,container,false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setupLaterTxt.setOnClickListener {
            findNavController().navigate(R.id.nav_home)
        }


        binding.continueAddPhone.setOnClickListener {
            val phoneString = binding.phoneNumberEt.text.toString()
            countryCode = binding.countryCodeEt.selectedCountryCode.toString()
            phoneNumber = "+$countryCode$phoneString"


            viewModel.checkDuplicateRegisteredNumber(phoneNumber)

        }

        viewModel.isRegistered.observe(viewLifecycleOwner){
            isDuplicate = it
            if (isDuplicate){
                Toast.makeText(requireContext(),"A user has registered with this number already, kindly register with a new number",
                Toast.LENGTH_SHORT)
                    .show()
            }
            else{
               savePhoneNumber(phoneNumber,countryCode)
            }

           }

    }


    private fun savePhoneNumber(phone: String,countryCode : String) {
        viewModel.savePhoneNumber(phone, countryCode)
        viewModel.saveNumber.observe(viewLifecycleOwner){
            if (it==null)
                return@observe
            if (it){
                toastMessage("Phone Number Added Successfully")
                findNavController().navigate(R.id.nav_home)
            }
        }
//        viewLifecycleOwner.lifecycleScope.launch {
//           if ( repository.savePhoneNumber(phone,countryCode)) {
//               toastMessage("Phone Number Added Successfully")
//               findNavController().navigate(R.id.nav_home)
//           }
//        }
    }

    private fun toastMessage(s: String) {
        Toast.makeText(requireContext(),s,Toast.LENGTH_SHORT).show()
    }


}