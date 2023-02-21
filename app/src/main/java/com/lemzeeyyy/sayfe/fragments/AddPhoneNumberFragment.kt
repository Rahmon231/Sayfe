package com.lemzeeyyy.sayfe.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.databinding.FragmentAddPhoneNumberBinding
import com.lemzeeyyy.sayfe.model.Users
import com.lemzeeyyy.sayfe.repository.SayfeRepository
import com.lemzeeyyy.sayfe.viewmodels.MainActivityViewModel
import kotlinx.coroutines.launch


class AddPhoneNumberFragment : Fragment() {

    private lateinit var binding : FragmentAddPhoneNumberBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private var isDuplicate = false
    private var phoneNumber = ""
    private var countryCode = ""

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
        viewLifecycleOwner.lifecycleScope.launch {
           if ( SayfeRepository.savePhoneNumber(phone,countryCode)) {
               toastMessage("Phone Number Added Successfully")
               findNavController().navigate(R.id.nav_home)
           }
        }
    }

    private fun toastMessage(s: String) {
        Toast.makeText(requireContext(),s,Toast.LENGTH_SHORT).show()
    }


}