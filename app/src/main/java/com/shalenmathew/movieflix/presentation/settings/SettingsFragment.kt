package com.shalenmathew.movieflix.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shalenmathew.movieflix.BuildConfig
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSettingsItems()
        setupFooter()
    }

    private fun setupFooter() {
        var footerText = getString(R.string.made_by_shalen_mathew)
        if (BuildConfig.DEBUG) {
            footerText += " {debug.mod}"
        }
        binding.tvFooter.text = footerText
    }

    private fun setupSettingsItems() {
        // Troubleshoot
        binding.itemTroubleshoot.itemSettingTitle.text = getString(R.string.learn_how_to_use)
        binding.itemTroubleshoot.itemSettingIcon.setImageResource(R.drawable.ic_troubleshoot)
        binding.itemTroubleshoot.root.setOnClickListener {
            val bundle = bundleOf("TITLE" to getString(R.string.learn_how_to_use))
            findNavController().navigate(R.id.action_settingsFragment_to_comingSoonFragment, bundle)
        }

        // Language
        binding.itemLanguage.itemSettingTitle.text = getString(R.string.change_language)
        binding.itemLanguage.itemSettingIcon.setImageResource(R.drawable.ic_language)
        binding.itemLanguage.root.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_languageFragment)
        }

        // App Suggestion
        binding.itemAppSuggestion.itemSettingTitle.text = getString(R.string.app_suggestion)
        binding.itemAppSuggestion.itemSettingIcon.setImageResource(R.drawable.ic_play_arrow)
        binding.itemAppSuggestion.root.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_moreAppsFragment)
        }

        // About Me
        binding.itemAboutMe.itemSettingTitle.text = getString(R.string.about_me)
        binding.itemAboutMe.itemSettingIcon.setImageResource(R.drawable.user_outline)
        binding.itemAboutMe.root.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_aboutMeFragment)
        }

        // Donation
        binding.itemDonation.itemSettingTitle.text = getString(R.string.donation)
        binding.itemDonation.itemSettingIcon.setImageResource(R.drawable.ic_donation)
        binding.itemDonation.root.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_donationFragment)
        }

        // Source Code
        binding.itemSourceCode.itemSettingTitle.text = getString(R.string.source_code)
        binding.itemSourceCode.itemSettingIcon.setImageResource(R.drawable.ic_source_code)
        binding.itemSourceCode.root.setOnClickListener {
            openUrl("https://github.com/shalenMathew/MovieFlix_App")
        }

        // Backup
        binding.itemBackup.itemSettingTitle.text = getString(R.string.backup_restore)
        binding.itemBackup.itemSettingIcon.setImageResource(R.drawable.ic_calendar_check) // Using a placeholder icon
        binding.itemBackup.root.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_backupFragment)
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}