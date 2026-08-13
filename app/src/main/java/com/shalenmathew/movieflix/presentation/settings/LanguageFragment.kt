package com.shalenmathew.movieflix.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.adapters.LanguageAdapter
import com.shalenmathew.movieflix.core.utils.DataStoreReference
import com.shalenmathew.movieflix.databinding.FragmentLanguageBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LanguageFragment : Fragment() {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val languages = listOf(
            LanguageItem(getString(R.string.english), "en-US"),
            LanguageItem(getString(R.string.spanish), "es-ES"),
            LanguageItem(getString(R.string.hindi), "hi-IN"),
            LanguageItem(getString(R.string.french), "fr-FR"),
            LanguageItem(getString(R.string.german), "de-DE")
        )

        lifecycleScope.launch {
            val currentLang = DataStoreReference.getSelectedLanguage(requireContext()).first()
            
            val adapter = LanguageAdapter(languages, currentLang) { selectedLang ->
                updateAppLanguage(selectedLang)
            }

            binding.rvLanguages.apply {
                this.adapter = adapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }

    private fun updateAppLanguage(languageCode: String) {
        lifecycleScope.launch {
            DataStoreReference.setSelectedLanguage(requireContext(), languageCode)
        }

        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
        
        // Navigate back after selection
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class LanguageItem(val name: String, val code: String)
}