package com.lemzeeyyy.sayfe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.databinding.FragmentGuardianAngelsBinding


class GuardianAngelsFragment : Fragment() {

    private lateinit var binding : FragmentGuardianAngelsBinding
    private lateinit var adapter : GuardianAngelAdapter
     private val viewModel: GuardianAngelsViewModel by activityViewModels()

    private lateinit var fAuth: FirebaseAuth
    private val database = Firebase.firestore
    private val collectionReference = database.collection("Guardian Angels")


            override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentGuardianAngelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        fAuth = Firebase.auth
        val user = fAuth.currentUser
        val currentUserId = user!!.uid

        adapter = GuardianAngelAdapter()
        viewModel.getGuardianAngelsListToDb(currentUserId)
        viewModel.guardianLiveData.observe(viewLifecycleOwner){
            val dataList = it.guardianInfo
            adapter.updateGuardianAngelsList(dataList)
        }
        binding.guardianContactsRecycler.adapter = adapter


        binding.backArrowGuardianAngel.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.nav_home)
        }

    }

}