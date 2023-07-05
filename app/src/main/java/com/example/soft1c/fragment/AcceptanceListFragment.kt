package com.example.soft1c.fragment

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soft1c.R
import com.example.soft1c.adapter.AcceptanceAdapter
import com.example.soft1c.databinding.FragmentAcceptanceListBinding
import com.example.soft1c.repository.model.Acceptance
import com.example.soft1c.repository.model.AcceptanceEnableVisible
import com.example.soft1c.repository.model.ItemClicked
import com.example.soft1c.utils.Utils
import com.example.soft1c.viewmodel.AcceptanceViewModel
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDate
import java.time.LocalDateTime

class AcceptanceListFragment :
    BaseFragment<FragmentAcceptanceListBinding>(FragmentAcceptanceListBinding::inflate) {

    private lateinit var acceptanceAdapter: AcceptanceAdapter
    private var acceptanceList: List<Acceptance>? = null
    private var sortedList: List<Acceptance> = emptyList()
    private val viewModel: AcceptanceViewModel by viewModels()
    private var showColumnZone = true
    private var showText = true
    private var isAscending = false
    private val user = Utils.user
    private var icon :Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getAcceptanceList()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.includeToolbar.toolbar.title =
//            resources.getString(R.string.text_title_acceptance_list)
        initUI()
        observeViewModels()
    }

    private fun observeViewModels() {
        viewModel.toastLiveData.observe(viewLifecycleOwner, ::toast)
        viewModel.acceptanceListLiveData.observe(viewLifecycleOwner) { it ->
            val list = it.sortedByDescending { LocalDateTime.parse(it.date) }
            if (sortedList.isNotEmpty() && !Utils.refreshList) {
                showAcceptanceList(sortedList)
            } else {
                sortedList = list
                showAcceptanceList(list)
            }
            acceptanceList = list
        }
        viewModel.acceptanceLiveData.observe(viewLifecycleOwner, ::acceptanceByNumber)
    }

    private fun showAcceptanceList(list: List<Acceptance>) {
        if (AcceptanceAdapter.ACCEPTANCE_GUID.isEmpty() && list.isNotEmpty()) {
            AcceptanceAdapter.ACCEPTANCE_GUID = list[0].ref
        }
        showPbLoading(false)
        acceptanceAdapter.submitList(checkMarks(list))
        showColumnZone()
        showText()
    }

    // TODO: Сделать сравнение не с локальной датой, а с серверной
    private fun acceptanceByNumber(pair: Pair<Acceptance, List<AcceptanceEnableVisible>>) {
        val acceptance = pair.first
        closeDialogLoading()
        binding.etxtDocumentNumber.text?.clear()
        if (acceptance.ref.isNotEmpty()) {
            AcceptanceFragment.IS_TODAY =
                LocalDateTime.parse(acceptance.date).toLocalDate() == LocalDate.now()
            onItemClicked(ItemClicked.ITEM, acceptance)
        }
        else
            toast(resources.getString(R.string.text_element_not_found))
    }

    private fun initUI() {
        if (Utils.refreshList) viewModel.getAcceptanceList()
        showPbLoading(true)
        initRvList()
        icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_search_off)
        val chbListener = View.OnClickListener {
            with(binding)
            {
                val isAccept = it.id == binding.chbAcceptance.id
                val isWeight = it.id == binding.chbWeight.id
                val isCapacity = it.id == binding.chbSize.id

                if ((it as CheckBox).isChecked) {
                    chbAcceptance.isChecked = isAccept
                    chbWeight.isChecked = isWeight
                    chbSize.isChecked = isCapacity
                }
                showAcceptanceList(sortedList)
            }
        }
        with(binding) {
            chbAcceptance.setOnClickListener(chbListener)
            chbWeight.setOnClickListener(chbListener)
            chbSize.setOnClickListener(chbListener)

            ivAdd.setOnClickListener {
                if (user.acceptanceCargo || user.isAdmin) {
                    findNavController().navigate(R.id.action_acceptanceFragment_to_acceptanceFragment)
                } else {
                    toast(getString(R.string.text_no_rights))
                }
            }
            ivBack.setOnClickListener {
                findNavController().navigate(R.id.action_acceptanceListFragment_to_mainFragment)
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
                        viewModel.getAcceptance(text)
                    }
                }

            })
            chbAcceptance.isChecked = true

            chbVisibiliy.setOnClickListener {
                showColumnZone = !showColumnZone
                initRvList()
                viewModel.getAcceptanceList()
                showColumnZone()
            }
            chbShowText.setOnClickListener {
                showText = !showText
                showText()
            }
            ivRefresh.setOnClickListener {
                initRvList()
                acceptanceList?.let {
                    showAcceptanceList(it)
                    sortedList = it
                    return@setOnClickListener }
                showPbLoading(true)
                viewModel.getAcceptanceList()
            }
            txtZone.setOnClickListener {
                isAscending = !isAscending
                showAcceptanceList(sortList("zone", isAscending))
            }
            txtDocumentNumber.setOnClickListener {
                isAscending = !isAscending
                showAcceptanceList(sortList("documentNumber", isAscending))
            }

            txtClient.setOnClickListener {
                isAscending = !isAscending
                showAcceptanceList(sortList("client", isAscending))
            }

            txtPackage.setOnClickListener {
                isAscending = !isAscending
                showAcceptanceList(sortList("package", isAscending))
            }
            txtEmptyWeight.setOnClickListener {
                isAscending = !isAscending
                showAcceptanceList(sortList("weight", isAscending))
            }
            txtEmptyCapacity.setOnClickListener {
                isAscending = !isAscending
                showAcceptanceList(sortList("capacity", isAscending))
            }

            ivSearch.setOnClickListener {
                AcceptanceAdapter.IS_CLICKABLE = true
                sortedList = acceptanceAdapter.updateFilteredItems()
                showAcceptanceList(sortedList)
                ivSearchOff.isEnabled = true
                icon?.setTint(Color.parseColor("#007D2D"))
                ivSearchOff.setImageDrawable(icon)
            }
            ivSearchOff.setOnClickListener {
                initRvList()
                AcceptanceAdapter.IS_CLICKABLE = false
                acceptanceList?.let {
                        it1 ->
                    sortedList = it1
                    showAcceptanceList(it1) }
                ivSearchOff.isEnabled = false
                icon?.setTint(Color.parseColor("#5C2920"))
                ivSearchOff.setImageDrawable(icon)
            }

            if (AcceptanceAdapter.IS_CLICKABLE){
                icon?.setTint(Color.parseColor("#007D2D"))
                ivSearchOff.setImageDrawable(icon)
            }else{
                icon?.setTint(Color.parseColor("#5C2920"))
                ivSearchOff.setImageDrawable(icon)
                ivSearchOff.isEnabled = false
            }
            ivBack.setOnClickListener { closeActivity() }
        }
    }

    private fun showColumnZone() {
        binding.txtZone.isVisible = showColumnZone
    }

    private fun showText() {
        binding.linearText.isVisible = showText
    }

    private fun initRvList() {
        acceptanceAdapter = AcceptanceAdapter(::onItemClicked, showColumnZone)
        with(binding.rvAcceptanceList) {
            adapter = acceptanceAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun checkMarks(list: List<Acceptance>): List<Acceptance> {
        if (binding.chbWeight.isChecked) {
            return list.filter { !it.weight }
        } else if (binding.chbSize.isChecked) {
            return list.filter { !it.capacity }
        }
        return list
    }

    private fun findOpenDocumentByNumber(eView: View, key: Int, event: KeyEvent): Boolean {
        val thisView = (eView as TextInputEditText)
        if (key == 66 && event.action == KeyEvent.ACTION_UP) {
            if (thisView.text!!.isEmpty()) {
                thisView.error = resources.getString(R.string.text_field_is_empyt)
                thisView.requestFocus()
                return false
            }
            thisView.error = null
            showDialogLoading()
            viewModel.getAcceptance(thisView.text.toString())
            return true
        }
        return false
    }


    private fun onItemClicked(itemClicked: ItemClicked, acceptance: Acceptance) {
        when (itemClicked) {
            ItemClicked.ITEM -> {
                val args = Bundle().apply {
                    putString(AcceptanceFragment.KEY_ACCEPTANCE_NUMBER, acceptance.number)
                    putString(AcceptanceSizeFragment.KEY_ACCEPTANCE_GUID, acceptance.ref)
                }
                AcceptanceFragment.IS_TODAY = true
                openAcceptanceDetail(args, acceptance.weight, acceptance.capacity)
            }
            else -> return
        }
    }

    private fun openAcceptanceDetail(bundle: Bundle, weight: Boolean, capacity: Boolean) {
        with(binding) {
            val action = when {
                chbAcceptance.isChecked && (user.acceptanceCargo || user.isAdmin) -> R.id.action_acceptanceFragment_to_acceptanceFragment
                chbWeight.isChecked && (!weight || AcceptanceFragment.IS_TODAY) && (user.weightAccess || user.isAdmin) -> R.id.action_acceptanceListFragment_to_acceptanceWeightFragment
                chbSize.isChecked && (!capacity || AcceptanceFragment.IS_TODAY) && (user.measureCargo || user.isAdmin) -> R.id.action_acceptanceListFragment_to_acceptanceSizeFragment
                else -> {
                    toast(getString(R.string.text_no_rights))
                    return
                }  // Do nothing and exit the function
            }

            findNavController().navigate(action, bundle)
        }
    }

    private fun closeActivity() {
        activity?.onBackPressed()
    }

    private fun showPbLoading(show: Boolean) {
        with(binding) {
            rvAcceptanceList.isVisible = !show
            pbLoading.isVisible = show
        }
    }
    private fun sortList(sortBy: String, ascending: Boolean): List<Acceptance> {
        sortedList = sortedList.sortedWith(
            when (sortBy) {
                "client" -> compareBy { it.client.trimStart('0').toInt() }
                "zone" -> compareBy { it.zone.toIntOrNull() ?: Int.MIN_VALUE }
                "documentNumber" -> compareBy { it.number.replace("[A-Z]".toRegex(), "").trimStart('0').toInt() }
                "package" -> compareBy { it._package.substringAfterLast(' ').toIntOrNull() }
                "weight" -> compareBy { it.weight }
                "capacity" -> compareBy<Acceptance> { it.capacity }
                else -> throw IllegalArgumentException("Invalid sort parameter: $sortBy")
            }.thenBy { it.number.replace("[A-Z]".toRegex(), "").trimStart('0').toInt() }
        )

        if (!ascending)
            sortedList = sortedList.reversed()

        return sortedList
    }

}