package com.lemzeeyyy.sayfe.fragments

import android.content.Context
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.CheckedContactListener
import com.lemzeeyyy.sayfe.activities.MainActivity
import com.lemzeeyyy.sayfe.activities.PERMISSION_REQUEST
import com.lemzeeyyy.sayfe.adapters.PhonebookRecyclerAdapter
import com.lemzeeyyy.sayfe.databinding.FragmentPhoneBookBinding
import com.lemzeeyyy.sayfe.model.ContactsState
import com.lemzeeyyy.sayfe.model.GuardianData
import com.lemzeeyyy.sayfe.model.PhonebookContact
import com.lemzeeyyy.sayfe.model.RecipientContact
import com.lemzeeyyy.sayfe.viewmodels.BUSY
import com.lemzeeyyy.sayfe.viewmodels.EMPTY
import com.lemzeeyyy.sayfe.viewmodels.MainActivityViewModel
import com.lemzeeyyy.sayfe.viewmodels.PASSED
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val REQUEST_CONTACT = 10
class PhoneBookFragment : Fragment(), CheckedContactListener {

    private var contactList = emptyList<PhonebookContact>()

    private val viewModel: MainActivityViewModel by activityViewModels()

    private lateinit var binding : FragmentPhoneBookBinding
    private lateinit var adapter : PhonebookRecyclerAdapter
    private lateinit var fAuth: FirebaseAuth
    private val database = Firebase.firestore
    private val collectionReference = database.collection("Guardian Angels")
    private val usersReference = database.collection("Users")




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
        viewModel.getPhoneBook(requireActivity())

        binding.addGuardianAngel.setOnClickListener {
            adapter.triggerCheckedListInterface()
        }
        binding.allContactsRecycler.adapter = adapter

        val readContactPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS)


        if (readContactPermission == PackageManager.PERMISSION_GRANTED )
        {
            viewModel.contactStatus.observe(viewLifecycleOwner){
                updateContactView(it)
            }
            viewModel.userContactsLiveDataList.observe(viewLifecycleOwner) {
                when(it){
                    is ContactsState.Success ->{
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
        else {
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
                binding.phoneBookEmptyState.visibility = View.INVISIBLE
                //binding.phoneBookLoadingState.visibility = View.VISIBLE
                binding.phonebookShimmer.visibility  = View.VISIBLE
                binding.phonebookShimmer.stopShimmer()
                binding.addGuardianAngel.visibility = View.INVISIBLE
                binding.allContactsRecycler.visibility = View.INVISIBLE
                binding.searchContactTvId.visibility = View.INVISIBLE
                binding.phoneBookSavingState.visibility = View.INVISIBLE
                binding.phoneBookFailedState.visibility = View.INVISIBLE
            }
            EMPTY ->{
                binding.phoneBookEmptyState.visibility = View.VISIBLE
                binding.phonebookShimmer.visibility  = View.INVISIBLE
                binding.phonebookShimmer.stopShimmer()
                binding.phoneBookLoadingState.visibility = View.INVISIBLE
                binding.addGuardianAngel.visibility = View.INVISIBLE
                binding.allContactsRecycler.visibility = View.INVISIBLE
                binding.searchContactTvId.visibility = View.INVISIBLE
                binding.phoneBookSavingState.visibility = View.INVISIBLE
                binding.phoneBookFailedState.visibility = View.INVISIBLE
            }

            PASSED->{
                binding.phoneBookEmptyState.visibility = View.INVISIBLE
                binding.phoneBookLoadingState.visibility = View.INVISIBLE
                binding.addGuardianAngel.visibility = View.VISIBLE
                binding.allContactsRecycler.visibility = View.VISIBLE
                binding.searchContactTvId.visibility = View.VISIBLE
                binding.phoneBookSavingState.visibility = View.INVISIBLE
                binding.phoneBookFailedState.visibility = View.INVISIBLE
                binding.phonebookShimmer.visibility  = View.INVISIBLE
                binding.phonebookShimmer.stopShimmer()
            }


        }

    }

    private fun saveGuardianAngelsListToDb(checkedList: MutableList<PhonebookContact>, context: Context) {
        val user = fAuth.currentUser
        val currentUserId = user?.uid
        if (currentUserId != null) {
            binding.phoneBookSavingState.visibility = View.VISIBLE
            binding.phoneBookLoadingState.visibility = View.INVISIBLE
            binding.phoneBookEmptyState.visibility = View.INVISIBLE
            binding.phoneBookLoadingState.visibility = View.INVISIBLE
            binding.addGuardianAngel.visibility = View.INVISIBLE
            binding.allContactsRecycler.visibility = View.INVISIBLE
            binding.searchContactTvId.visibility = View.INVISIBLE

            collectionReference.document(currentUserId)
                .get()
                .addOnSuccessListener {

                        collectionReference
                            .document(currentUserId)
                            .set(GuardianData(checkedList))
                            .addOnSuccessListener {
                                binding.phoneBookFailedState.visibility = View.INVISIBLE
                                binding.phoneBookEmptyState.visibility = View.INVISIBLE
                                binding.phoneBookLoadingState.visibility = View.INVISIBLE
                                binding.addGuardianAngel.visibility = View.VISIBLE
                                binding.allContactsRecycler.visibility = View.VISIBLE
                                binding.searchContactTvId.visibility = View.VISIBLE
                                binding.phoneBookSavingState.visibility = View.INVISIBLE
                                Toast.makeText(context,"Guardian angels added successfully",Toast.LENGTH_SHORT)
                                    .show()
                                findNavController().navigateUp()
                            }
                            .addOnFailureListener {
                                binding.phoneBookFailedState.visibility = View.VISIBLE
                                binding.phoneBookEmptyState.visibility = View.INVISIBLE
                                binding.phoneBookLoadingState.visibility = View.INVISIBLE
                                binding.addGuardianAngel.visibility = View.INVISIBLE
                                binding.allContactsRecycler.visibility = View.INVISIBLE
                                binding.searchContactTvId.visibility = View.INVISIBLE
                                binding.phoneBookSavingState.visibility = View.INVISIBLE
                                Toast.makeText(context,"Unable to add guardian angels",Toast.LENGTH_SHORT)
                                    .show()
                            }



                }

                .addOnFailureListener {
                    Toast.makeText(requireContext(),it.message.toString(),Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun requestContactPermission() {

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
                viewModel.contactStatus.observe(viewLifecycleOwner){
                    updateContactView(it)
                }
                viewModel.userContactsLiveDataList.observe(viewLifecycleOwner) {
                    when(it){
                        is ContactsState.Success ->{
                            contactList = it.contacts
                            adapter.updatePhonebookData(contactList.distinctBy { recipientData->
                                recipientData.name
                            })
                        }
                        else -> Unit

                    }


                }
                viewModel.userContactsLiveDataList.observe(viewLifecycleOwner) {
                    when(it){

                        is ContactsState.Empty ->{

                        }
                        is ContactsState.Failure ->{

                        }

                        is ContactsState.Success ->{
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
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.WRITE_CONTACTS,
                    android.Manifest.permission.READ_CONTACTS),
                    PERMISSION_REQUEST
                )
            }
        }
    }

    override fun onContactClick(contacts: MutableList<PhonebookContact>,dbContacts : MutableList<PhonebookContact>) {
        Log.d("TAG", "onContactClick: Triggered")
        dbContacts.forEach {
            if (contacts.contains(it)) {
                contacts.remove(it)
            }
            else {
                contacts.add(it)
            }
        }
        saveGuardianAngelsListToDb(contacts,requireContext())


    }

    override fun onResume() {
        super.onResume()
        val readContactPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS)

        if (readContactPermission == PackageManager.PERMISSION_GRANTED )
        {
          //  Toast.makeText(requireContext(),"Permission granted",Toast.LENGTH_SHORT).show()
            viewModel.getPhoneBook(requireActivity())
            viewModel.contactStatus.observe(viewLifecycleOwner){
                updateContactView(it)
            }
            viewModel.userContactsLiveDataList.observe(viewLifecycleOwner) {
                when(it){
                    is ContactsState.Success ->{
                        contactList = it.contacts
                        adapter.updatePhonebookData(contactList.distinctBy { recipientData->
                            recipientData.name
                        })
                    }
                    else -> Unit

                }


            }
            viewModel.getPhoneBook(requireActivity())
            viewModel.userContactsLiveDataList.observe(viewLifecycleOwner) {
                when(it){
                    is ContactsState.Empty ->{

                    }
                    is ContactsState.Success ->{
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