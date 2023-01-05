package com.lemzeeyyy.sayfe.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.CheckedContactListener
import com.lemzeeyyy.sayfe.adapters.PhonebookRecyclerAdapter
import com.lemzeeyyy.sayfe.databinding.FragmentPhoneBookBinding
import com.lemzeeyyy.sayfe.model.GuardianData
import com.lemzeeyyy.sayfe.model.RecipientContact
import com.lemzeeyyy.sayfe.viewmodels.MainActivityViewModel

const val REQUEST_CONTACT = 10
class PhoneBookFragment : Fragment(), CheckedContactListener {

    companion object{
        var recipientContacts : MutableList<RecipientContact> = ArrayList()
    }

    private val viewModel: MainActivityViewModel by activityViewModels()

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

        adapter = PhonebookRecyclerAdapter(this)

        viewModel.getPhoneBook()
        viewModel.userContactsLiveDataList.observe(viewLifecycleOwner){contactList->
            adapter.updatePhonebookData(contactList!!.distinctBy {
                it.name
            })
            binding.searchContactId.addTextChangedListener{substring->
                if (substring.toString().isBlank()){
                    adapter.updatePhonebookData(contactList.distinctBy { distinctContact->
                        distinctContact.number
                    })
                    return@addTextChangedListener
                }
                val filteredContacts = contactList.filter {
                    it.name.startsWith(substring.toString(),true)
                }
                adapter.updatePhonebookData(filteredContacts)

            }
        }

        binding.allContactsRecycler.adapter = adapter

    }

    private fun saveGuardianAngelsListToDb(checkedList: MutableList<RecipientContact>) {
        val user = fAuth.currentUser
        val currentUserId = user!!.uid
        val docData = GuardianData(checkedList)

        collectionReference
            .document(currentUserId)
            .set(docData)
            .addOnSuccessListener {

            }
            .addOnFailureListener {

            }
    }



    override fun onContactClick(contacts: MutableList<RecipientContact>) {
        binding.addGuardianAngel.setOnClickListener {
            saveGuardianAngelsListToDb(contacts)
            Toast.makeText(requireContext(),"Guardian Angles Contacts Saved",Toast.LENGTH_LONG).show()
        }
    }

}