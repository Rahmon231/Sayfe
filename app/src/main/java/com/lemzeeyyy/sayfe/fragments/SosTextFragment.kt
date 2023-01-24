package com.lemzeeyyy.sayfe.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.databinding.FragmentSosTextBinding


class SosTextFragment : Fragment() {
    private lateinit var binding : FragmentSosTextBinding


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
        binding.backArrowSos.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}