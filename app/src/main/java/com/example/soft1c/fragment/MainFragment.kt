package com.example.soft1c.fragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.soft1c.R
import com.example.soft1c.databinding.FragmentMainBinding

class MainFragment : BaseFragment<FragmentMainBinding>(FragmentMainBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        with(binding){
            acceptanceCardView.setOnClickListener { findNavController().navigate(R.id.action_mainFragment_to_acceptanceFragment) }
            loadingCardView.setOnClickListener { findNavController().navigate(R.id.action_mainFragment_to_loadingFragment) }
        }
    }
}