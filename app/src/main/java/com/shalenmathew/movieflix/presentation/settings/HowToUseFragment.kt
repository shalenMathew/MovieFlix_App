package com.shalenmathew.movieflix.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shalenmathew.movieflix.core.adapters.FAQAdapter
import com.shalenmathew.movieflix.databinding.FragmentHowToUseBinding
import com.shalenmathew.movieflix.domain.model.FAQItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HowToUseFragment : Fragment() {

    private var _binding: FragmentHowToUseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHowToUseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        val faqList = listOf(
            FAQItem(
                "What is Watchlist?",
                "A dedicated space to save movies and TV shows you want to watch later."
            ),
            FAQItem(
                "What is Favorites?",
                "A place to keep all your favorite movies and shows in one central location."
            ),
            FAQItem(
                "What are Lists?",
                "Customizable folders designed to help you organize and categorize your media."
            ),
            FAQItem(
                "What are Collections?",
                "Folders within your Watchlist and Favorites to keep your library organized as it grows."
            ),
            FAQItem(
                "What is Media Gallery?",
                "Located in the movie detail screen, this feature allows you to save your favorite scenes, fan art, or personal media related to that title."
            ),
            FAQItem(
                "How does pinning lists work?",
                "You can pin any list to your Favorites or Watchlist for quicker access. Lists created within those tabs are automatically pinned, but you can also manually manage pins from the Lists tab."
            ),
            FAQItem(
                "Hold poster for quick options",
                "By long-pressing any movie or show poster in your Favorites or Watchlist, you can quickly share, remove, add to a list, or even change the poster image."
            ),
            FAQItem(
                "What is the Tracking feature?",
                "It helps you keep record of your progress through TV shows. You can start tracking a series from its detail screen under 'More Options'. Once tracked, use the Tracking tab to mark episodes as watched."
            ),
            FAQItem(
                "Customize Tracking banners",
                "In the Tracking tab, long-press on any series banner to customize its look with official artwork or your own gallery images."
            ),
            FAQItem(
                "How to backup data?",
                "Go to Settings > Backup to export your data as a file. You can import this file later to restore your entire library."
            )
        )

        val adapter = FAQAdapter(faqList)
        binding.rvFaq.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
