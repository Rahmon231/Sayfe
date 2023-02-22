package com.lemzeeyyy.sayfe.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.databinding.FragmentSettingsBinding
import com.lemzeeyyy.sayfe.repository.SayfeRepository
import com.lemzeeyyy.sayfe.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


const val IMG_REQUEST_CODE = 120

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private val viewModel: MainActivityViewModel by activityViewModels()

    private lateinit var binding : FragmentSettingsBinding

    private lateinit var backPressedCallback: OnBackPressedCallback


    companion object{
        var imageUri : Uri = Uri.EMPTY
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentSettingsBinding.inflate(inflater, container, false)
        backPressedCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(SettingsFragmentDirections.actionNavSettingsToNavHome())
        }
        backPressedCallback.isEnabled = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getUserName()
        viewModel.getUserEmail()
        viewModel.getCurrentUserId()
        viewModel.currentUserID.observe(viewLifecycleOwner){
            viewModel.getImageUriFromDb(it)
        }


        viewModel.userEmail.observe(viewLifecycleOwner){
            if (it == null) {
                return@observe
            }
            binding.currentUserEmail.setText(it.toString())
            }
            viewModel.userName.observe(viewLifecycleOwner){
                if (it==null)
                    return@observe
                binding.currrentUserName.setText(it.toString())
            }

        binding.accountSettingsSettings.setOnClickListener {
            findNavController().navigate(R.id.accountSettings)
        }

        viewModel.userImageUri.observe(viewLifecycleOwner){
            if (it.equals(Uri.EMPTY)){
                binding.profileImageSettings.setImageResource(R.drawable.profile_image)
            }else{
                Glide.with(requireActivity()).load(it).into(binding.profileImageSettings)
            }

        }

        binding.fabBtnUploadPic.setOnClickListener {
            val i = Intent()
            i.type = "image/*"
            i.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(i,IMG_REQUEST_CODE)

        }

        binding.logoutSettingsSettings.setOnClickListener {
            viewModel.signOutUser()
            findNavController().navigate(R.id.signInFragment)
        }

        binding.languageSettingsSettings.setOnClickListener {
            findNavController().navigate(R.id.languageFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK && requestCode == IMG_REQUEST_CODE){
             data?.let {
                  imageUri= data.data!!
                 if(imageUri!=null){
                     saveImageUri(imageUri)
                     binding.profileImageSettings.setImageURI(imageUri)
                     Toast.makeText(requireContext(),"Image Uploaded Successfully",Toast.LENGTH_LONG)
                         .show()
                 }

             }
        }
    }

    private fun saveImageUri(imageUri: Uri){
        viewModel.currentUserID.observe(viewLifecycleOwner){
            viewModel.saveImageUri(imageUri,it)
        }
    }

}