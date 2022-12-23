package com.lemzeeyyy.sayfe

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.lemzeeyyy.sayfe.databinding.FragmentHomeBinding
import com.lemzeeyyy.sayfe.model.RecipientContact



const val REQUEST_CONTACT = 10
class HomeFragment : Fragment() {
    companion object{
         var recipientContacts : MutableList<RecipientContact> = ArrayList()
    }

    lateinit var binding : FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       //

        binding.alertBeneficiaryDashboard.setOnClickListener {



        }
    }


    @SuppressLint("Range")
    private fun getPhoneBook() : MutableList<RecipientContact>? {

        val cr = requireActivity().contentResolver
        val query = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        null)
        if (query!!.count > 0){
            while (query.moveToNext()){
                val id = query.getString(query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NAME_RAW_CONTACT_ID))
                val name = query.getString(query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number = query.getString(query.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                recipientContacts.add(RecipientContact(id,name,number))
                recipientContacts.forEach {
                   // Log.d("phonedata", "getPhoneBook: ${it.number} ")
                }
            }
        }

        return recipientContacts

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
         getPhoneBook()
    }

    override fun onDetach() {
        super.onDetach()

        recipientContacts = emptyList<RecipientContact>() as MutableList<RecipientContact>
    }

}