package com.example.soft1c.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import com.google.zxing.integration.android.IntentIntegrator
import androidx.navigation.fragment.findNavController
import com.example.soft1c.R
import com.example.soft1c.databinding.FragmentTestBinding
import timber.log.Timber

class TestFragment : BaseFragment<FragmentTestBinding>(FragmentTestBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            it.title = resources.getString(R.string.text_title_test)
        }
        initUI()
    }

    override fun onResume() {
        super.onResume()
        if (resultForSearch.isNotEmpty()) {
            binding.txtFoundedText.text = resultForSearch
            binding.etxtNumber.requestFocus()
            resultForSearch = ""
        }
    }

    private fun initUI() {
        with(binding) {
            etxtNumber.apply {
                isFocusable = true
                isFocusableInTouchMode = true
                setOnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        view.postDelayed({
                            activity?.let {
                                val imm =
                                    it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.showSoftInput(view.findFocus(), 0)
                            }
                        }, 300)

                    }
                }
            }

            cardFind.setOnClickListener {
                openForSearch()
            }
            btnSubmit.setOnClickListener {
                submitClicked()
            }

            val listOfs = listOf("Python", "Java", "C#", "Javascript")
            val adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listOfs)
            etxtCorrect.setAdapter(adapter)
        }
    }

    private fun submitClicked() {
        toast(resources.getString(R.string.text_submitted))
    }

    private fun openForSearch() {
        if (findNavController().currentDestination?.id == R.id.testFragment)
            findNavController().navigate(R.id.action_testFragment_to_searchFragment)
    }

    companion object {
        var resultForSearch = ""
    }
}