package com.lemzeeyyy.sayfe

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lemzeeyyy.sayfe.HomeFragment.Companion.recipientContacts
import com.lemzeeyyy.sayfe.databinding.FragmentPhoneBookBinding

class PhoneBookFragment : Fragment() {
    private lateinit var binding : FragmentPhoneBookBinding
    private lateinit var adapter : PhonebookRecyclerAdapter


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

        adapter = PhonebookRecyclerAdapter()
        adapter.updatePhonebookData(phoneBookData = recipientContacts)

        binding.allContactsRecycler.adapter = adapter

    }

}