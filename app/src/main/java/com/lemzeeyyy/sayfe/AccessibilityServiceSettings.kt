package com.lemzeeyyy.sayfe

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lemzeeyyy.sayfe.databinding.FragmentAccessibilityServiceSettingsBinding

class AccessibilityServiceSettings : BottomSheetDialogFragment() {
    private lateinit var binding : FragmentAccessibilityServiceSettingsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAccessibilityServiceSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.frameYes.setOnClickListener {
            checkAccessibilityPermission()
            dismiss()
        }
        binding.frameNo.setOnClickListener {
            dismiss()
        }

    }

    private fun checkAccessibilityPermission(): Boolean {
        var accessEnabled = 0
        try {
            accessEnabled =
                Settings.Secure.getInt(requireActivity().contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }
        return if (accessEnabled == 0) {
            /** if not construct intent to request permission  */
            Toast.makeText(requireContext(),"Permission not granted, to use the background service of this product, kindly grant permission",
                Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            /** request permission via start activity for result  */
            startActivity(intent)
            false
        } else {
            true
        }
    }

}