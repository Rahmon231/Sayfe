package com.lemzeeyyy.sayfe.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.lemzeeyyy.sayfe.EmptyGuardiansListFragment
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.adapters.GuardianAngelAdapter
import com.lemzeeyyy.sayfe.databinding.FragmentGuardianAngelsBinding
import com.lemzeeyyy.sayfe.model.GuardianState
import com.lemzeeyyy.sayfe.model.PhonebookContact
import com.lemzeeyyy.sayfe.repository.SayfeRepository
import com.lemzeeyyy.sayfe.viewmodels.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GuardianAngelsFragment : Fragment() {

    private lateinit var binding : FragmentGuardianAngelsBinding
    private lateinit var adapter : GuardianAngelAdapter
     private val viewModel: MainActivityViewModel by activityViewModels()
    private val authViewModel : AuthViewModel by activityViewModels()
    @Inject
    lateinit var repository : SayfeRepository

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

        adapter = GuardianAngelAdapter()

        binding.guardianContactsRecycler.adapter = adapter
        viewModel.getGuardianAngels()
        viewModel.guardianAngelsStatus.observe(viewLifecycleOwner){
            updateGuardianAngelsView(it)
        }

        viewModel.guardianLiveData.observe(viewLifecycleOwner){
            val dataList = it.guardianInfo
            adapter.updateGuardianAngelsList(dataList.toMutableList())
        }

        binding.backArrowGuardianAngel.setOnClickListener {
          findNavController().navigate(R.id.nav_home)
        }

        binding.verticalEllipse.setOnClickListener {
            openBottomDialog()
        }

        binding.contactGuardianBtn.setOnClickListener {
           findNavController().navigate(GuardianAngelsFragmentDirections.actionGuardianAngelsFragmentToNavHome())
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
                    updateGuardianAngelsToDb(contacts)
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
                                updateGuardianAngelsToDb(contacts)
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

        })
            .attachToRecyclerView(binding.guardianContactsRecycler)
    }

    private fun updateGuardianStatus(it: GuardianState?) {
        if (it==null){
            return
        }
        when (it) {
            GuardianState.Empty -> {
                binding.guardianListEmptyState.visibility = View.VISIBLE
                binding.verticalEllipse.visibility = View.INVISIBLE
                binding.guardianLoadingState.visibility = View.INVISIBLE
                binding.guardianShimmer.visibility = View.INVISIBLE
                binding.guardianShimmer.stopShimmer()
                Log.d("EMPTY STATE ON", "onViewCreated: ")
                Log.d("OTHER STATES OFF", "onViewCreated: ")
            }
            is GuardianState.Failure -> {
                Toast.makeText(requireContext(), it.exception.message, Toast.LENGTH_SHORT).show()
                binding.guardianFailedState.visibility = View.VISIBLE
                binding.guardianListEmptyState.visibility = View.INVISIBLE
                binding.contactListRelGuar.visibility = View.INVISIBLE
                binding.guardianContactsRecycler.visibility = View.INVISIBLE
                binding.guardianListEmptyState.visibility = View.INVISIBLE
                binding.guardianLoadingState.visibility = View.INVISIBLE
                binding.guardianShimmer.visibility = View.INVISIBLE
                binding.guardianShimmer.stopShimmer()
                Log.d("FAILURE STATE ON", "onViewCreated: ")
                Log.d("OTHER STATES OFF", "onViewCreated: ")
            }
            GuardianState.Loading -> {
                binding.guardianShimmer.visibility = View.VISIBLE
                binding.guardianShimmer.startShimmer()
                // binding.guardianLoadingState.visibility = View.VISIBLE
                binding.contactListRelGuar.visibility = View.INVISIBLE
                binding.guardianContactsRecycler.visibility = View.INVISIBLE
                Log.d("LOADING STATE ON", "onViewCreated: ")
                Log.d("OTHER STATES OFF", "onViewCreated: ")
            }
            is GuardianState.Success -> {
                binding.guardianContactsRecycler.visibility = View.VISIBLE
                binding.guardianListEmptyState.visibility = View.INVISIBLE
                binding.contactListRelGuar.visibility = View.VISIBLE
                binding.guardianListEmptyState.visibility = View.INVISIBLE
                binding.guardianLoadingState.visibility = View.INVISIBLE
                binding.guardianShimmer.visibility = View.INVISIBLE
                binding.guardianShimmer.stopShimmer()
                Log.d("SUCCESS STATE ON", "onViewCreated: ")
                Log.d("OTHER STATES OFF", "onViewCreated: ")
                Log.d("VALUE", "onViewCreated: ${it.contacts.size}")
            }
            null -> {
                return
            }
        }
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
                binding.guardianShimmer.visibility = View.VISIBLE
                binding.guardianShimmer.startShimmer()
              // binding.guardianLoadingState.visibility = View.VISIBLE
                binding.contactListRelGuar.visibility = View.INVISIBLE
                binding.guardianContactsRecycler.visibility = View.INVISIBLE
            }
            EMPTY ->{
                binding.guardianListEmptyState.visibility = View.VISIBLE
                binding.verticalEllipse.visibility = View.INVISIBLE
                binding.guardianLoadingState.visibility = View.INVISIBLE
                binding.guardianShimmer.visibility = View.INVISIBLE
                binding.guardianShimmer.stopShimmer()

            }
            PASSED ->{
                binding.guardianContactsRecycler.visibility = View.VISIBLE
                binding.guardianListEmptyState.visibility = View.INVISIBLE
                binding.contactListRelGuar.visibility = View.VISIBLE
                binding.guardianListEmptyState.visibility = View.INVISIBLE
                binding.guardianLoadingState.visibility = View.INVISIBLE
                binding.guardianShimmer.visibility = View.INVISIBLE
                binding.guardianShimmer.stopShimmer()
            }
            FAILED -> {
                binding.guardianFailedState.visibility = View.VISIBLE
                binding.guardianListEmptyState.visibility = View.INVISIBLE
                binding.contactListRelGuar.visibility = View.INVISIBLE
                binding.guardianContactsRecycler.visibility = View.INVISIBLE
                binding.guardianListEmptyState.visibility = View.INVISIBLE
                binding.guardianLoadingState.visibility = View.INVISIBLE
                binding.guardianShimmer.visibility = View.INVISIBLE
                binding.guardianShimmer.stopShimmer()
            }

        }

    }


    private fun updateGuardianAngelsToDb(checkedList: MutableList<PhonebookContact>){
        viewLifecycleOwner.lifecycleScope.launch {
            repository.saveGuardianList(checkedList)
        }

    }
}