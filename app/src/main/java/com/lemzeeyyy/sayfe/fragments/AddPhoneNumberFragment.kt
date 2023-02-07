package com.lemzeeyyy.sayfe.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.databinding.FragmentAddPhoneNumberBinding
import com.lemzeeyyy.sayfe.model.Users
import com.lemzeeyyy.sayfe.viewmodels.MainActivityViewModel


class AddPhoneNumberFragment : Fragment() {

    private lateinit var binding : FragmentAddPhoneNumberBinding
    private lateinit var fAuth: FirebaseAuth
    private val database = Firebase.firestore
    private val collectionReference = database.collection("Users")
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
        fAuth = Firebase.auth
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
        val user = fAuth.currentUser
         val currentUserId = user?.uid

        collectionReference.whereEqualTo("currentUserId",currentUserId)
            .addSnapshotListener { value, error ->
                if(!value!!.isEmpty){
                    for( snapshot : QueryDocumentSnapshot in value){
                        val users = snapshot.toObject(Users::class.java)
                        users.phoneNumber = phone.filter {
                            !it.isWhitespace()
                        }
                        users.number = users.phoneNumber.filter {
                            !it.isWhitespace()
                        }.takeLast(10)
                        users.countryCode = countryCode
                        collectionReference.document(snapshot.id)
                            .set(users)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(),"Phone Number Added Successfully",Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(),it.message.toString(),Toast.LENGTH_LONG).show()
                            }
                    }

                }
            }
        findNavController().navigate(R.id.nav_home)
    }

    override fun onResume() {
        super.onResume()
        Log.d("RESUME TAG", "onResume: Called?")
    }

}