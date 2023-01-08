package com.lemzeeyyy.sayfe.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.databinding.FragmentWebViewBinding
import com.lemzeeyyy.sayfe.model.IncomingAlertData


class WebViewFragment : Fragment() {
private lateinit var binding : FragmentWebViewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.webViewId.webViewClient = WebViewClient()

        binding.webViewId.loadUrl("https://www.google.com/")

        binding.webViewId.settings.javaScriptEnabled = true

        binding.webViewId.settings.setSupportZoom(true)
    }

}
