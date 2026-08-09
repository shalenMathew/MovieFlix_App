package com.shalenmathew.movieflix.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shalenmathew.movieflix.databinding.FragmentMoreAppsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MoreAppsFragment : Fragment() {

    private var _binding: FragmentMoreAppsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.itemQuotesApp.root.setOnClickListener {
            // Open link to the app
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shalenmathew.github.io/quotes_app_website/"))
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}