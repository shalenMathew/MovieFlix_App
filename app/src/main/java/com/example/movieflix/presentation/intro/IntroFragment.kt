package com.example.movieflix.presentation.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.movieflix.R
import com.example.movieflix.core.utils.DataStoreReference
import com.example.movieflix.databinding.FragmentIntroBinding
import kotlinx.coroutines.launch


class IntroFragment : Fragment() {

    private var _binding:FragmentIntroBinding?=null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        // Inflate the layout for this fragment
        _binding=DataBindingUtil.inflate(inflater,R.layout.fragment_intro,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleClickListeners()
    }

    private fun handleClickListeners() {
        binding.fragmentIntroCtnBtn.setOnClickListener {
            lifecycleScope.launch {
                DataStoreReference.updateIntroCompleted(requireContext(),true)
            }
            findNavController().navigate(R.id.action_introFragment_to_homeFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }

}
