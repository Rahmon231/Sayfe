package com.lemzeeyyy.sayfe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.databinding.FragmentChangeNumberBinding
import com.lemzeeyyy.sayfe.model.Users


class ChangeNumberFragment : Fragment() {
    private lateinit var binding : FragmentChangeNumberBinding
    private lateinit var fAuth: FirebaseAuth
    private val database = Firebase.firestore
    private val collectionReference = database.collection("Users")
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
        fAuth = Firebase.auth
        binding.savePhoneNum.setOnClickListener {
            val phoneString = binding.phoneNumberEtChange.text!!.toString()
            val countryCode = binding.countryCodeEtChange.selectedCountryCode.toString()
            val phoneNumber = "$countryCode$phoneString"
            savePhoneNumber(view,phoneNumber)
        }

    }

    private fun savePhoneNumber(view: View, phoneNumber: String) {
        val user = fAuth.currentUser
        val currentUserId = user!!.uid
        collectionReference.whereEqualTo("currentUserId",currentUserId)
            .addSnapshotListener { value, error ->
                if(!value!!.isEmpty)
                    for (snapshot : QueryDocumentSnapshot in value){
                            val users = snapshot.toObject(Users::class.java)
                        val newNumber = phoneNumber.filter {
                            !it.isWhitespace()
                        }
                        val number = newNumber.takeLast(10)
                        users.phoneNumber = newNumber
                        users.number = number

                            collectionReference.document(snapshot.id)
                                .set(users)
                                .addOnSuccessListener {
                                    Toast.makeText(view.context,"Phone Number Updated Successfully",Toast.LENGTH_LONG).show()
                                    findNavController().navigateUp()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(view.context,it.toString(),Toast.LENGTH_SHORT).show()
                                }
                        }
            }


    }
}