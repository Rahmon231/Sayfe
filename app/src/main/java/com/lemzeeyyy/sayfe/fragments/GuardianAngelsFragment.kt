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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.EmptyGuardiansListFragment
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.adapters.GuardianAngelAdapter
import com.lemzeeyyy.sayfe.databinding.FragmentGuardianAngelsBinding
import com.lemzeeyyy.sayfe.model.GuardianData
import com.lemzeeyyy.sayfe.model.PhonebookContact
import com.lemzeeyyy.sayfe.model.RecipientContact
import com.lemzeeyyy.sayfe.viewmodels.*


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
        val currentUserId = user?.uid
        adapter = GuardianAngelAdapter()
        viewModel.guardianAngelsStatus.observe(viewLifecycleOwner){
            updateGuardianAngelsView(it)
        }
        if (currentUserId != null) {
            viewModel.getGuardianAngelsListFromDb(currentUserId)
        }
        viewModel.guardianLiveData.observe(viewLifecycleOwner){
            val dataList = it.guardianInfo
//            if (dataList.isEmpty()){
//                binding.guardianListEmptyState.visibility = View.VISIBLE
//                binding.verticalEllipse.visibility = View.GONE
//            }else{
//
//                binding.guardianListEmptyState.visibility = View.GONE
//                binding.verticalEllipse.visibility = View.VISIBLE
//            }
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
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
               return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.guardianLiveData.observe(viewLifecycleOwner){
                    val contacts = it.guardianInfo
                    val deletedContacts = contacts[viewHolder.adapterPosition]
                    val position = viewHolder.adapterPosition
                    contacts.removeAt(position)
                    saveGuardianAngelsToDb(contacts,requireContext())
                    viewModel.guardianLiveData.observe(viewLifecycleOwner){
                        val dataList = it.guardianInfo
                        if (dataList.isEmpty()){
                            binding.guardianListEmptyState.visibility = View.VISIBLE
                            binding.verticalEllipse.visibility = View.GONE
                        }else{
                            binding.guardianListEmptyState.visibility = View.GONE
                            binding.verticalEllipse.visibility = View.VISIBLE
                        }
                        adapter.updateGuardianAngelsList(contacts.toMutableList())
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
                    }

                    Snackbar.make(binding.guardianContactsRecycler,"Deleted ${deletedContacts.name}",Snackbar.LENGTH_SHORT)
                        .setAction(
                            "Undo", View.OnClickListener {
                                contacts.add(position,deletedContacts)
                                saveGuardianAngelsToDb(contacts,requireContext())
                                //adapter.notifyItemChanged(position)
                                viewModel.guardianLiveData.observe(viewLifecycleOwner){
                                    val dataList = it.guardianInfo
                                    if (dataList.isEmpty()){
                                        binding.guardianListEmptyState.visibility = View.VISIBLE
                                        binding.verticalEllipse.visibility = View.GONE
                                    }else{
                                        binding.guardianListEmptyState.visibility = View.GONE
                                        binding.verticalEllipse.visibility = View.VISIBLE
                                    }
                                    adapter.updateGuardianAngelsList(contacts.toMutableList())
                                    adapter.notifyItemChanged(position)
                                }
                            }
                        )
                        .show()


                }


            }

        }).attachToRecyclerView(binding.guardianContactsRecycler)

    }

    private fun openBottomDialog() {
        val dialog = EmptyGuardiansListFragment()
        dialog.setCancelable(true)
        dialog.show(childFragmentManager, "NOTIFICATION SHEET")
    }

    private fun updateGuardianAngelsView(contactState: Int?) {
        if (contactState == null)
            return

        when(contactState){
            BUSY ->{
               binding.guardianLoadingState.visibility = View.VISIBLE
                binding.contactListRelGuar.visibility = View.INVISIBLE
                binding.guardianContactsRecycler.visibility = View.INVISIBLE
            }
            EMPTY ->{
                binding.guardianListEmptyState.visibility = View.VISIBLE
                binding.verticalEllipse.visibility = View.GONE
            }
            PASSED ->{
                binding.guardianContactsRecycler.visibility = View.VISIBLE
                binding.guardianListEmptyState.visibility = View.INVISIBLE
                binding.contactListRelGuar.visibility = View.VISIBLE
                binding.guardianListEmptyState.visibility = View.INVISIBLE
                binding.guardianLoadingState.visibility = View.INVISIBLE
            }
            FAILED -> {
                binding.guardianFailedState.visibility = View.VISIBLE
                binding.guardianListEmptyState.visibility = View.INVISIBLE
                binding.contactListRelGuar.visibility = View.INVISIBLE
                binding.guardianContactsRecycler.visibility = View.INVISIBLE
                binding.guardianListEmptyState.visibility = View.INVISIBLE
                binding.guardianLoadingState.visibility = View.INVISIBLE
            }

        }

    }

    private fun saveGuardianAngelsToDb(checkedList: MutableList<PhonebookContact>, context: Context) {
        val user = fAuth.currentUser
        val currentUserId = user?.uid
        if (currentUserId != null) {
            collectionReference.document(currentUserId)
                .get()
                .addOnSuccessListener {
                    val doc = it.toObject(GuardianData::class.java)
                    if (doc != null) {
                        collectionReference
                            .document(currentUserId)
                            .set(GuardianData(checkedList))
                            .addOnSuccessListener {

//                                Toast.makeText(context,"Guardian angel added successfully deleted",Toast.LENGTH_SHORT)
//                                    .show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context,"Unable to delete guardian angel",Toast.LENGTH_SHORT)
                                    .show()
                            }

                    }

                }

                .addOnFailureListener {
                    Toast.makeText(requireContext(),it.message.toString(),Toast.LENGTH_SHORT).show()
                }
        }
    }




}