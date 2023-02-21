package com.lemzeeyyy.sayfe.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.databinding.FragmentSosTextBinding
import com.lemzeeyyy.sayfe.model.Users
import com.lemzeeyyy.sayfe.repository.SayfeRepository
import kotlinx.coroutines.launch


class SosTextFragment : Fragment() {
    private lateinit var binding : FragmentSosTextBinding
    var sosText = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSosTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.saveSos.setOnClickListener {
            sosText = binding.defaultText.text.toString()
            if (sosText.isNotEmpty()){
                saveSosText(sosText)
            }else{
                Toast.makeText(requireContext(),"You haven't entered any text",Toast.LENGTH_SHORT).show()
            }
            binding.backArrowSos.setOnClickListener {
                findNavController().navigateUp()
            }
        }

    }
    private fun saveSosText(sosText : String){
        viewLifecycleOwner.lifecycleScope.launch {
            if (SayfeRepository.saveSosText(sosText)){
                Toast.makeText(requireContext(),"Saved",Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

    }
}