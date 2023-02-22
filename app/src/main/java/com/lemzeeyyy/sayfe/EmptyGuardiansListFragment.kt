package com.lemzeeyyy.sayfe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lemzeeyyy.sayfe.adapters.GuardianAngelAdapter
import com.lemzeeyyy.sayfe.databinding.FragmentEmptyGuardiansListBinding
import com.lemzeeyyy.sayfe.repository.SayfeRepository
import com.lemzeeyyy.sayfe.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EmptyGuardiansListFragment : BottomSheetDialogFragment() {
    private lateinit var binding : FragmentEmptyGuardiansListBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var adapter : GuardianAngelAdapter
    @Inject
    lateinit var repository: SayfeRepository

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

        binding.frameEmptyNow.setOnClickListener {
            emptyGuardianAngelList()
            findNavController().navigate(R.id.guardianAngelsFragment)
            dismiss()
        }

        binding.frameCancel.setOnClickListener {
            dismiss()
        }

    }
    private fun emptyGuardianAngelList(){
        viewLifecycleOwner.lifecycleScope.launch {
            repository.emptyGuardianList()
        }

    }

}