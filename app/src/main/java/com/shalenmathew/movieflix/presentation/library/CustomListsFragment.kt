package com.shalenmathew.movieflix.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.shalenmathew.movieflix.core.utils.ClickHandler
import com.shalenmathew.movieflix.R
import androidx.databinding.DataBindingUtil
import com.shalenmathew.movieflix.databinding.FragmentCustomListsBinding
import com.shalenmathew.movieflix.presentation.viewmodels.CustomListViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.LibrarySearchViewModel
import com.shalenmathew.movieflix.core.adapters.CustomListAdapter
import com.shalenmathew.movieflix.core.utils.gone
import com.shalenmathew.movieflix.core.utils.visible
import com.shalenmathew.movieflix.core.utils.showToast
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomListsFragment : Fragment() {

    private val viewModel: CustomListViewModel by viewModels()
    private var _binding: FragmentCustomListsBinding? = null
    private val mBinding get() = _binding!!

    private lateinit var adapter: CustomListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate<FragmentCustomListsBinding>(inflater, com.shalenmathew.movieflix.R.layout.fragment_custom_lists, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = CustomListAdapter(
            onListClick = { list ->
                if (ClickHandler.isClickAllowed() && findNavController().currentDestination?.id == R.id.libraryFragment) {
                    val bundle = Bundle().apply {
                        putInt("listId", list.id)
                        putString("listName", list.name)
                        putString("listDesc", list.description)
                    }
                    findNavController().navigate(R.id.action_libraryFragment_to_listDetailsFragment, bundle)
                }
            },
            onDeleteClick = { list ->
                viewModel.deleteList(list.id)
            }
        )
        mBinding.customListsRv.adapter = adapter
    }

    private fun observeData() {
        viewModel.allLists.observe(viewLifecycleOwner) { lists ->
            if (lists.isNotEmpty()) {
                adapter.submitList(lists)
                mBinding.customListsRv.visible()
                mBinding.customListsPlaceholder.gone()
                mBinding.peekingLogo.gone()
                mBinding.peekingShelf.gone()
            } else {
                mBinding.customListsRv.gone()
                mBinding.customListsPlaceholder.visible()
                mBinding.peekingLogo.visible()
                mBinding.peekingShelf.visible()
            }
        }
    }

    private fun setupClickListeners() {
        mBinding.createListFab.setOnClickListener {
            showCreateListDialog()
        }
    }

    private fun showCreateListDialog() {
        val dialog = BottomSheetDialog(requireContext(), com.shalenmathew.movieflix.R.style.SheetDialog)
        val view = layoutInflater.inflate(com.shalenmathew.movieflix.R.layout.dialog_create_list, null)
        
        val nameEt = view.findViewById<com.google.android.material.textfield.TextInputEditText>(com.shalenmathew.movieflix.R.id.list_name_et)
        val descEt = view.findViewById<com.google.android.material.textfield.TextInputEditText>(com.shalenmathew.movieflix.R.id.list_desc_et)
        val createBtn = view.findViewById<android.view.View>(com.shalenmathew.movieflix.R.id.create_list_confirm_btn)

        createBtn.setOnClickListener {
            val name = nameEt.text.toString().trim()
            if (name.isNotEmpty()) {
                viewModel.createList(name, descEt.text.toString().trim().takeIf { it.isNotEmpty() })
                dialog.dismiss()
            } else {
                nameEt.error = "Name cannot be empty"
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
