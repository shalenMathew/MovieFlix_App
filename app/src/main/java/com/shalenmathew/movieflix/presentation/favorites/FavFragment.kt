package com.shalenmathew.movieflix.presentation.favorites

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
import com.shalenmathew.movieflix.core.adapters.FavAdapters
import com.shalenmathew.movieflix.core.utils.ClickHandler
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.gone
import com.shalenmathew.movieflix.core.utils.visible
import com.shalenmathew.movieflix.databinding.FragmentFavBinding
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.presentation.viewmodels.FavMovieViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.LibrarySearchViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.ScheduledViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.CustomListViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.HomeInfoViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.WatchListViewModel
import com.shalenmathew.movieflix.presentation.viewmodels.SeriesTrackingViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.shalenmathew.movieflix.core.utils.showToast
import com.shalenmathew.movieflix.core.utils.shareMovie
import com.shalenmathew.movieflix.core.utils.NetworkResults
import com.shalenmathew.movieflix.core.utils.loadImage
import android.widget.TextView
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavFragment : Fragment() {
    private val favMovieViewModel: FavMovieViewModel by viewModels()
    private val scheduledViewModel: ScheduledViewModel by viewModels()
    private val librarySearchVm: LibrarySearchViewModel by activityViewModels()
    private val customListViewModel: CustomListViewModel by viewModels()
    private val homeInfoViewModel: HomeInfoViewModel by viewModels()
    private val watchListViewModel: WatchListViewModel by viewModels()
    private val seriesTrackingViewModel: SeriesTrackingViewModel by viewModels()

    private var _binding: FragmentFavBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FavAdapters
    private var fullList: List<FavouritesEntity> = emptyList()

    private var currentMediaIdForPoster: Int? = null
    private val pickPosterLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            handleLocalPosterSelection(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFavBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        observe()
        observeSearch()
    }

    private fun init() {
        favMovieViewModel.getAllMovieData()
        adapter = FavAdapters(
            onPosterClick = {
                if (ClickHandler.isClickAllowed() && findNavController().currentDestination?.id == R.id.libraryFragment) {
                    val bundle = Bundle()
                    bundle.putString(Constants.MEDIA_SEND_REQUEST_KEY, Gson().toJson(it))
                    findNavController().navigate(R.id.movieDetailsFragment, bundle)
                }
            },
            onLongClick = { movie ->
                val entity = fullList.find { it.id == movie.id }
                entity?.let { showQuickActionsBottomSheet(it) }
            }
        )
        val spanCount = resources.getInteger(R.integer.grid_span_count)
        binding.fragmentFavRv.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.fragmentFavRv.adapter = adapter
    }

    private fun showQuickActionsBottomSheet(favouritesEntity: FavouritesEntity) {
        val movie = favouritesEntity.movieResult
        val ctx = context ?: return
        val dialog = BottomSheetDialog(ctx, R.style.SheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_quick_actions, null)

        val header = view.findViewById<TextView>(R.id.quick_actions_header)
        val shareItem = view.findViewById<View>(R.id.quick_action_share)
        val changePosterItem = view.findViewById<View>(R.id.quick_action_change_poster)
        val changePosterText = view.findViewById<TextView>(R.id.quick_action_change_poster_text)
        val removeItem = view.findViewById<View>(R.id.quick_action_remove)
        val removeText = view.findViewById<android.widget.TextView>(R.id.quick_action_remove_text)
        val collectionItem = view.findViewById<View>(R.id.quick_action_collection)

        header.text = movie.title ?: movie.name
        removeText.text = getString(R.string.btn_remove_from_favorites)

        shareItem.setOnClickListener {
            dialog.dismiss()
            shareMovie(ctx, movie.title ?: movie.name ?: "", "")
        }

        // Setup Change Poster Option
        changePosterItem.visibility = View.VISIBLE
        val isTVItem = movie.mediaType == "tv" || (movie.name != null && movie.title == null)
        changePosterText.text = if (isTVItem) getString(R.string.btn_change_show_poster) else getString(R.string.btn_change_movie_poster)
        changePosterItem.setOnClickListener {
            dialog.dismiss()
            showChoosePosterBottomSheet(favouritesEntity)
        }

        removeItem.setOnClickListener {
            dialog.dismiss()
            favMovieViewModel.deleteFavMovieData(movie)
            showToast(ctx, getString(R.string.msg_removed_from_favorites))
        }

        collectionItem.setOnClickListener {
            dialog.dismiss()
            showChooseCustomListBottomSheet(movie)
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showChoosePosterBottomSheet(favouritesEntity: FavouritesEntity) {
        val movie = favouritesEntity.movieResult
        val ctx = context ?: return
        val dialog = BottomSheetDialog(ctx, R.style.SheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_choose_poster, null)

        val loader = view.findViewById<View>(R.id.choose_poster_loader)
        val rv = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.choose_poster_rv)
        val cancelBtn = view.findViewById<View>(R.id.choose_poster_cancel_btn)
        val galleryBtn = view.findViewById<View>(R.id.choose_poster_from_gallery_btn)

        galleryBtn.setOnClickListener {
            currentMediaIdForPoster = movie.id
            pickPosterLauncher.launch("image/*")
            dialog.dismiss()
        }

        val adapter = com.shalenmathew.movieflix.core.adapters.PosterChoiceAdapter { selectedPath ->
            updateMediaPoster(movie.id ?: -1, selectedPath)
            dialog.dismiss()
        }
        rv.adapter = adapter

        val lang = if (movie.originalLanguage?.length == 2) movie.originalLanguage else null
        val mediaId = movie.id ?: -1
        val isTV = movie.mediaType == "tv" || (movie.name != null && movie.title == null)

        if (isTV) {
            homeInfoViewModel.getTVImages(mediaId, lang)
        } else {
            homeInfoViewModel.getMovieImages(mediaId, lang)
        }

        // Remove any previous observers to prevent state pollution from other movies
        homeInfoViewModel.mediaImages.removeObservers(viewLifecycleOwner)
        homeInfoViewModel.mediaImages.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResults.Loading -> {
                    loader.visibility = View.VISIBLE
                    rv.visibility = View.GONE
                }
                is NetworkResults.Success -> {
                    val posters = result.data?.posters?.mapNotNull { it.filePath }
                    if (posters.isNullOrEmpty()) {
                        // Fallback: If we tried TV and failed/empty, try Movie (and vice versa)
                        // This handles cases where mediaType in DB was wrong or null
                        if (isTV) {
                            homeInfoViewModel.getMovieImages(mediaId, lang)
                        } else if (movie.mediaType == null) {
                            homeInfoViewModel.getTVImages(mediaId, lang)
                        }
                    }
                    loader.visibility = View.GONE
                    rv.visibility = View.VISIBLE
                    adapter.submitList(posters)
                }
                is NetworkResults.Error -> {
                    // Fallback on Error as well
                    if (isTV) {
                        homeInfoViewModel.getMovieImages(mediaId, lang)
                    } else if (movie.mediaType == null) {
                        homeInfoViewModel.getTVImages(mediaId, lang)
                    } else {
                        loader.visibility = View.GONE
                        showToast(ctx, result.message ?: getString(R.string.msg_something_went_wrong))
                    }
                }
            }
        }

        cancelBtn.setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun updateMediaPoster(id: Int, posterPath: String) {
        // Update Favorites
        favMovieViewModel.updateFavPoster(id, posterPath)
        
        // Global Sync
        watchListViewModel.updateWatchListPoster(id, posterPath)
        seriesTrackingViewModel.updateSeriesPoster(id, posterPath)
        customListViewModel.updateMoviePosterAcrossLists(id, posterPath)
        
        showToast(requireContext(), getString(R.string.msg_poster_updated))
    }

    private fun handleLocalPosterSelection(uri: android.net.Uri) {
        val ctx = context ?: return
        val id = currentMediaIdForPoster ?: return
        val fileName = "poster_${id}_${System.currentTimeMillis()}.jpg"
        val file = java.io.File(ctx.filesDir, fileName)

        try {
            ctx.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            updateMediaPoster(id, file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(ctx, "Failed to copy image")
        }
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
            // Optional: navigate to custom list fragment to create
            showToast(ctx, "Go to Lists tab to create new lists")
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun observe() {
        favMovieViewModel.getAllMovieData().observe(viewLifecycleOwner) { list ->
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

    private fun submitAndToggle(list: List<FavouritesEntity>) {
        val query = librarySearchVm.searchQuery.value

        if (list.isNotEmpty()) {
            adapter.submitList(list)
            binding.fragmentFavRv.visible()
            binding.fragmentFavPlaceholder.gone()
            binding.peekingLogo.gone()
            binding.peekingShelf.gone()
        } else {
            adapter.submitList(emptyList())
            binding.fragmentFavRv.gone()

            if (query.isBlank()) {
                binding.tvNoResult.gone()
                binding.fragmentFavPlaceholder.visible()
                binding.peekingLogo.visible()
                binding.peekingShelf.visible()
            }
            else {
                binding.tvNoResult.visible()
                binding.fragmentFavPlaceholder.gone()
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