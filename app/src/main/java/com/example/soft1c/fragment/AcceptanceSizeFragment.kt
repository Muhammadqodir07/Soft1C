package com.example.soft1c.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.KeyEvent
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soft1c.R
import com.example.soft1c.adapter.AcceptanceSizeAdapter
import com.example.soft1c.databinding.FragmentAcceptanceSizeBinding
import com.example.soft1c.repository.model.Acceptance
import com.example.soft1c.repository.model.DocumentType
import com.example.soft1c.repository.model.FieldsAccess
import com.example.soft1c.repository.model.ItemClicked
import com.example.soft1c.repository.model.SizeAcceptance
import com.example.soft1c.utils.Utils
import com.example.soft1c.viewmodel.AcceptanceViewModel
import timber.log.Timber

class AcceptanceSizeFragment :
    BaseFragment<FragmentAcceptanceSizeBinding>(FragmentAcceptanceSizeBinding::inflate) {

    private lateinit var acceptanceGuid: String
    private val viewModel: AcceptanceViewModel by viewModels()
    private lateinit var acceptanceSize: SizeAcceptance
    private lateinit var acceptance: Acceptance
    private lateinit var sizeAdapter: AcceptanceSizeAdapter
    private var indexSeatNumber = 0
    private var focusedEditText: EditText? = null
    private var hasFocusCanSave = false
    private var saveSize = false
    private lateinit var disabilityReason: SpannableStringBuilder
    private val user = Utils.user

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        acceptanceGuid = arguments?.getString(KEY_ACCEPTANCE_GUID, "") ?: ""
        val acceptanceNumber =
            arguments?.getString(AcceptanceFragment.KEY_ACCEPTANCE_NUMBER, "") ?: ""
        viewModel.getAcceptanceSizeData(acceptanceGuid)
        viewModel.getAcceptance(acceptanceNumber, Utils.OperationType.SIZE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observeViewModels()
    }

    private fun observeViewModels() {
        viewModel.connectionLiveData.observe(viewLifecycleOwner){
            if (it) {
                Toast.makeText(requireContext(), "Success connection", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "No connection", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.toastLiveData.observe(viewLifecycleOwner) {
            errorDialog(it, true)
        }
        viewModel.toastResIdLiveData.observe(viewLifecycleOwner) {
            errorDialog(getString(it), false)
        }
        viewModel.acceptanceSizeLiveData.observe(viewLifecycleOwner, ::acceptanceSizeDetail)
        viewModel.acceptanceLiveData.observe(viewLifecycleOwner, ::showAcceptanceDetail)
        viewModel.updateAcceptanceSizeLiveData.observe(viewLifecycleOwner) {
            binding.ivSave.isEnabled = true
            if (it.second) {
                if (it.first.isEmpty())
                    toast(resources.getString(R.string.text_successfully_saved))
                else {
                    errorDialog(it.first, false)
                    return@observe
                }
            } else {
                errorDialog(it.first, false)
                return@observe
            }
            Utils.refreshList = true
            closeDialogLoading()
            closeActivity()
        }
        viewModel.logSendingResultLiveData.observe(viewLifecycleOwner) {
            if (it) {
                toast("Success")
            } else {
                toast("Fail")
            }
        }
    }

    private fun showAcceptanceDetail(triple: Triple<Acceptance, FieldsAccess, String>) {
        if (triple.third.isNotEmpty()) {
            errorDialog(triple.third, false)
        }
        acceptance = triple.first
        with(binding) {
            txtDocNumber.text = acceptance.number
            txtCode.text = acceptance.client.code
            txtSeatCount.text = acceptance.countSeat.toString()
            txtPackage.text = acceptance._package.filter { !it.isDigit() }
        }
        checkEditRights(triple.second)
        showPbLoading(false)
    }

    private fun acceptanceSizeDetail(pair: Pair<SizeAcceptance, String>) {
        if (pair.second.isNotEmpty()) {
            errorDialog(pair.second, false)
        }
        acceptanceSize = pair.first
        showPbLoading(false)
        if (!acceptanceSize.recordAllowed){
            errorDialog(getString(R.string.txt_doc_not_printed), false)
        }
        with(binding) {
            txtWeight.text = acceptanceSize.allWeight.toString()
            txtSum.text = acceptanceSize.sum.toString()
            txtPriceM3.text = acceptanceSize.priceM3.toString()
            txtPriceWeight.text = acceptanceSize.priceWeight.toString()
            sizeAdapter.submitList(pair.first.dataArray)
        }
        fillIndexSeatNumber()
        enableFields()
    }

    private fun fillIndexSeatNumber() {
        indexSeatNumber = 0
        if (indexSeatNumber == 0) {
            if (acceptanceSize.dataArray.isEmpty())
                indexSeatNumber = 1
            else {
                val sizeAcceptance = acceptanceSize.dataArray[0]
                indexSeatNumber = sizeAcceptance.seatNumber
                fillFields(sizeAcceptance)
            }
        }
        binding.etxtCurrentIndex.setText(indexSeatNumber.toString())
    }

    private fun fillIndexSeatNumber(index: Int) {
        if (acceptanceSize.dataArray.isNotEmpty()) {
            if (acceptanceSize.dataArray.size > index) {
                val sizeAcceptance = acceptanceSize.dataArray[index]
                indexSeatNumber = sizeAcceptance.seatNumber
                with(binding) {
                    fillFields(sizeAcceptance)
                }
            }
        }
    }

    private fun fillFields(sizeAcceptance: SizeAcceptance.SizeData) {
        with(binding) {
            sizeAcceptance.length.takeIf { it != 0 }?.toString()?.let { etxtLength.setText(it) }
                ?: etxtLength.setText("")
            sizeAcceptance.height.takeIf { it != 0 }?.toString()?.let { etxtHeight.setText(it) }
                ?: etxtHeight.setText("")
            sizeAcceptance.width.takeIf { it != 0 }?.toString()?.let { etxtWidth.setText(it) }
                ?: etxtWidth.setText("")
        }
    }


    private fun setAutoCompleteFocusListener(view: View, hasFocus: Boolean) {
        view as AutoCompleteTextView
        with(binding) {
            when (view) {
                etxtSave -> if (hasFocus) {
                    if (hasFocusCanSave) {
                        hasFocusCanSave = !hasFocusCanSave
                        return@with
                    }
                    fillList()
                    Timber.d("setAutoCompleteFocusListener")
                }
            }
        }
    }

    private fun setEditTextFocusListener(view: View, hasFocus: Boolean) {
        view as EditText
        if (hasFocus) {
            view.selectAll()
            saveSize = false
            focusedEditText = view
        } else {
            focusedEditText = null
        }
    }

    private fun sizeItemClicked(sizeAcceptance: SizeAcceptance.SizeData, itemClicked: ItemClicked) {
        when (itemClicked) {
            ItemClicked.SIZE_ITEM -> {
                indexSeatNumber = sizeAcceptance.seatNumber
                with(binding) {
                    etxtCurrentIndex.setText(indexSeatNumber.toString())
                    fillFields(sizeAcceptance)
                    focusedEditText?.let { editText ->
                        editText.setSelection(editText.text.length)
                    }
                }

            }

            else -> {}
        }
    }

    private fun initUI() {
        sizeAdapter = AcceptanceSizeAdapter(::sizeItemClicked)
        showPbLoading(true)
        with(binding) {
//            includeToolbar.toolbar.title = resources.getString(R.string.text_title_acceptance)
//            includeToolbar.toolbar.setNavigationOnClickListener {
//                closeActivity()
//            }
            rvMain.adapter = sizeAdapter
            rvMain.setHasFixedSize(true)
            rvMain.layoutManager = LinearLayoutManager(requireContext())

            etxtChangeColumnsNumber.setOnKeyListener { _, key, keyEvent ->
                if (key == 66 && keyEvent.action == KeyEvent.ACTION_UP) {
                    hasFocusCanSave = true
                    etxtSave.requestFocus()
                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }
            etxtSave.setOnKeyListener(::autoCompleteOnKeyListener)
            etxtSave.setOnFocusChangeListener(::setAutoCompleteFocusListener)

            ivSave.setOnClickListener {
                ivSave.isEnabled = false
                viewModel.updateAcceptanceSize(acceptanceGuid, acceptanceSize)
                showDialogLoading()
            }

            etxtWidth.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    etxtWidth.selectAll()
                    saveSize = false
                    if (acceptanceSize.dataArray.last().seatNumber < indexSeatNumber) {
                        ivSave.requestFocus()
                    }
                }
            }
            etxtLength.setOnFocusChangeListener(::setEditTextFocusListener)
            etxtHeight.setOnFocusChangeListener(::setEditTextFocusListener)
            etxtChangeColumnsNumber.setOnFocusChangeListener(::setEditTextFocusListener)
            btnDocInfo.setOnClickListener {
                showEditWarningDialog(
                    disabilityReason,
                    showCheckBox = false,
                    navigateToLog = true
                ){
                    viewModel.checkConnection()
                }
            }
        }
    }

    private fun autoCompleteOnKeyListener(view: View, key: Int, keyEvent: KeyEvent): Boolean {
        if (key == 66 && keyEvent.action == KeyEvent.ACTION_UP) {
            view as AutoCompleteTextView
            with(binding) {
                when (view) {
                    etxtSave -> {
                        if (saveSize) {
                            ivSave.performClick()
                        }
                        fillList()
                        return true
                    }

                    else -> false
                }
            }
        }
        return false
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun fillList() {
        if (!checkedFillFields()) return
        val listData = acceptanceSize.dataArray.toMutableList()
        var lastChangedItemIndex = -1
        with(binding) {
            val seatNumberText = etxtChangeColumnsNumber.text.toString()
            val length = etxtLength.text.toString().toInt()
            val width = etxtWidth.text.toString().toInt()
            val height = etxtHeight.text.toString().toInt()
            when {
                seatNumberText.isEmpty() -> {
                    listData.forEach { listElement ->
                        if (listElement.seatNumber == indexSeatNumber) {
                            listElement.length = length
                            listElement.width = width
                            listElement.height = height
                            listElement.weight = length * width * height * 0.000001
                        }
                    }
                    indexSeatNumber += 1
                }

                else -> {
                    for (index in 0 until seatNumberText.toInt()) {
                        val indexList = listData.indexOf(listData.find {
                            it.seatNumber == indexSeatNumber
                        } ?: continue)
                        val listElement = listData[indexList]
                        listElement.length = length
                        listElement.width = width
                        listElement.height = height
                        listElement.weight = length * width * height * 0.000001
                        if (acceptanceSize.dataArray.last().seatNumber != indexSeatNumber) indexSeatNumber += 1
                        else saveSize = true
                        lastChangedItemIndex = indexList
                    }
                    if (listData.lastIndex != lastChangedItemIndex)
                        lastChangedItemIndex++
                }
            }
            etxtChangeColumnsNumber.setText("1")
            etxtLength.text.clear()
            etxtWidth.text.clear()
            etxtHeight.text.clear()
            binding.etxtCurrentIndex.setText(indexSeatNumber.toString())
            updateWeight(listData)
        }
        sizeAdapter.submitList(listData)
        sizeAdapter.notifyDataSetChanged()
        binding.rvMain.scrollToPosition(lastChangedItemIndex)
        sizeAdapter.focusNumber = sizeAdapter.currentList[lastChangedItemIndex].seatNumber
        fillIndexSeatNumber(lastChangedItemIndex)
        acceptanceSize.dataArray = listData
        if (!saveSize) {
            binding.etxtLength.requestFocus()
        }
    }

    private fun checkedFillFields(): Boolean {
        with(binding) {
            if (!checkedEditTextField(etxtLength)) return false
            if (!checkedEditTextField(etxtWidth)) return false
            if (!checkedEditTextField(etxtHeight)) return false
        }
        return true
    }

    private fun updateWeight(list: MutableList<SizeAcceptance.SizeData>) {
        var sum = 0.0
        for (data in list) {
            sum += data.weight
        }
        binding.txtWeight.text = String.format("%.6f", sum)
    }

    private fun checkedEditTextField(editText: EditText): Boolean {
        val checkField = editText.text.toString()
        return if (checkField.isEmpty()) {
            editText.error = resources.getString(R.string.text_field_is_empyt)
            false
        } else {
            editText.error = null
            true
        }
    }

    private fun enableFields() {
        with(binding) {
            if (acceptanceSize.recordAllowed) {
                etxtLength.requestFocus(); etxtLength.selectAll()
            }
            etxtLength.isEnabled = acceptanceSize.recordAllowed
            etxtWidth.isEnabled = acceptanceSize.recordAllowed
            etxtHeight.isEnabled = acceptanceSize.recordAllowed
            etxtChangeColumnsNumber.isEnabled = acceptanceSize.recordAllowed
//          btnOk.isEnabled = acceptanceSize.recordAllowed
            etxtSave.isEnabled = acceptanceSize.recordAllowed
            if (!acceptanceSize.recordAllowed)
                ivSave.requestFocus()
        }
    }

    private fun checkEditRights(fieldsAccess: FieldsAccess) {
        disabilityReason = fieldsAccess.getInaccessibilityReason(
            requireContext(),
            user,
            acceptance.isPrinted,
            acceptance.whoWeigh,
            DocumentType.ACCEPTANCE_SIZE
        )
        if ((user.username != acceptance.whoMeasure) && acceptance.capacity && !user.isAdmin) {
            binding.linearInputFields.children.forEach { it.isEnabled = false }
            binding.ivSave.isEnabled = true
            binding.btnDocInfo.isEnabled = true
            if (getSharedPref(Utils.Settings.SHOW_DISABILITY_DIALOG).isEmpty()) {
                showEditWarningDialog(
                    disabilityReason,
                    showCheckBox = true,
                    navigateToLog = false
                ){}
            }
        }
    }

    private fun closeActivity() {
        activity?.onBackPressed()
    }

    private fun showPbLoading(show: Boolean) {
        with(binding) {
            pbLoading.isVisible = show
            containerMain.isVisible = !show
        }
    }

    companion object {
        const val KEY_ACCEPTANCE_GUID = "acceptance_guid"
    }
}