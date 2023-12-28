package com.example.soft1c.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soft1c.R
import com.example.soft1c.adapter.LoadingAdapter
import com.example.soft1c.databinding.FragmentLoadingListBinding
import com.example.soft1c.repository.model.ItemClicked
import com.example.soft1c.repository.model.Loading
import com.example.soft1c.repository.model.LoadingEnableVisible
import com.example.soft1c.utils.Utils
import com.example.soft1c.viewmodel.LoadingViewModel
import com.google.android.material.textfield.TextInputEditText

class LoadingListFragment : BaseFragment<FragmentLoadingListBinding>(FragmentLoadingListBinding::inflate) {

    private lateinit var loadingAdapter: LoadingAdapter
    private val viewModel: LoadingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getLoadingList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observeViewModels()
    }

    private fun observeViewModels(){
        viewModel.toastLiveDat.observe(viewLifecycleOwner, ::toast)
        viewModel.loadingListLiveData.observe(viewLifecycleOwner, ::showLoadingList)
        viewModel.loadingLiveData.observe(viewLifecycleOwner, ::loadingByNumber)
    }

    private fun showLoadingList(list: List<Loading>){
        if(LoadingAdapter.LOADING_GUID.isEmpty() && list.isNotEmpty()){
            LoadingAdapter.LOADING_GUID = list[0].ref
        }
        showPbLoading(false)
        loadingAdapter.submitList(list)
    }

    private fun initUI(){
        if (Utils.refreshList) viewModel.getLoadingList()
        showPbLoading(true)
        initRvList()

        with(binding){
            ivAdd.setOnClickListener {
                findNavController().navigate(R.id.action_loadingListFragment_to_loadingFragment)
            }
            etxtDocumentNumber.setOnKeyListener(::findOpenDocumentByNumber)
            etxtDocumentNumber.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    val text = s.toString().trim()
                    if (text.length == 9) {
                        // Perform your desired operations here
                        showDialogLoading()
                        viewModel.getLoading(text)
                    }
                }
            })
            ivRefresh.setOnClickListener {
                showPbLoading(true)
                viewModel.getLoadingList()
            }
        }
    }

    private fun findOpenDocumentByNumber(eview: View, key: Int, event: KeyEvent): Boolean {
        if(key == 66 && event.action == KeyEvent.ACTION_UP){
            val thisView = (eview as TextInputEditText)
            if(thisView.text!!.isEmpty()){
                thisView.error = resources.getString(R.string.text_field_is_empyt)
                thisView.requestFocus()
                return false
            }
            thisView.error = null
            showDialogLoading()
            viewModel.getLoading(thisView.text.toString())
            return true
        }
        return false
    }

    private fun initRvList() {
        loadingAdapter = LoadingAdapter(::onItemClicked)
        with(binding.rvLoadingList){
            adapter = loadingAdapter
            setHasFixedSize(true)
            layoutManager= LinearLayoutManager(requireContext())
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    private fun onItemClicked(itemClicked: ItemClicked, loading: Loading) {
        when(itemClicked) {
            ItemClicked.ITEM -> {
                val args = Bundle().apply {
                    putString(LoadingFragment.KEY_LOADING_NUMBER, loading.guid)
                }
                openLoadingDetail(args)
            }
            else -> return
        }
    }

    private fun openLoadingDetail(bundle: Bundle) {
        findNavController().navigate(R.id.action_loadingListFragment_to_loadingFragment, bundle)
    }

    private fun loadingByNumber(pair: Pair<Loading, List<LoadingEnableVisible>>){
        val loading = pair.first
        closeDialogLoading()
        binding.etxtDocumentNumber.text?.clear()
        if(loading.ref.isNotEmpty())
            onItemClicked(ItemClicked.ITEM, loading)
        else
            toast(resources.getString(R.string.text_element_not_found))
    }


    private fun showPbLoading(show: Boolean) {
        with(binding) {
            rvLoadingList.isVisible = !show
            pbLoading.isVisible = show
        }
    }
}
