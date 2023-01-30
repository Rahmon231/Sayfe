package com.lemzeeyyy.sayfe.fragments

import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.EmptyGuardiansListFragment
import com.lemzeeyyy.sayfe.viewmodels.MainActivityViewModel
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.adapters.GuardianAngelAdapter
import com.lemzeeyyy.sayfe.databinding.FragmentGuardianAngelsBinding
import com.lemzeeyyy.sayfe.model.GuardianData
import com.lemzeeyyy.sayfe.model.RecipientContact


class GuardianAngelsFragment : Fragment() {

    private lateinit var binding : FragmentGuardianAngelsBinding
    private lateinit var adapter : GuardianAngelAdapter
     private val viewModel: MainActivityViewModel by activityViewModels()

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
        viewModel.getGuardianAngelsListFromDb(currentUserId)
        viewModel.guardianLiveData.observe(viewLifecycleOwner){
            val dataList = it.guardianInfo
            if (dataList.isEmpty()){
                binding.guardianListEmptyState.visibility = View.VISIBLE
                binding.verticalEllipse.visibility = View.GONE
            }else{
                binding.guardianListEmptyState.visibility = View.GONE
                binding.verticalEllipse.visibility = View.VISIBLE
            }
            adapter.updateGuardianAngelsList(dataList.toMutableList())
        }
        binding.guardianContactsRecycler.adapter = adapter


        binding.backArrowGuardianAngel.setOnClickListener {
          findNavController().navigate(R.id.nav_home)
        }

        binding.verticalEllipse.setOnClickListener {
            openBottomDialog()
        }
        binding.contactGuardianBtn.setOnClickListener {
           findNavController().navigateUp()
        }
    }

    private fun openBottomDialog() {
        val dialog = EmptyGuardiansListFragment()
        dialog.setCancelable(true)
        dialog.show(childFragmentManager, "NOTIFICATION SHEET")
    }




}