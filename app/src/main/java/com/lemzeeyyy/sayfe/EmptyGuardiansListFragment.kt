package com.lemzeeyyy.sayfe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.adapters.GuardianAngelAdapter
import com.lemzeeyyy.sayfe.databinding.FragmentEmptyGuardiansListBinding
import com.lemzeeyyy.sayfe.model.GuardianData
import com.lemzeeyyy.sayfe.model.PhonebookContact
import com.lemzeeyyy.sayfe.model.RecipientContact
import com.lemzeeyyy.sayfe.viewmodels.MainActivityViewModel
import kotlinx.coroutines.launch


class EmptyGuardiansListFragment : BottomSheetDialogFragment() {
    private lateinit var binding : FragmentEmptyGuardiansListBinding
    private lateinit var fAuth: FirebaseAuth
    private val database = Firebase.firestore
    private val collectionReference = database.collection("Guardian Angels")
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var adapter : GuardianAngelAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEmptyGuardiansListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = GuardianAngelAdapter()
        fAuth = Firebase.auth
        val user = fAuth.currentUser
        val currentUserId = user!!.uid

        viewModel.getGuardianAngelsListFromDb(currentUserId)
        binding.frameEmptyNow.setOnClickListener {
            emptyGuardianAngelList(listOf<RecipientContact>().toMutableList())
            findNavController().navigate(R.id.guardianAngelsFragment)
            dismiss()
        }

        binding.frameCancel.setOnClickListener {
            dismiss()
        }

    }
    private fun emptyGuardianAngelList(checkedList: MutableList<RecipientContact>){
        val user = fAuth.currentUser
        val currentUserId = user!!.uid
        val docData = GuardianData(listOf<PhonebookContact>().toMutableList())

        collectionReference
            .document(currentUserId)
            .set(docData)
            .addOnSuccessListener {

            }
            .addOnFailureListener {

            }
    }

}