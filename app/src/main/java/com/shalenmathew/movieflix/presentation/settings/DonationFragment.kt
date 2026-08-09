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
import com.shalenmathew.movieflix.databinding.FragmentDonationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DonationFragment : Fragment() {

    private var _binding: FragmentDonationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDonationBinding.inflate(inflater, container, false)
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
        // Buy me a coffee
        binding.itemBuyMeACoffee.itemSettingTitle.text = getString(R.string.buy_me_a_coffee)
        binding.itemBuyMeACoffee.itemSettingIcon.setImageResource(R.drawable.ic_coffee)
        binding.itemBuyMeACoffee.root.setOnClickListener {
            openUrl("https://buymeacoffee.com/shalenmathew")
        }

        // Ko-fi
        binding.itemKoFi.itemSettingTitle.text = getString(R.string.ko_fi)
        binding.itemKoFi.itemSettingIcon.setImageResource(R.drawable.ic_coffe_2)
        binding.itemKoFi.root.setOnClickListener {
            openUrl("https://ko-fi.com/shalenmathew")
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