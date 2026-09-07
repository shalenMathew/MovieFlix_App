package com.shalenmathew.movieflix.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import android.widget.ImageView
import android.widget.TextView
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
import com.shalenmathew.movieflix.domain.model.UserCustomList
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CustomListsFragment : Fragment() {

    private val viewModel: CustomListViewModel by viewModels()
    private val librarySearchVm: LibrarySearchViewModel by activityViewModels()
    private var _binding: FragmentCustomListsBinding? = null
    private val mBinding get() = _binding!!

    private lateinit var adapter: CustomListAdapter
    private var fullList: List<UserCustomList> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate<FragmentCustomListsBinding>(inflater, com.shalenmathew.movieflix.R.layout.fragment_custom_lists, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
        observeSearch()
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
            onMoreClick = { view, list ->
                showListOptionsBottomSheet(list)
            }
        )
        mBinding.customListsRv.adapter = adapter
    }

    private fun showListOptionsBottomSheet(list: UserCustomList) {
        val dialog = BottomSheetDialog(requireContext(), R.style.SheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_list_options, null)
        
        view.findViewById<TextView>(R.id.list_options_header).text = list.name
        
        val pinBtn = view.findViewById<View>(R.id.action_pin_list)
        val pinText = view.findViewById<TextView>(R.id.pin_text)

        pinText.text = if (list.isPinned) "Unpin from Favorites" else "Pin to Favorites"
        
        pinBtn.setOnClickListener {
            viewModel.togglePinList(list.id, !list.isPinned)
            showToast(requireContext(), if (list.isPinned) "Unpinned" else "Pinned")
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.action_delete_list).setOnClickListener {
            dialog.dismiss()
            showDeleteConfirmationDialog(list)
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showDeleteConfirmationDialog(list: com.shalenmathew.movieflix.domain.model.UserCustomList) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(), R.style.TrackingAlertDialog)
            .setTitle(getString(R.string.msg_delete_list_title))
            .setMessage(getString(R.string.msg_delete_list_message, list.name))
            .setPositiveButton(getString(R.string.btn_delete)) { _, _ ->
                viewModel.deleteList(list.id)
                showToast(requireContext(), "List deleted")
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun observeData() {
        viewModel.allLists.observe(viewLifecycleOwner) { lists ->
            fullList = lists
            val currentQuery = librarySearchVm.searchQuery.value
            if (currentQuery.isNullOrBlank()) {
                submitAndToggle(lists)
            } else {
                applyFilter(currentQuery)
            }
        }
    }

    private fun observeSearch() {
        lifecycleScope.launchWhenStarted {
            librarySearchVm.searchQuery.collectLatest { query ->
                if (query.isNullOrBlank()) {
                    submitAndToggle(fullList)
                } else {
                    applyFilter(query)
                }
            }
        }
    }

    private fun applyFilter(query: String) {
        val filtered = fullList.filter { list ->
            list.name.contains(query, ignoreCase = true)
        }
        submitAndToggle(filtered)
    }

    private fun submitAndToggle(lists: List<UserCustomList>) {
        val query = librarySearchVm.searchQuery.value

        if (lists.isNotEmpty()) {
            adapter.submitList(lists)
            mBinding.customListsRv.visible()
            mBinding.customListsPlaceholder.gone()
            mBinding.peekingLogo.gone()
            mBinding.peekingShelf.gone()
            mBinding.tvNoResult.gone()
        } else {
            adapter.submitList(emptyList())
            mBinding.customListsRv.gone()

            if (query.isNullOrBlank()) {
                mBinding.tvNoResult.gone()
                mBinding.customListsPlaceholder.visible()
                mBinding.peekingLogo.visible()
                mBinding.peekingShelf.visible()
            } else {
                mBinding.tvNoResult.visible()
                mBinding.customListsPlaceholder.gone()
                mBinding.peekingLogo.gone()
                mBinding.peekingShelf.gone()
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
