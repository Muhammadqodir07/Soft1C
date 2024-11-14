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
import com.example.soft1c.repository.model.FieldsAccess
import com.example.soft1c.repository.model.Filter
import com.example.soft1c.repository.model.Filter.ascending
import com.example.soft1c.repository.model.ItemClicked
import com.example.soft1c.utils.Utils
import com.example.soft1c.viewmodel.AcceptanceViewModel
import com.google.android.material.textfield.TextInputEditText
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
    private var icon: Drawable? = null
    private lateinit var operationType: String

    private var rvLastPosition: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getAcceptanceList()
        resetFilter()
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
        viewModel.toastResIdLiveData.observe(viewLifecycleOwner){
            toast(getString(it))
        }
        viewModel.acceptanceListLiveData.observe(viewLifecycleOwner) { it ->
            closeDialogLoading()
            val list = it.sortedByDescending { LocalDateTime.parse(it.date) }
            acceptanceList = list
            if (sortedList.isNotEmpty() && !Utils.refreshList) {
                showAcceptanceList(sortedList)
            } else {
                sortedList = list
                autoSort()
            }
        }
        viewModel.acceptanceLiveData.observe(viewLifecycleOwner, ::acceptanceByNumber)
    }

    private fun showAcceptanceList(list: List<Acceptance>) {
        if (AcceptanceAdapter.ACCEPTANCE_GUID.isEmpty() && list.isNotEmpty()) {
            AcceptanceAdapter.ACCEPTANCE_GUID = list[0].ref
        }
        showPbLoading(false)
        acceptanceAdapter.submitList(list)
        showColumnZone()
        showText()
    }

    private fun acceptanceByNumber(triple: Triple<Acceptance, FieldsAccess, String>) {
        val acceptance = triple.first
        closeDialogLoading()
        binding.etxtDocumentNumber.text?.clear()
        if (acceptance.ref.isNotEmpty()) {
            onItemClicked(ItemClicked.ITEM, acceptance)
        } else
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
                    if (count>=5 && count+start != 9){
                        val text = s.toString().trim()
                        showDialogLoading()
                        checkOperationType()
                        viewModel.getAcceptance(text, operationType)
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    val text = s.toString().trim()
                    if (text.length == 9) {
                        showDialogLoading()
                        checkOperationType()
                        viewModel.getAcceptance(text, operationType)
                    }
                }

            })
            etxtDocumentNumber.requestFocus()
            chbAcceptance.isChecked = true

            chbVisibiliy.setOnClickListener {
                showColumnZone = !showColumnZone
                initRvList()
                showAcceptanceList(sortedList)
                showColumnZone()
            }
            chbShowText.setOnClickListener {
                showText = !showText
                showText()
            }
            ivRefresh.setOnClickListener {
                initRvList()
                resetFilter()
                showPbLoading(true)
                viewModel.getAcceptanceList()
            }
            txtZone.setOnClickListener {
                isAscending = !isAscending
                ascending = Pair(Filter.ZONE, isAscending)
                showAcceptanceList(sortList(Filter.ZONE, isAscending))
            }
            txtDocumentNumber.setOnClickListener {
                isAscending = !isAscending
                ascending = Pair(Filter.DOCUMENT, isAscending)
                showAcceptanceList(sortList(Filter.DOCUMENT, isAscending))
            }

            txtClient.setOnClickListener {
                isAscending = !isAscending
                ascending = Pair(Filter.CLIENT, isAscending)
                showAcceptanceList(sortList(Filter.CLIENT, isAscending))
            }

            txtPackage.setOnClickListener {
                isAscending = !isAscending
                ascending = Pair(Filter.PACKAGE, isAscending)
                showAcceptanceList(sortList(Filter.PACKAGE, isAscending))
            }
            txtEmptyWeight.setOnClickListener {
                isAscending = !isAscending
                ascending = Pair(Filter.WEIGHT, isAscending)
                showAcceptanceList(sortList(Filter.WEIGHT, isAscending))
            }
            txtEmptyCapacity.setOnClickListener {
                isAscending = !isAscending
                ascending = Pair(Filter.SIZE, isAscending)
                showAcceptanceList(sortList(Filter.SIZE, isAscending))
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
                resetFilter()
                AcceptanceAdapter.IS_CLICKABLE = false
                acceptanceList?.let { it1 ->
                    sortedList = it1
                    showAcceptanceList(it1)
                }
                ivSearchOff.isEnabled = false
                icon?.setTint(Color.parseColor("#5C2920"))
                ivSearchOff.setImageDrawable(icon)
            }

            if (AcceptanceAdapter.IS_CLICKABLE) {
                icon?.setTint(Color.parseColor("#007D2D"))
                ivSearchOff.setImageDrawable(icon)
            } else {
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

    fun checkOperationType(){
        with(binding){
            if(chbAcceptance.isChecked){
                operationType = Utils.OperationType.ACCEPTANCE
            }else if (chbWeight.isChecked){
                operationType = Utils.OperationType.WEIGHT
            }else{
                operationType = Utils.OperationType.SIZE
            }
        }
    }


    private fun initRvList() {
        acceptanceAdapter = AcceptanceAdapter(::onItemClicked, showColumnZone)
        with(binding.rvAcceptanceList) {
            adapter = acceptanceAdapter
            layoutManager = LinearLayoutManager(requireContext())
            if (rvLastPosition != null){
                scrollToPosition(rvLastPosition!!)
                rvLastPosition = null
            }
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
            checkOperationType()
            viewModel.getAcceptance(thisView.text.toString(), operationType)
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
                openAcceptanceDetail(args)
            }
            else -> return
        }
    }

    private fun openAcceptanceDetail(bundle: Bundle) {
        with(binding) {
            val action = when {
                chbAcceptance.isChecked && (user.acceptanceCargo || user.isAdmin) -> R.id.action_acceptanceFragment_to_acceptanceFragment
                chbWeight.isChecked && (user.weightAccess || user.isAdmin) -> R.id.action_acceptanceListFragment_to_acceptanceWeightFragment
                chbSize.isChecked && (user.measureCargo || user.isAdmin) -> R.id.action_acceptanceListFragment_to_acceptanceSizeFragment
                else -> {
                    toast(getString(R.string.text_no_rights))
                    return
                }  // Do nothing and exit the function
            }
            rvLastPosition = rvAcceptanceList.computeVerticalScrollOffset()
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

    private fun sortList(sortBy: Int, ascending: Boolean): List<Acceptance> {
        sortedList = sortedList.sortedWith(
            when (sortBy) {
                Filter.CLIENT -> compareBy { it.client.code.toIntOrNull() ?: Int.MIN_VALUE }
                Filter.ZONE -> compareBy { it.zone.toIntOrNull() ?: Int.MIN_VALUE }
                Filter.DOCUMENT -> compareBy {
                    it.number.replace("[A-Z]".toRegex(), "").trimStart('0').toInt()
                }
                Filter.PACKAGE -> compareBy { it._package.substringAfterLast(' ').toIntOrNull() }
                Filter.WEIGHT -> compareBy { it.weight }
                Filter.SIZE -> compareBy<Acceptance> { it.capacity }
                else -> throw IllegalArgumentException("Invalid sort parameter: $sortBy")
            }.thenBy { it.number.replace("[A-Z]".toRegex(), "").trimStart('0').toInt() }
        )

        if (!ascending)
            sortedList = sortedList.reversed()

        return sortedList
    }

    private fun autoSort() {
        if (acceptanceList != null && acceptanceList != emptyList<Acceptance>()) {
            with(Filter) {
                if (client.isNotEmpty()) sortedList =
                    sortedList.filter { it.client.code.trimStart('0') == client }
                if (_package.isNotEmpty()) sortedList =
                    sortedList.filter { it._package.filter { !it.isDigit() } == _package }
                if (zone.isNotEmpty()) sortedList = sortedList.filter { it.zone == zone }
                if (weight != null) sortedList =
                    sortedList.filter { it.weight == weight }
                if (size != null) sortedList = sortedList.filter { it.capacity == size }
                if (ascending.first != -1) {
                    sortedList = sortedList.sortedWith(
                        when (ascending.first) {
                            CLIENT -> compareBy { it.client.code.trimStart('0').toInt() }
                            ZONE -> compareBy { it.zone.toIntOrNull() ?: Int.MIN_VALUE }
                            DOCUMENT -> compareBy {
                                it.number.replace("[A-Z]".toRegex(), "").trimStart('0').toInt()
                            }
                            PACKAGE -> compareBy {
                                it._package.substringAfterLast(' ').toIntOrNull()
                            }
                            WEIGHT -> compareBy { it.weight }
                            SIZE -> compareBy<Acceptance> { it.capacity }
                            else -> throw IllegalArgumentException("Invalid sort parameter: ${ascending.first}")
                        }.thenBy { it.number.replace("[A-Z]".toRegex(), "").trimStart('0').toInt() }
                    )
                }
            }

            showAcceptanceList(sortedList)
        }
    }

    private fun resetFilter() {
        with(Filter) {
            client = ""
            _package = ""
            zone = ""
            weight = null
            size = null
            ascending = Pair(-1, false)
        }
    }

}