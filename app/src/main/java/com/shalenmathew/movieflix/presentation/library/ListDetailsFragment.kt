package com.shalenmathew.movieflix.presentation.library

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.ContentValues
import android.provider.MediaStore
import android.os.Environment
import android.graphics.Bitmap.CompressFormat
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.adapters.HorizontalAdapter
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.showToast
import com.shalenmathew.movieflix.databinding.FragmentListDetailsBinding
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.presentation.viewmodels.CustomListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class ListDetailsFragment : Fragment() {

    private val viewModel: CustomListViewModel by viewModels()
    private var _binding: FragmentListDetailsBinding? = null
    private val mBinding get() = _binding!!

    private var listId: Int = -1
    private var listName: String = ""
    private var listDesc: String? = null

    private lateinit var adapter: HorizontalAdapter
    private var currentMovies: List<MovieResult> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate<FragmentListDetailsBinding>(inflater, R.layout.fragment_list_details, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        listId = arguments?.getInt("listId") ?: -1
        listName = arguments?.getString("listName") ?: ""
        listDesc = arguments?.getString("listDesc")

        setupUI()
        setupRecyclerView()
        observeData()
    }

    private fun setupUI() {
        mBinding.listDetailsName.text = listName
        mBinding.listDetailsDesc.text = listDesc ?: ""
        mBinding.listDetailsDesc.visibility = if (listDesc.isNullOrEmpty()) View.GONE else View.VISIBLE
        
        mBinding.listDetailsBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        mBinding.listDetailsShareBtn.setOnClickListener {
            shareListAsImage()
        }
    }

    private fun setupRecyclerView() {
        adapter = HorizontalAdapter(onPosterClick = { movie ->
            val bundle = Bundle()
            bundle.putString(Constants.MEDIA_SEND_REQUEST_KEY, Gson().toJson(movie))
            findNavController().navigate(R.id.movieDetailsFragment, bundle)
        })
        mBinding.listMoviesRv.adapter = adapter
    }

    private fun observeData() {
        if (listId != -1) {
            viewModel.getMoviesInList(listId).observe(viewLifecycleOwner) { movies ->
                val movieResults = movies.map { 
                    MovieResult(
                        id = it.mediaId,
                        name = it.name,
                        title = it.name,
                        posterPath = it.posterPath,
                        backdropPath = it.backdropPath,
                        overview = it.overview,
                        mediaType = it.mediaType,
                        voteAverage = it.voteAverage,
                        releaseDate = it.releaseDate,
                        originalLanguage = it.originalLanguage
                    )
                }
                currentMovies = movieResults
                adapter.submitList(movieResults)
            }
        }
    }

    private fun shareListAsImage() {
        val moviesToShare = currentMovies.take(9)
        if (moviesToShare.isEmpty()) {
            showToast(requireContext(), "Add movies to the list first!")
            return
        }

        showToast(requireContext(), "Generating sharable image...")

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val posterBitmaps = moviesToShare.map { movie ->
                    val path = movie.posterPath
                    val isLocal = path != null && (path.startsWith("content://") || path.count { it == '/' } > 1)
                    val fullPath = if (isLocal) path else Constants.TMDB_POSTER_IMAGE_BASE_URL_W342.plus(path)
                    
                    Glide.with(this@ListDetailsFragment)
                        .asBitmap()
                        .load(fullPath)
                        .submit()
                        .get()
                }

                withContext(Dispatchers.Main) {
                    val shareView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_sharable_list, null)
                    shareView.findViewById<TextView>(R.id.sharable_title).text = listName
                    shareView.findViewById<TextView>(R.id.sharable_desc).text = listDesc ?: ""
                    shareView.findViewById<View>(R.id.sharable_desc).visibility = if (listDesc.isNullOrEmpty()) View.GONE else View.VISIBLE

                    val posterIds = listOf(R.id.poster_1, R.id.poster_2, R.id.poster_3, R.id.poster_4, R.id.poster_5, R.id.poster_6, R.id.poster_7, R.id.poster_8, R.id.poster_9)
                    
                    posterIds.forEachIndexed { index, id ->
                        val imageView = shareView.findViewById<ImageView>(id)
                        if (index < posterBitmaps.size) {
                            imageView.setImageBitmap(posterBitmaps[index])
                            imageView.visibility = View.VISIBLE
                        } else {
                            imageView.visibility = View.GONE
                        }
                    }

                    // Dynamically calculate height based on content
                    val width = 1080
                    
                    shareView.measure(
                        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                    val measuredHeight = shareView.measuredHeight
                    shareView.layout(0, 0, width, measuredHeight)

                    val bitmap = Bitmap.createBitmap(width, measuredHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    shareView.draw(canvas)

                    showSharePreviewBottomSheet(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showToast(requireContext(), "Failed to generate image")
                }
            }
        }
    }

    private fun showSharePreviewBottomSheet(bitmap: Bitmap) {
        val dialog = BottomSheetDialog(requireContext(), R.style.SheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_share_preview, null)

        val previewImg = view.findViewById<ImageView>(R.id.share_preview_image)
        val downloadBtn = view.findViewById<View>(R.id.share_download_btn)
        val shareBtn = view.findViewById<View>(R.id.share_now_btn)
        val cancelBtn = view.findViewById<View>(R.id.share_cancel_btn)

        previewImg.setImageBitmap(bitmap)

        downloadBtn.setOnClickListener {
            saveBitmapToGallery(bitmap)
            dialog.dismiss()
        }

        shareBtn.setOnClickListener {
            saveAndShareBitmap(bitmap)
            dialog.dismiss()
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val filename = "MovieFlix_List_${System.currentTimeMillis()}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MovieFlix")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = requireContext().contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        try {
            uri?.let {
                val stream = resolver.openOutputStream(it)
                if (stream != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.close()
                    
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        values.clear()
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)
                        resolver.update(it, values, null, null)
                    }
                    showToast(requireContext(), "Saved to Gallery!")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(requireContext(), "Failed to save image")
        }
    }

    private fun saveAndShareBitmap(bitmap: Bitmap) {
        try {
            val cachePath = File(requireContext().cacheDir, "shared_images")
            cachePath.mkdirs()
            val file = File(cachePath, "movie_list_share.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share List"))
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(requireContext(), "Failed to share image")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
