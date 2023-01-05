package com.lemzeeyyy.sayfe.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.databinding.FragmentSettingsBinding
import com.lemzeeyyy.sayfe.viewmodels.MainActivityViewModel


const val IMG_REQUEST_CODE = 120
class SettingsFragment : Fragment() {
    private var storageRef = FirebaseStorage.getInstance().getReference();

    private val viewModel: MainActivityViewModel by activityViewModels()

    private var imagesRef: StorageReference = storageRef.child("profile_images")

    private lateinit var fAuth: FirebaseAuth

    private lateinit var binding : FragmentSettingsBinding


    companion object{
        var imageUri : Uri = Uri.EMPTY
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fAuth = Firebase.auth
        val user = fAuth.currentUser
        val currentUserId = user!!.uid

        viewModel.getImageUriFromDb(currentUserId)
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
            fAuth.signOut()
            findNavController().navigate(R.id.signInFragment)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK && requestCode == IMG_REQUEST_CODE){
             data?.let {
                  imageUri= data.data!!
                 if(imageUri!=null){
                     Log.d("henny", "onActivityResult: ${imageUri}")
                     saveImageUri(imageUri)
                     binding.profileImageSettings.setImageURI(imageUri)
                     Toast.makeText(requireContext(),"Image Uploaded Successfully",Toast.LENGTH_LONG)
                         .show()
                 }
             }
        }
    }

    private fun saveImageUri(imageUri: Uri){

        fAuth = Firebase.auth
        val user = fAuth.currentUser
        val currentUserId = user!!.uid

        imagesRef.child(currentUserId)
            .putFile(imageUri)
            .addOnSuccessListener {

            }
            .addOnFailureListener {

            }

    }

}