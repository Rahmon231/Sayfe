package com.lemzeeyyy.sayfe.fragments

import android.Manifest.permission.READ_CONTACTS
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.browser.customtabs.CustomTabsClient.getPackageName
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
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
import com.lemzeeyyy.sayfe.repository.SayfeRepository
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
    private var distinctNumber = emptyList<PhonebookContact>()
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var binding : FragmentPhoneBookBinding
    private lateinit var adapter : PhonebookRecyclerAdapter
    private lateinit var backPressedCallback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentPhoneBookBinding.inflate(inflater, container, false)
        backPressedCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(PhoneBookFragmentDirections.actionNavPhonebookToNavHome())
        }
        backPressedCallback.isEnabled = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.allowPermissionBtn.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null);
            intent.setData(uri);
            startActivity(intent);
        }

        adapter = PhonebookRecyclerAdapter(this,requireContext())

        binding.addGuardianAngel.setOnClickListener {
            adapter.triggerCheckedListInterface()
        }

        binding.allContactsRecycler.adapter = adapter
        requestContactPermission()

    }

    private fun updateViewForNoPermission(){
        binding.phoneBookEmptyState.visibility = View.VISIBLE
        binding.searchContactTvId.visibility = View.INVISIBLE
        binding.addGuardianAngel.visibility = View.INVISIBLE
        binding.allowPermissionBtn.visibility = View.VISIBLE
    }

    private fun updateContactView(contactState: Int?) {
        if (contactState == null)
            return

        when(contactState){
            BUSY ->{
                binding.phoneBookEmptyState.visibility = View.INVISIBLE
                binding.addGuardianAngel.visibility = View.INVISIBLE
                binding.allContactsRecycler.visibility = View.INVISIBLE
                binding.searchContactTvId.visibility = View.INVISIBLE
                binding.phoneBookSavingState.visibility = View.INVISIBLE
                binding.phoneBookFailedState.visibility = View.INVISIBLE
                binding.allowPermissionBtn.visibility = View.INVISIBLE
                binding.emptyStateImagee.visibility = View.GONE
            }
            EMPTY ->{
                binding.emptyStateImagee.visibility = View.VISIBLE
                binding.phonebookShimmer.visibility  = View.INVISIBLE
                binding.phonebookShimmer.stopShimmer()
                binding.addGuardianAngel.visibility = View.INVISIBLE
                binding.allContactsRecycler.visibility = View.INVISIBLE
                binding.searchContactTvId.visibility = View.INVISIBLE
                binding.phoneBookSavingState.visibility = View.INVISIBLE
                binding.phoneBookFailedState.visibility = View.INVISIBLE
                binding.allowPermissionBtn.visibility = View.INVISIBLE
            }

            PASSED->{
                binding.phoneBookEmptyState.visibility = View.INVISIBLE
                binding.addGuardianAngel.visibility = View.VISIBLE
                binding.allContactsRecycler.visibility = View.VISIBLE
                binding.searchContactTvId.visibility = View.VISIBLE
                binding.phoneBookSavingState.visibility = View.INVISIBLE
                binding.phoneBookFailedState.visibility = View.INVISIBLE
                binding.phonebookShimmer.visibility  = View.INVISIBLE
                binding.allowPermissionBtn.visibility = View.INVISIBLE
                binding.phonebookShimmer.stopShimmer()
                binding.emptyStateImagee.visibility = View.GONE
            }


        }

    }

    private fun updateGuardianAngelsToDb(checkedList: MutableList<PhonebookContact>){
        viewLifecycleOwner.lifecycleScope.launch {
           try {
               if (SayfeRepository.saveGuardianList(checkedList)){
                   Toast.makeText(requireContext(),"Guardian Number Added Successfully",Toast.LENGTH_SHORT).show()
                   binding.phoneBookFailedState.visibility = View.INVISIBLE
                   binding.phoneBookEmptyState.visibility = View.INVISIBLE
                   binding.addGuardianAngel.visibility = View.VISIBLE
                   binding.allContactsRecycler.visibility = View.VISIBLE
                   binding.searchContactTvId.visibility = View.VISIBLE
                   binding.phoneBookSavingState.visibility = View.INVISIBLE
                   findNavController().navigate(PhoneBookFragmentDirections.actionNavPhonebookToNavHome())
               }else{
                   binding.phoneBookFailedState.visibility = View.VISIBLE
                   binding.phoneBookEmptyState.visibility = View.INVISIBLE
                   binding.addGuardianAngel.visibility = View.INVISIBLE
                   binding.allContactsRecycler.visibility = View.INVISIBLE
                   binding.searchContactTvId.visibility = View.INVISIBLE
                   binding.phoneBookSavingState.visibility = View.INVISIBLE
                   Toast.makeText(requireContext(),"Unable to add guardian angel",Toast.LENGTH_SHORT).show()
               }
           } catch (e:Exception){
               Log.d("Add Guardian Exception", "updateGuardianAngelsToDb: ${e.message}")
           }
        }

    }

    private fun requestContactPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.contactStatus.observe(viewLifecycleOwner){
                    updateContactView(it)
                }
                viewModel.getPhoneBook()
                viewModel.userContactsLiveDataList.observe(viewLifecycleOwner){
                    if (it != null) {

                        contactList = it.distinctBy { distinctById->
                            distinctById.id
                        }
                        distinctNumber = contactList.distinctBy { distinctByNumber->
                            distinctByNumber.number
                        }
                        adapter.updatePhonebookData(distinctNumber)

                        binding.searchContactId.addTextChangedListener{substring->
                            if (substring.toString().isBlank()){
                                adapter.updatePhonebookData(distinctNumber)
                                return@addTextChangedListener
                            }
                            val filteredContacts = distinctNumber.filter {filtered->
                                filtered.name.contains(substring.toString(),true)
                            }
                            adapter.updatePhonebookData(filteredContacts)
                        }
                    }
                }
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS) -> {
                updateViewForNoPermission()
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.

            }
            else -> {

                ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                    android.Manifest.permission.READ_CONTACTS),
                    PERMISSION_REQUEST)
            }
        }
    }

    override fun onRequestPermissionsResult
                (requestCode: Int,
                 permissions: Array<String>,
                 grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CONTACT -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    viewModel.contactStatus.observe(viewLifecycleOwner){
                        updateContactView(it)
                    }
                    viewModel.getPhoneBook()
                    viewModel.userContactsLiveDataList.observe(viewLifecycleOwner){
                        if (it != null) {
                            adapter.updatePhonebookData(it)
                            contactList = it
                            binding.searchContactId.addTextChangedListener{substring->
                                if (substring.toString().isBlank()){
                                    adapter.updatePhonebookData(contactList.distinctBy { distinctContact->
                                        distinctContact.number
                                    })
                                    return@addTextChangedListener
                                }
                                val filteredContacts = contactList.filter {filtered->
                                    filtered.name.contains(substring.toString(),true)
                                }
                                adapter.updatePhonebookData(filteredContacts)
                            }
                        }
                    }
                } else {
                    updateViewForNoPermission()
                }
                return
            }

            else -> {
                // Ignore all other requests.
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


        updateGuardianAngelsToDb(contacts)


    }

    override fun onResume() {
        super.onResume()
        requestContactPermission()
    }


}