package com.example.soft1c.fragment

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.soft1c.R
import com.example.soft1c.databinding.FragmentAcceptanceWeightBinding
import com.example.soft1c.repository.model.Acceptance
import com.example.soft1c.repository.model.FieldsAccess
import com.example.soft1c.utils.Utils
import com.example.soft1c.viewmodel.AcceptanceViewModel
import com.google.android.material.textfield.TextInputEditText
import com.maltaisn.calcdialog.CalcDialog
import java.math.BigDecimal

class AcceptanceWeightFragment :
    BaseFragment<FragmentAcceptanceWeightBinding>(FragmentAcceptanceWeightBinding::inflate), CalcDialog.CalcDialogCallback {

    private lateinit var acceptance: Acceptance
    private lateinit var acceptanceGuid: String
    private val viewModel: AcceptanceViewModel by viewModels()
    private var hasFocusCanSave = false
    private val user = Utils.user
    private var value: BigDecimal? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val acceptanceNumber = arguments?.getString(KEY_WEIGHT_ACCEPTANCE_NUMBER, "") ?: ""
        acceptanceGuid = arguments?.getString(AcceptanceSizeFragment.KEY_ACCEPTANCE_GUID, "") ?: ""
        acceptance = if (acceptanceNumber.isNotEmpty()) {
            viewModel.getAcceptance(acceptanceNumber, Utils.OperationType.WEIGHT)
            Acceptance(number = acceptanceNumber)
        } else {
            Acceptance(number = "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observeViewModels()
    }


    private fun observeViewModels() {
        viewModel.acceptanceLiveData.observe(viewLifecycleOwner, ::showDetails)
        viewModel.toastLiveData.observe(viewLifecycleOwner) {
            errorDialog(it, true)
        }
        viewModel.createUpdateLiveData.observe(viewLifecycleOwner, ::createUpdateAcceptance)
        viewModel.logSendingResultLiveData.observe(viewLifecycleOwner){
            if(it){
                toast("Success")
            }else{
                toast("Fail")
            }
        }
    }

    private fun createUpdateAcceptance(pair: Pair<Acceptance, String>) {
        if (pair.second.isNotEmpty()) {
            errorDialog(pair.second, false)
            return
        }
        Utils.refreshList = true
        closeActivity()
    }

    private fun initUI() {
        showPbLoading(true)
        showAcceptance()
        val calcDialog = CalcDialog()
        with(binding) {
            setInitFocuses()
//            includeToolbar.toolbar.title = resources.getString(R.string.text_title_acceptance)
//            includeToolbar.toolbar.setNavigationOnClickListener {
//                closeActivity()
//            }

            btnClose.setOnClickListener {
                closeActivity()
            }
            btnCloseCopy.setOnClickListener {
                closeActivity()
            }
            btnCalc.setOnClickListener {
                calcDialog.settings.isZeroShownWhenNoValue = true
                calcDialog.settings.initialValue = etxtWeight.text.toString().toDouble().toBigDecimal()
                calcDialog.settings.isSignBtnShown = false
                calcDialog.settings.isExpressionShown = true
                calcDialog.show(childFragmentManager, "calc_dialog") }
            etxtSave.setOnKeyListener(::autoCompleteOnKeyListener)
            etxtSave.setOnFocusChangeListener(::setAutoCompleteFocusListener)
            etxtSaveCopy.setOnFocusChangeListener(::setAutoCompleteFocusListener)

//            btnSave.setOnClickListener {
//                createUpdateAcceptance()
//            }
//            btnSaveCopy.setOnClickListener {
//                createUpdateAcceptance()
//            }
            etxtWeight.setOnKeyListener(::customSetOnKeyListener)
            btnDocInfo.setOnClickListener {
                logFileDialog(viewModel)
            }
        }
    }

    private fun setAutoCompleteFocusListener(view: View, hasFocus: Boolean) {
        view as AutoCompleteTextView
        with(binding) {
            when (view) {
                etxtSave, etxtSaveCopy -> if (hasFocus) {
                    if (hasFocusCanSave) {
                        hasFocusCanSave = !hasFocusCanSave
                        return@with
                    }
                    createUpdateAcceptance()
                }
            }
        }
    }

    private fun autoCompleteOnKeyListener(view: View, key: Int, keyEvent: KeyEvent): Boolean {
        if (key == 66 && keyEvent.action == KeyEvent.ACTION_DOWN) {
            view as AutoCompleteTextView
            with(binding) {
                when (view) {
                    etxtSave -> {
                        createUpdateAcceptance()
                        return true
                    }
                    else -> false
                }
            }
        }
        return false
    }

    private fun customSetOnKeyListener(view: View, key: Int, keyEvent: KeyEvent): Boolean {
        if (key == 66 && keyEvent.action == KeyEvent.ACTION_UP) {
            with(binding) {
                val etxtView = view as TextInputEditText
                if (etxtView.text!!.isEmpty()) {
                    etxtView.error = resources.getString(R.string.text_field_is_empyt)
                    return true
                }
                when (etxtView) {
                    etxtWeight -> {
                        hasFocusCanSave = true
                        etxtSave.requestFocus()
                        return true
                    }
                    else -> {
                        return false
                    }
                }
            }
        }
        return false
    }

    private fun createUpdateAcceptance() {
        with(binding) {
            val weight = etxtWeight.text.toString()
            if (weight.isNotEmpty())
                acceptance.allWeight = weight.toDouble()
            acceptance.type = 1
        }
        viewModel.createUpdateAcceptance(acceptance)
    }

    private fun showPbLoading(show: Boolean) {
        with(binding) {
            pbLoading.isVisible = show
            scrollMain.isVisible = !show
        }

    }

    private fun closeActivity() {
        activity?.onBackPressed()
    }

    private fun showDetails(triple: Triple<Acceptance, FieldsAccess, String>) {
        if (triple.third.isNotEmpty()){
            errorDialog(triple.third, false)
        }
        acceptance = triple.first
        if (this.acceptance.ref.isEmpty()) {
            binding.pbLoading.isVisible = false
            return
        }
        showAcceptance()
        setInitFocuses()
        checkEditRights(triple.second)
        showPbLoading(false)
    }

    private fun showAcceptance() {
        with(binding) {
            setCheckEmptyText(txtZone, acceptance.zone)
            etxtCodeClient.setText(acceptance.client)
            etxtSeatsNumber.setText(acceptance.countSeat.toString())
            etxtDocumentNumber.setText(acceptance.number)
            setCheckEmptyText(txtPackage, acceptance._package)
//            chbExclamation.isChecked = acceptance.glass
//            chbCurrency.isChecked = acceptance.expensive
//            chbArrow.isChecked = acceptance.notTurnOver
//            chbBrand.isChecked = acceptance.brand
//            chbZ.isChecked = acceptance.z
            etxtWeight.setText(acceptance.allWeight.toString())
        }
    }


    private fun setInitFocuses() {
        with(binding) {
            with(etxtWeight) {
                requestFocus()
                val length = text?.length ?: 0
                if (length > 0) setSelection(length)
            }
        }
    }

    private fun setCheckEmptyText(textV: TextView, text: String) {
        if (text.isNotEmpty()) {
            textV.text = text
        }
    }

    private fun checkEditRights(fieldsAccess: FieldsAccess){
        if ((user.username != acceptance.whoWeigh) && acceptance.weight && !user.isAdmin) {
            binding.etxtWeight.isEnabled=false
            binding.btnCalc.isEnabled = false
            toast(getString(R.string.weighEnable1))
            return
        }

        if (!user.weightAccess) {
            binding.etxtWeight.isEnabled=false
            binding.btnCalc.isEnabled = false
            toast(getString(R.string.weighEnable2))
            return
        }

        if (!user.isAdmin && !fieldsAccess.weightEnable) {
            binding.etxtWeight.isEnabled=false
            binding.btnCalc.isEnabled = false
            toast(getString(R.string.weighEnable3))
            return
        }

        if (fieldsAccess.readOnly) {
            binding.etxtWeight.isEnabled=false
            binding.btnCalc.isEnabled = false
            toast(getString(R.string.weighEnable4))
            return
        }
        binding.etxtWeight.selectAll()
    }

    companion object {
        const val KEY_WEIGHT_ACCEPTANCE_NUMBER = "acceptance_number"
    }

    override fun onValueEntered(requestCode: Int, value: BigDecimal?) {
        binding.etxtWeight.setText(value?.toDouble().toString())
        hasFocusCanSave = true
        binding.etxtSave.requestFocus()
    }

//    override fun onValueEntered(requestCode: Int, value: BigDecimal?) {
//        this.value = value
//    }
}
