package com.shalenmathew.movieflix.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.databinding.FragmentAboutMeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutMeFragment : Fragment() {

    private var _binding: FragmentAboutMeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutMeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        setupItems()
    }

    private fun setupItems() {
        // Portfolio
        binding.itemPortfolio.itemSettingTitle.text = getString(R.string.portfolio)
        binding.itemPortfolio.itemSettingIcon.setImageResource(R.drawable.ic_portfolio)
        binding.itemPortfolio.root.setOnClickListener {
            openUrl("https://shalenmathew.github.io/portfolio-website/")
        }

        // Github
        binding.itemGithub.itemSettingTitle.text = getString(R.string.github)
        binding.itemGithub.itemSettingIcon.setImageResource(R.drawable.ic_github)
        binding.itemGithub.root.setOnClickListener {
            openUrl("https://github.com/shalenMathew")
        }

        // Twitter
        binding.itemTwitter.itemSettingTitle.text = getString(R.string.twitter)
        binding.itemTwitter.itemSettingIcon.setImageResource(R.drawable.ic_twitter)
        binding.itemTwitter.root.setOnClickListener {
            openUrl("https://x.com/shalenmathew")
        }

        // Discord
        binding.itemDiscord.itemSettingTitle.text = getString(R.string.discord)
        binding.itemDiscord.itemSettingIcon.setImageResource(R.drawable.ic_message)
        binding.itemDiscord.root.setOnClickListener {
            openUrl("https://discord.gg/sxRMDnrbRg")
        }

        // LinkedIn
        binding.itemLinkedin.itemSettingTitle.text = getString(R.string.linkedin)
        binding.itemLinkedin.itemSettingIcon.setImageResource(R.drawable.ic_ln)
        binding.itemLinkedin.root.setOnClickListener {
            openUrl("https://www.linkedin.com/in/shalen-mathew-3b566921b/")
        }

        // Youtube
        binding.itemYoutube.itemSettingTitle.text = getString(R.string.youtube)
        binding.itemYoutube.itemSettingIcon.setImageResource(R.drawable.ic_youtube)
        binding.itemYoutube.root.setOnClickListener {
            openUrl("https://www.youtube.com/@shalenmathew")
        }

        // LinkTree
        binding.itemLinktree.itemSettingTitle.text = getString(R.string.linktree)
        binding.itemLinktree.itemSettingIcon.setImageResource(R.drawable.ic_link)
        binding.itemLinktree.root.setOnClickListener {
            openUrl("https://linktr.ee/shalenmathew")
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}