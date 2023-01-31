package com.lemzeeyyy.sayfe.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.CheckedContactListener
import com.lemzeeyyy.sayfe.activities.MainActivity
import com.lemzeeyyy.sayfe.activities.PERMISSION_REQUEST
import com.lemzeeyyy.sayfe.adapters.PhonebookRecyclerAdapter
import com.lemzeeyyy.sayfe.databinding.FragmentPhoneBookBinding
import com.lemzeeyyy.sayfe.model.*
import com.lemzeeyyy.sayfe.viewmodels.BUSY
import com.lemzeeyyy.sayfe.viewmodels.MainActivityViewModel
import com.lemzeeyyy.sayfe.viewmodels.PASSED

const val REQUEST_CONTACT = 10
class PhoneBookFragment : Fragment(), CheckedContactListener {

    private var contactList = emptyList<PhonebookContact>()

    private val viewModel: MainActivityViewModel by activityViewModels()

    private lateinit var binding : FragmentPhoneBookBinding
    private lateinit var adapter : PhonebookRecyclerAdapter
    private lateinit var fAuth: FirebaseAuth
    private val database = Firebase.firestore
    private val collectionReference = database.collection("Guardian Angels")
    private var isDuplicate : Boolean = false
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
        binding.addGuardianAngel.setOnClickListener {
            adapter.triggerCheckedListInterface()
        }
        binding.allContactsRecycler.adapter = adapter

        val readContactPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS)


        if (readContactPermission == PackageManager.PERMISSION_GRANTED )
        {
            viewModel.getPhoneBook(requireActivity())
            viewModel.userContactsLiveDataList.observe(viewLifecycleOwner) {
                when(it){
                    is ContactsState.Empty ->{
                        binding.phoneBookEmptyState.visibility = View.GONE
                        binding.addGuardianAngel.visibility = View.VISIBLE
                        binding.allContactsRecycler.visibility = View.GONE
                        binding.searchContactTvId.visibility = View.GONE
                        binding.phonebookShimmer.visibility = View.VISIBLE
                        binding.phonebookShimmer.startShimmer()
                    }
                    is ContactsState.Success ->{
                        binding.phonebookShimmer.visibility = View.GONE
                        binding.phonebookShimmer.stopShimmer()
                        binding.phoneBookEmptyState.visibility = View.GONE
                        binding.addGuardianAngel.visibility = View.VISIBLE
                        binding.allContactsRecycler.visibility = View.VISIBLE
                        binding.searchContactTvId.visibility = View.VISIBLE
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
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.WRITE_CONTACTS,
                android.Manifest.permission.READ_CONTACTS),
                PERMISSION_REQUEST
            )
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
                binding.searchContactTvId.visibility = View.GONE
            }
            PASSED->{
                binding.phoneBookEmptyState.visibility = View.GONE
                binding.addGuardianAngel.visibility = View.VISIBLE
                binding.allContactsRecycler.visibility = View.VISIBLE
                binding.searchContactTvId.visibility = View.VISIBLE
            }

        }

    }

    private fun saveGuardianAngelsListToDb(checkedList: MutableList<PhonebookContact>) {
        val user = fAuth.currentUser
        val currentUserId = user?.uid

        if (currentUserId != null) {
            collectionReference.document(currentUserId)
                .get()
                .addOnSuccessListener {


                    }

                .addOnFailureListener {
                    Toast.makeText(requireContext(),it.message.toString(),Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun requestPermission() {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.getPhoneBook(requireActivity())
                viewModel.userContactsLiveDataList.observe(viewLifecycleOwner) {
                    when(it){
                        is ContactsState.Empty ->{
                            binding.phoneBookEmptyState.visibility = View.GONE
                            binding.addGuardianAngel.visibility = View.VISIBLE
                            binding.allContactsRecycler.visibility = View.GONE
                            binding.searchContactTvId.visibility = View.GONE
                            binding.phonebookShimmer.visibility = View.VISIBLE
                            binding.phonebookShimmer.startShimmer()
                        }
                        is ContactsState.Success ->{
                            binding.phonebookShimmer.visibility = View.GONE
                            binding.phonebookShimmer.stopShimmer()
                            binding.phoneBookEmptyState.visibility = View.GONE
                            binding.addGuardianAngel.visibility = View.VISIBLE
                            binding.allContactsRecycler.visibility = View.VISIBLE
                            binding.searchContactTvId.visibility = View.VISIBLE
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
            } else {
                //ask for permission
            }
        }
    }

    override fun onContactClick(contacts: MutableList<PhonebookContact>) {
        val user = fAuth.currentUser
        val currentUserId = user?.uid
        if (currentUserId != null) {
            collectionReference.document(currentUserId)
                .get()
                .addOnSuccessListener {

                }
        }

    }

    override fun onResume() {
        super.onResume()
        val readContactPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS)

        if (readContactPermission == PackageManager.PERMISSION_GRANTED )
        {
          //  Toast.makeText(requireContext(),"Permission granted",Toast.LENGTH_SHORT).show()
            viewModel.getPhoneBook(requireActivity())
            viewModel.userContactsLiveDataList.observe(viewLifecycleOwner) {
                when(it){
                    is ContactsState.Empty ->{
                        binding.phoneBookEmptyState.visibility = View.GONE
                        binding.addGuardianAngel.visibility = View.VISIBLE
                        binding.allContactsRecycler.visibility = View.GONE
                        binding.searchContactTvId.visibility = View.GONE
                        binding.phonebookShimmer.visibility = View.VISIBLE
                        binding.phonebookShimmer.startShimmer()
                    }
                    is ContactsState.Success ->{
                        binding.phonebookShimmer.visibility = View.GONE
                        binding.phonebookShimmer.stopShimmer()
                        binding.phoneBookEmptyState.visibility = View.GONE
                        binding.addGuardianAngel.visibility = View.VISIBLE
                        binding.allContactsRecycler.visibility = View.VISIBLE
                        binding.searchContactTvId.visibility = View.VISIBLE
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
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.WRITE_CONTACTS,
                android.Manifest.permission.READ_CONTACTS),
                PERMISSION_REQUEST
            )
        }
    }


}