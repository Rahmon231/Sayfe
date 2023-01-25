package com.lemzeeyyy.sayfe.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.CheckedContactListener
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.adapters.PhonebookRecyclerAdapter
import com.lemzeeyyy.sayfe.databinding.ActivityMainBinding.bind
import com.lemzeeyyy.sayfe.databinding.AppBarMainBinding.bind
import com.lemzeeyyy.sayfe.databinding.FragmentPhoneBookBinding
import com.lemzeeyyy.sayfe.model.ContactsState
import com.lemzeeyyy.sayfe.model.GuardianData
import com.lemzeeyyy.sayfe.model.RecipientContact
import com.lemzeeyyy.sayfe.viewmodels.BUSY
import com.lemzeeyyy.sayfe.viewmodels.MainActivityViewModel
import com.lemzeeyyy.sayfe.viewmodels.PASSED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val REQUEST_CONTACT = 10
class PhoneBookFragment : Fragment(), CheckedContactListener {

    var contactList = emptyList<RecipientContact>()

    private val viewModel: MainActivityViewModel by activityViewModels()

    private lateinit var binding : FragmentPhoneBookBinding
    private lateinit var adapter : PhonebookRecyclerAdapter
    private lateinit var fAuth: FirebaseAuth
    private val database = Firebase.firestore
    private var list = mutableListOf<RecipientContact>()
    private val collectionReference = database.collection("Guardian Angels")
    private var isContained : Boolean = false
    private var duplicateNumber : String = ""


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
        adapter = PhonebookRecyclerAdapter(this,requireContext())
        binding.allContactsRecycler.adapter = adapter

        viewModel.getPhoneBook(requireActivity())

//        viewModel.userContactsLiveDataList.observe(viewLifecycleOwner){contactList->
//
//            adapter.updatePhonebookData(contactList!!.distinctBy {
//                it.name
//            })
        viewModel.userContactsLiveDataList.observe(viewLifecycleOwner) {
            when(it){
                is ContactsState.Empty ->{
                    binding.phoneBookEmptyState.visibility = View.VISIBLE
                    binding.addGuardianAngel.visibility = View.GONE
                    binding.allContactsRecycler.visibility = View.GONE
                }
                is ContactsState.Success ->{
                    binding.phoneBookEmptyState.visibility = View.GONE
                    binding.addGuardianAngel.visibility = View.VISIBLE
                    binding.allContactsRecycler.visibility = View.VISIBLE
                    contactList = it.contacts
                    adapter.updatePhonebookData(contactList.distinctBy { recipientData->
                        recipientData.name
                    })

                }
                else -> Unit

            }


        }
        binding.searchContactId.addTextChangedListener{substring->
            if (substring.toString().isBlank()){
                adapter.updatePhonebookData(contactList.distinctBy { distinctContact->
                    distinctContact.number
                })
                return@addTextChangedListener
            }
            val filteredContacts = contactList.filter {
                it.name.contains(substring.toString(),true)
            }
            adapter.updatePhonebookData(filteredContacts)

        }

    }

    private fun updateContactView(contactState: Int?) {
        if (contactState == null)
            return

        when(contactState){
            BUSY ->{
                binding.phoneBookEmptyState.visibility = View.VISIBLE
                binding.addGuardianAngel.visibility = View.GONE
                binding.allContactsRecycler.visibility = View.GONE
            }
            PASSED->{
                binding.phoneBookEmptyState.visibility = View.GONE
                binding.addGuardianAngel.visibility = View.VISIBLE
                binding.allContactsRecycler.visibility = View.VISIBLE
            }

        }

    }

    private fun saveGuardianAngelsListToDb(checkedList: MutableList<RecipientContact>) {
        val user = fAuth.currentUser
        val currentUserId = user!!.uid
        collectionReference.document(currentUserId)
            .get()
            .addOnSuccessListener {
                val data = it.toObject(GuardianData::class.java)
                collectionReference
                    .document(currentUserId)
                    .set(GuardianData(checkedList))
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(),"Guardian angels added successfully",Toast.LENGTH_SHORT)
                            .show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(),"Unable to add guardian angels",Toast.LENGTH_SHORT)
                            .show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(),it.message.toString(),Toast.LENGTH_SHORT).show()
            }
    }


    override fun onContactClick(contacts: MutableList<RecipientContact>) {
        binding.addGuardianAngel.setOnClickListener {
            //Check if the checked item is in the list
            //if true, dont add to the list(show a toast)
            //else, add to the list
            fAuth.currentUser?.uid?.let { it1 -> viewModel.getGuardianAngelsListFromDb(currentUserid = it1) }
            viewModel.guardianLiveData.observe(viewLifecycleOwner){
                val guardians = it.guardianInfo
                guardians.forEach { databaseGurdians ->
                    contacts.forEach { clickedContacts->
                        if (databaseGurdians.number == clickedContacts.number) {
                            duplicateNumber = clickedContacts.number
                            isContained = true
                        }
                    }
                }
             }
            if (isContained) {
                Toast.makeText(requireContext(), "$duplicateNumber has previously been added", Toast.LENGTH_SHORT).show()
                Toast.makeText(requireContext(), "Duplicate found, please uncheck contacts already in list", Toast.LENGTH_SHORT).show()
                isContained=false
            }else{
                saveGuardianAngelsListToDb(contacts)
            }

        }
    }


}