package com.shalenmathew.movieflix.presentation.watchlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.adapters.WatchListAdapter
import com.shalenmathew.movieflix.core.utils.ClickHandler
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.gone
import com.shalenmathew.movieflix.core.utils.visible
import com.shalenmathew.movieflix.databinding.FragmentWatchListBinding
import com.shalenmathew.movieflix.data.local_storage.entity.WatchListEntity
import com.shalenmathew.movieflix.presentation.viewmodels.LibrarySearchViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.ScheduledViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.WatchListViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.CustomListViewModel
import com.shalenmathew.movieflix.presentation.MainActivity
import com.shalenmathew.movieflix.core.utils.QuickActionOverlay
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.shalenmathew.movieflix.core.utils.showToast
import com.shalenmathew.movieflix.core.utils.shareMovie
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
@AndroidEntryPoint
class WatchListFragment : Fragment() {

    private val watchListViewModel: WatchListViewModel by viewModels()
    private val scheduledViewModel: ScheduledViewModel by viewModels()
    private val librarySearchVm: LibrarySearchViewModel by activityViewModels()
    private val customListViewModel: CustomListViewModel by viewModels()

    private var _binding: FragmentWatchListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: WatchListAdapter

    private var fullList: List<WatchListEntity> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentWatchListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inIt()
        observer()
        observeSearch()
    }

    private fun inIt() {
        watchListViewModel.getAllWatchListData()
        adapter = WatchListAdapter(
            onPosterClick = {
                if (ClickHandler.isClickAllowed() && findNavController().currentDestination?.id == R.id.libraryFragment) {
                    val bundle = Bundle()
                    bundle.putString(Constants.MEDIA_SEND_REQUEST_KEY, Gson().toJson(it))
                    findNavController().navigate(R.id.movieDetailsFragment, bundle)
                }
            },
            onLongClick = { view, movie ->
                showFloatingQuickActions(view, movie)
            }
        )
        val spanCount = resources.getInteger(R.integer.grid_span_count)
        binding.fragmentWatchListRv.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.fragmentWatchListRv.adapter = adapter
    }

    private fun showFloatingQuickActions(targetView: View, movie: com.shalenmathew.movieflix.domain.model.MovieResult) {
        val mainActivity = requireActivity() as? MainActivity ?: return

        val actions = mutableListOf<QuickActionOverlay.ActionItem>()

        actions.add(QuickActionOverlay.ActionItem(
            icon = R.drawable.baseline_share_24,
            label = getString(R.string.share).plus(" Movie"),
            action = { shareMovie(requireContext(), movie.title ?: movie.name ?: "", "") }
        ))

        actions.add(QuickActionOverlay.ActionItem(
            icon = R.drawable.baseline_delete_24,
            label = getString(R.string.btn_remove_from_watchlist),
            action = {
                watchListViewModel.deleteWatchListData(movie)
                showToast(requireContext(), getString(R.string.msg_removed_from_watchlist))
            }
        ))

        actions.add(QuickActionOverlay.ActionItem(
            icon = R.drawable.baseline_add_circle_24,
            label = getString(R.string.add_to_collection),
            action = { showChooseCustomListBottomSheet(movie) }
        ))

        mainActivity.showQuickActionMenu(
            targetView = targetView,
            actions = actions
        )
    }



    private fun showChooseCustomListBottomSheet(movie: com.shalenmathew.movieflix.domain.model.MovieResult) {
        val ctx = context ?: return
        val dialog = BottomSheetDialog(ctx, R.style.SheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_choose_custom_list, null)
        
        val rv = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.choose_list_rv)
        val createBtn = view.findViewById<View>(R.id.create_new_list_btn)

        customListViewModel.allLists.observe(viewLifecycleOwner) { lists ->
            viewLifecycleOwner.lifecycleScope.launch {
                val checkList = mutableListOf<Int>()
                for (list in lists) {
                    if (customListViewModel.isMovieInList(list.id, movie.id ?: -1)) {
                        checkList.add(list.id)
                    }
                }
                
                val adapter = com.shalenmathew.movieflix.core.adapters.ChooseCustomListAdapter(
                    onListClick = { selectedList ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            if (customListViewModel.isMovieInList(selectedList.id, movie.id ?: -1)) {
                                customListViewModel.removeMovieFromList(selectedList.id, movie.id ?: -1)
                                showToast(ctx, "Removed from ${selectedList.name}")
                            } else {
                                customListViewModel.addMovieToList(selectedList.id, movie)
                                showToast(ctx, "Added to ${selectedList.name}")
                            }
                        }
                    },
                    checkList = checkList
                )
                rv.adapter = adapter
                adapter.submitList(lists)
            }
        }

        createBtn.setOnClickListener {
            dialog.dismiss()
            showToast(ctx, "Go to Lists tab to create new lists")
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun observer() {
        watchListViewModel.getAllWatchListData().observe(viewLifecycleOwner) { list ->
            fullList = list
            val currentQuery = librarySearchVm.searchQuery.value
            if (currentQuery.isNullOrBlank()) {
                submitAndToggle(list)
            } else {
                applyFilter(currentQuery)
            }
        }
        scheduledViewModel.getAllScheduledMovies().observe(viewLifecycleOwner) { scheduledList ->
            val ids = scheduledList.mapNotNull { entity -> entity.id }.toSet()
            adapter.updateScheduledMovies(ids)
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
        val filtered = fullList.filter { entity ->
            val title = entity.movieResult?.title ?: entity.movieResult?.name ?: ""
            title.contains(query, ignoreCase = true)
        }
        submitAndToggle(filtered)
    }

    private fun submitAndToggle(list: List<WatchListEntity>) {
        val query = librarySearchVm.searchQuery.value

        if (list.isNotEmpty()) {
            adapter.submitList(list)
            binding.fragmentWatchListRv.visible()
            binding.fragmentWatchListPlaceholder.gone()
            binding.peekingLogo.gone()
            binding.peekingShelf.gone()
        }
        else {
            adapter.submitList(emptyList())
            binding.fragmentWatchListRv.gone()

            if (query.isBlank()) {
                binding.tvNoResult.gone()
                binding.fragmentWatchListPlaceholder.visible()
                binding.peekingLogo.visible()
                binding.peekingShelf.visible()
            }
            else {
                binding.tvNoResult.visible()
                binding.fragmentWatchListPlaceholder.gone()
                binding.peekingLogo.gone()
                binding.peekingShelf.gone()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}