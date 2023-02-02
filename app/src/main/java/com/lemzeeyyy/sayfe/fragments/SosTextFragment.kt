package com.lemzeeyyy.sayfe.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.databinding.FragmentSosTextBinding
import com.lemzeeyyy.sayfe.model.Users


class SosTextFragment : Fragment() {
    private lateinit var binding : FragmentSosTextBinding
    var sosText = ""
    private lateinit var fAuth: FirebaseAuth
    private val database = Firebase.firestore
    private val collectionReference = database.collection("Users")



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
        fAuth = Firebase.auth
        val user = fAuth.currentUser
        val currentUserId = user?.uid
        binding.saveSos.setOnClickListener {
            sosText = binding.defaultText.text.toString()
            if (currentUserId != null) {
                collectionReference.whereEqualTo("currentUserId",currentUserId)
                    .addSnapshotListener { value, error ->
                        if (value != null) {
                            if (!value.isEmpty)
                                for (snapshot : QueryDocumentSnapshot in value){
                                    val users = snapshot.toObject(Users::class.java)
                                    users.userSOSText = sosText
                                    collectionReference.document(snapshot.id)
                                        .set(users)
                                        .addOnSuccessListener {
                                            Toast.makeText(requireContext(),"Saved",Toast.LENGTH_SHORT).show()
                                            findNavController().navigateUp()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(requireContext(),it.message.toString(),Toast.LENGTH_SHORT).show()
                                        }
                                }
                        }
                    }
            }
            binding.backArrowSos.setOnClickListener {
                findNavController().navigateUp()
            }
        }

    }
}