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
import com.shalenmathew.movieflix.domain.model.FAQRow
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
        val faqRows = listOf(
            // --- Widget Setup & Issues ---
            FAQRow.Header("Widget Setup & Issues"),
            FAQRow.Item(
                FAQItem(
                    "How to add the widget?",
                    "Long-press an empty space on your device home screen, select 'Widgets', find MovieFlix, and place the 'Up Next' widget on your screen."
                )
            ),
            FAQRow.Item(
                FAQItem(
                    "How does battery optimization affect the widget?",
                    "System battery optimization can restrict background processes and delay image loading for widgets. For the best experience and reliable poster updates, set MovieFlix's battery usage to 'Unrestricted' or 'No restrictions' in your device's app settings."
                )
            ),
            FAQRow.Item(
                FAQItem(
                    "Why don't recent changes reflect immediately on the widget?",
                    "Android limits widget refresh frequency to preserve battery, so updates might not appear right away. You can tap the refresh icon on the top right of the widget after a few moments. Additionally, the widget automatically performs a background sync every 6 hours to resolve any staleness."
                )
            ),

            // --- Notifications ---
            FAQRow.Header("Notifications"),
            FAQRow.Item(
                FAQItem(
                    "Why is the notification not displayed?",
                    "Ensure that notification permissions are granted in your device settings. Also, check if the app is excluded from battery optimization, as strict power-saving modes can delay or block scheduled reminders."
                )
            ),
            FAQRow.Item(
                FAQItem(
                    "How to schedule watch reminders?",
                    "On any movie or show detail screen, choose the notification icon or options to schedule a reminder for the exact date and time you want to watch."
                )
            ),

            // --- Library & Media Management ---
            FAQRow.Header("Library & Organizing"),
            FAQRow.Item(
                FAQItem(
                    "What is Watchlist?",
                    "A dedicated space to save movies and TV shows you want to watch later."
                )
            ),
            FAQRow.Item(
                FAQItem(
                    "What is Favorites?",
                    "A place to keep all your favorite movies and shows in one central location."
                )
            ),
            FAQRow.Item(
                FAQItem(
                    "What are Lists?",
                    "Customizable folders designed to help you organize and categorize your media."
                )
            ),
            FAQRow.Item(
                FAQItem(
                    "What are Collections?",
                    "Folders within your Watchlist and Favorites to keep your library organized as it grows."
                )
            ),
            FAQRow.Item(
                FAQItem(
                    "How does pinning lists work?",
                    "You can pin any list to your Favorites or Watchlist for quicker access. Lists created within those tabs are automatically pinned, but you can also manually manage pins from the Lists tab."
                )
            ),
            FAQRow.Item(
                FAQItem(
                    "Hold poster for quick options",
                    "By long-pressing any movie or show poster in your Favorites or Watchlist, you can quickly share, remove, or add it to a list."
                )
            ),
            FAQRow.Item(
                FAQItem(
                    "Changing posters & banners",
                    "You can customize the poster or banner of any movie or show with official artwork or images from your device gallery in both your Watchlist and Favorites. Simply long-press any poster to open options and select 'Change Poster' or 'Change Banner'."
                )
            ),
            FAQRow.Item(
                FAQItem(
                    "What is Media Gallery?",
                    "Located in the movie detail screen, this feature allows you to save your favorite scenes, fan art, or personal media related to that title."
                )
            ),

            // --- Series Tracking ---
            FAQRow.Header("Series Tracking"),
            FAQRow.Item(
                FAQItem(
                    "What is the Tracking feature?",
                    "It helps you keep record of your progress through TV shows. You can start tracking a series from its detail screen under 'More Options'. Once tracked, use the Tracking tab to mark episodes as watched."
                )
            ),
            FAQRow.Item(
                FAQItem(
                    "Customize Tracking banners",
                    "In the Tracking tab, long-press on any series banner to customize its look with official artwork or your own gallery images."
                )
            ),

            // --- Backup & Data ---
            FAQRow.Header("App Data & Backup"),
            FAQRow.Item(
                FAQItem(
                    "How to backup data?",
                    "Go to Settings > Backup to export your data as a file. You can import this file later to restore your entire library."
                )
            )
        )

        val adapter = FAQAdapter(faqRows)
        binding.rvFaq.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
