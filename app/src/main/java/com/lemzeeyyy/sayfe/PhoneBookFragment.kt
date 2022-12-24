package com.lemzeeyyy.sayfe

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.HomeFragment.Companion.recipientContacts
import com.lemzeeyyy.sayfe.PhonebookRecyclerAdapter.Companion.checkedList
import com.lemzeeyyy.sayfe.databinding.FragmentPhoneBookBinding
import com.lemzeeyyy.sayfe.model.GuardianData
import com.lemzeeyyy.sayfe.model.RecipientContact

class PhoneBookFragment : Fragment() {
    private lateinit var binding : FragmentPhoneBookBinding
    private lateinit var adapter : PhonebookRecyclerAdapter
    private lateinit var fAuth: FirebaseAuth
    private val database = Firebase.firestore
    private val collectionReference = database.collection("Guardian Angels")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentPhoneBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fAuth = Firebase.auth

        adapter = PhonebookRecyclerAdapter()
        adapter.updatePhonebookData(phoneBookData = recipientContacts)

        binding.backArrowContactList.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.nav_home)
        }

        binding.allContactsRecycler.adapter = adapter

        binding.addGuardianAngel.setOnClickListener {
            saveGuardianAngelsListToDb(checkedList)
            Navigation.findNavController(view).navigate(R.id.nav_home)
        }

    }

    private fun saveGuardianAngelsListToDb(checkedList: MutableList<RecipientContact>) {

        val user = fAuth.currentUser
        val currentUserId = user!!.uid
        val docData = GuardianData(checkedList)

        collectionReference
            .document(currentUserId)
            .set(docData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(),"Guardian Angels Added Successfully",Toast.LENGTH_LONG)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(),it.message.toString(),Toast.LENGTH_LONG)
                    .show()
            }

    }

}