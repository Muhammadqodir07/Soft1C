package com.example.soft1c.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soft1c.R
import com.example.soft1c.adapter.LoadingAdapter
import com.example.soft1c.adapter.ReloadingAdapter
import com.example.soft1c.databinding.FragmentLoadingListBinding
import com.example.soft1c.repository.model.ItemClicked
import com.example.soft1c.repository.model.Loading
import com.example.soft1c.repository.model.LoadingEnableVisible
import com.example.soft1c.utils.Utils
import com.example.soft1c.utils.getDisplayWidth
import com.example.soft1c.viewmodel.LoadingViewModel
import com.example.soft1c.viewmodel.ReloadingViewModel
import com.google.android.material.textfield.TextInputEditText

class LoadingListFragment :
    BaseFragment<FragmentLoadingListBinding>(FragmentLoadingListBinding::inflate) {

    private lateinit var loadingAdapter: LoadingAdapter
    private lateinit var reloadingAdapter: ReloadingAdapter
    private val viewModel: LoadingViewModel by viewModels()
    private val reloadingViewModel: ReloadingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getLoadingList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observeViewModels()
    }

    private fun observeViewModels() {
        viewModel.toastLiveDat.observe(viewLifecycleOwner, ::toast)
        viewModel.loadingListLiveData.observe(viewLifecycleOwner, ::showLoadingList)
        viewModel.loadingLiveData.observe(viewLifecycleOwner, ::loadingByNumber)
        reloadingViewModel.reloadingListLiveData.observe(viewLifecycleOwner, ::showReloadingList)
        reloadingViewModel.reloadingLiveData.observe(viewLifecycleOwner, ::reloadingByNumber)
    }

    private fun showLoadingList(list: List<Loading>) {
        if (LoadingAdapter.LOADING_GUID.isEmpty() && list.isNotEmpty()) {
            LoadingAdapter.LOADING_GUID = list[0].ref
        }
        showPbLoading(false)
        loadingAdapter.submitList(list)
        //reloadingViewModel.getReloadingList()
    }

    private fun showReloadingList(list: List<Loading>) {
        showPbLoading(false)
        reloadingAdapter.submitList(list)
        if (binding.chbReloading.isChecked) {
            binding.scrollRvLayout.fullScroll(ScrollView.FOCUS_RIGHT)
        }
    }

    private fun initUI() {
        if (Utils.refreshList) viewModel.getLoadingList()
        showPbLoading(true)
        initRvList()

        val chbListener = View.OnClickListener {
            with(binding)
            {
                val isLoading = it.id == binding.chbLoading.id
                val isReloading = it.id == binding.chbReloading.id

                if ((it as CheckBox).isChecked) {
                    chbLoading.isChecked = isLoading
                    chbReloading.isChecked = isReloading
                }
                with(scrollRvLayout) {
                    if (isLoading) {
                        fullScroll(ScrollView.FOCUS_LEFT)
                    } else if (isReloading) {
                        fullScroll(ScrollView.FOCUS_RIGHT)
                    }
                }
            }
        }

        with(binding) {
            chbLoading.setOnClickListener(chbListener)
            chbReloading.setOnClickListener(chbListener)
            for (i in 0 until linearScrollChild.childCount) {
                val childView = linearScrollChild.getChildAt(i)
                val layoutParams = LinearLayout.LayoutParams(
                    getDisplayWidth(requireContext()), // Width size here
                    LinearLayout.LayoutParams.MATCH_PARENT // Height size here
                )
                childView.layoutParams = layoutParams
            }

            ivAdd.setOnClickListener {
                val cacheData = getLoadingFromCache()
                val args = Bundle()
                if (cacheData != null) {
                    showYesNoDialog(requireContext(), "Open the saved document?",
                        onYes = {
                            args.putString(LoadingFragment.KEY_LOADING_NUMBER, "")
                            args.putString(LoadingFragment.KEY_LOADING_DATA, cacheData)
                            findNavController().navigate(
                                R.id.action_loadingListFragment_to_loadingFragment,
                                args
                            )
                        }, onNo = {
                            findNavController().navigate(R.id.action_loadingListFragment_to_loadingFragment)
                        })
                }else
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
                        if (chbLoading.isChecked)
                            viewModel.getLoading(text)
                        else
                            reloadingViewModel.getReloading(text)
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
        if (key == 66 && event.action == KeyEvent.ACTION_UP) {
            val thisView = (eview as TextInputEditText)
            if (thisView.text!!.isEmpty()) {
                thisView.error = resources.getString(R.string.text_field_is_empyt)
                thisView.requestFocus()
                return false
            }
            thisView.error = null
            showDialogLoading()
            if (binding.chbLoading.isChecked)
                viewModel.getLoading(thisView.text.toString())
            else
                reloadingViewModel.getReloading("bfea5de4-b0f0-11ee-911c-000c29ed8257")
            return true
        }
        return false
    }

    private fun initRvList() {
        loadingAdapter = LoadingAdapter(::onItemClicked)
        reloadingAdapter = ReloadingAdapter(::onItemClicked)
        with(binding.rvLoadingList) {
            adapter = loadingAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        with(binding.rvReloadingList) {
            adapter = reloadingAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    private fun onItemClicked(itemClicked: ItemClicked, loading: Loading) {
        when (itemClicked) {
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
        if (binding.chbLoading.isChecked)
            findNavController().navigate(R.id.action_loadingListFragment_to_loadingFragment, bundle)
        else
            findNavController().navigate(
                R.id.action_loadingListFragment_to_reloadingFragment,
                bundle
            )
    }

    private fun loadingByNumber(pair: Pair<Loading, List<LoadingEnableVisible>>) {
        val loading = pair.first
        closeDialogLoading()
        binding.etxtDocumentNumber.text?.clear()
        if (loading.ref.isNotEmpty())
            onItemClicked(ItemClicked.ITEM, loading)
        else
            toast(resources.getString(R.string.text_element_not_found))
    }

    private fun reloadingByNumber(pair: Pair<Loading, List<LoadingEnableVisible>>) {
        val reloading = pair.first
        closeDialogLoading()
        binding.etxtDocumentNumber.text?.clear()
        if (reloading.ref.isNotEmpty())
            onItemClicked(ItemClicked.ITEM, reloading)
        else
            toast(resources.getString(R.string.text_element_not_found))
    }

    private fun getLoadingFromCache(): String? {
        val sharedPreferences = requireContext().getSharedPreferences(
            Loading.LOADING_SHARED_PREFS,
            Context.MODE_PRIVATE
        )
        return sharedPreferences.getString(Loading.LOADING_SHARED_PREFS_KEY, null)
    }


    private fun showPbLoading(show: Boolean) {
        with(binding) {
            rvLoadingList.isVisible = !show
            pbLoading.isVisible = show
        }
    }
}
