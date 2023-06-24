package com.example.soft1c.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.soft1c.R
import com.example.soft1c.databinding.FragmentAcceptanceBinding
import com.example.soft1c.repository.AcceptanceRepository
import com.example.soft1c.repository.model.*
import com.example.soft1c.utils.Utils
import com.example.soft1c.utils.Utils.acceptanceCopyList
import com.example.soft1c.viewmodel.AcceptanceViewModel
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import kotlin.collections.set

class AcceptanceFragment :
    BaseFragment<FragmentAcceptanceBinding>(FragmentAcceptanceBinding::inflate) {

    private var clientFound = false
    private lateinit var acceptance: Acceptance
    private var fieldsAccess = FieldsAccess()
    private val user = Utils.user

    private val viewModel: AcceptanceViewModel by viewModels()
    private var hasFocusCanSave = false
    private var documentCreate = false
    private lateinit var propertyList: List<AcceptanceEnableVisible>
    private var isCopiedAcceptance = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val acceptanceNumber = arguments?.getString(KEY_ACCEPTANCE_NUMBER, "") ?: ""
        acceptance = if (acceptanceNumber.isNotEmpty()) {
            viewModel.getAcceptance(acceptanceNumber)
            Acceptance(number = acceptanceNumber)
        } else {
            Acceptance(number = "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCopyForm()
        initUI()
        findAndFillAnyCopiedModel(
            Triple(Utils.addressess, Utils.ObjectModelType.ADDRESS, binding.etxtStoreAddress),
            Triple(Utils.productTypes, Utils.ObjectModelType.PRODUCT_TYPE, binding.etxtProductType),
            Triple(Utils.packages, Utils.ObjectModelType._PACKAGE, binding.etxtPackage),
            Triple(Utils.zones, Utils.ObjectModelType.ZONE, binding.etxtZone)
        )
        observeViewModels()
        checkEditRights()
    }

    private fun observeViewModels() {
        viewModel.acceptanceLiveData.observe(viewLifecycleOwner, ::showDetails)
        viewModel.fieldLiveData.observe(viewLifecycleOwner, ::fieldsAccess)
        viewModel.toastLiveData.observe(viewLifecycleOwner) {
            documentCreate = false
            toast(it)
        }
        viewModel.clientLiveData.observe(viewLifecycleOwner, ::clientObserve)
        viewModel.createUpdateLiveData.observe(viewLifecycleOwner, ::createUpdateAcceptance)
    }

    private fun createUpdateAcceptance(pair: Pair<Acceptance, String>) {
        if (pair.second.isNotEmpty()) {
            toast(pair.second)
            documentCreate = false
            return
        }
        Utils.refreshList = true
        if (NEXT_IS_NEED) {
            createCopyForm()
        } else {
            closeActivity()
        }
    }

    private fun clientObserve(pair: Pair<Client, Boolean>) {
        clientFound = pair.second
        enableFieldsAfterFieldClient(clientFound)
        closeDialogLoading()
        acceptance.client = pair.first.code
        setInitFocuses()
        showAcceptance()
        if (clientFound) {
            binding.etxtCardNumber.requestFocus()
        }
    }

    private fun initUI() {
        if (acceptance.number.isNotEmpty() && !clientFound)
            showPbLoading(true)
        else if (acceptance.number.isEmpty()) {
            enableFieldsAfterFieldClient(clientFound)
            setInitFocuses()
        }
        showAcceptance()
        with(binding) {
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
            etxtCodeClient.setOnKeyListener(::customSetOnKeyListener)
            etxtCardNumber.setOnKeyListener(::customSetOnKeyListener)
            etxtStorePhone.setOnKeyListener(::customSetOnKeyListener)
            etxtSeatsNumberCopy.setOnKeyListener(::customSetOnKeyListener)
            etxtPackageCount.setOnKeyListener(::customSetOnKeyListener)
            etxtCountInPackage.setOnKeyListener(::customSetOnKeyListener)

            etxtZone.setAdapter(
                ArrayAdapter(requireContext(),
                    android.R.layout.simple_list_item_1, Utils.zones.map {
                        (it as AnyModel.Zone).name
                    })
            )
            etxtZone.setOnKeyListener(::autoCompleteOnKeyListener)
            etxtZone.setOnItemClickListener { parent, view, position, _ ->
                val selectedModel = parent.getItemAtPosition(position) as String
                findAndFillAnySelectedModel(
                    Utils.zones,
                    Utils.ObjectModelType.ZONE,
                    etxtZone,
                    selectedModel
                )
                view?.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                view?.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                etxtCodeClient.requestFocus()
                if (etxtCardNumber.isEnabled && etxtCardNumber.isVisible) {
                    etxtCardNumber.requestFocus()

                }
            }

            etxtStoreAddress.setAdapter(
                ArrayAdapter(requireContext(),
                    android.R.layout.simple_list_item_1, Utils.addressess.map {
                        (it as AnyModel.AddressModel).name
                    })
            )
            etxtStoreAddress.setOnKeyListener(::autoCompleteOnKeyListener)
            etxtStoreAddress.setOnItemClickListener { parent, view, position, _ ->
                val selectedModel = parent.getItemAtPosition(position) as String
                findAndFillAnySelectedModel(
                    Utils.addressess,
                    Utils.ObjectModelType.ADDRESS,
                    binding.etxtStoreAddress,
                    selectedModel
                )
                view?.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                view?.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                if (etxtStoreNumber.isEnabled && etxtStoreNumber.isVisible) {
                    etxtStoreNumber.requestFocus()
                }
            }
            etxtProductType.text.clear()
            etxtProductType.setAdapter(
                ArrayAdapter(requireContext(),
                    android.R.layout.simple_list_item_1, Utils.productTypes.map {
                        (it as AnyModel.ProductType).name
                    })
            )
            etxtProductType.setOnKeyListener(::autoCompleteOnKeyListener)
            etxtProductType.setOnItemClickListener { parent, view, position, _ ->
                val selectedModel = parent.getItemAtPosition(position) as String
                findAndFillAnySelectedModel(
                    Utils.productTypes,
                    Utils.ObjectModelType.PRODUCT_TYPE,
                    etxtProductType,
                    selectedModel
                )
                view?.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                view?.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                if (etxtPackage.isEnabled && etxtPackage.isVisible) {
                    etxtPackage.requestFocus()
                }
            }

            etxtPackage.setAdapter(
                ArrayAdapter(requireContext(),
                    android.R.layout.simple_list_item_1, Utils.packages.map {
                        (it as AnyModel.PackageModel).name
                    })
            )
            etxtPackage.setOnKeyListener(::autoCompleteOnKeyListener)
            etxtPackage.setOnItemClickListener { parent, view, position, _ ->
                val selectedModel = parent.getItemAtPosition(position) as String
                findAndFillAnySelectedModel(
                    Utils.packages,
                    Utils.ObjectModelType._PACKAGE,
                    etxtPackage,
                    selectedModel
                )
                view?.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                view?.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                if (etxtSeatsNumberCopy.isEnabled && etxtSeatsNumberCopy.isVisible) {
                    etxtSeatsNumberCopy.requestFocus()
                }
            }
            etxtSave.setOnKeyListener(::autoCompleteOnKeyListener)
            etxtSave.setOnFocusChangeListener(::setAutoCompleteFocusListener)
            etxtSaveCopy.setOnFocusChangeListener(::setAutoCompleteFocusListener)

            etxtCodeClient.setOnFocusChangeListener(::etxtFocusChangeListener)
            etxtStoreNumber.setOnFocusChangeListener(::etxtFocusChangeListener)
            etxtCardNumber.setOnFocusChangeListener(::etxtFocusChangeListener)
            etxtAutoNumber.setOnFocusChangeListener(::etxtFocusChangeListener)
            etxtRepresentative.setOnFocusChangeListener(::etxtFocusChangeListener)
            etxtCountInPackage.setOnFocusChangeListener(::etxtFocusChangeListener)
            etxtPackageCount.setOnFocusChangeListener(::etxtFocusChangeListener)
            etxtSeatsNumberCopy.setOnFocusChangeListener(::etxtFocusChangeListener)
            etxtStorePhone.setOnFocusChangeListener(::etxtFocusChangeListener)
            etxtZone.setOnFocusChangeListener(::etxtAutoCompleteFocusChangeListener)
            etxtStoreAddress.setOnFocusChangeListener(::etxtAutoCompleteFocusChangeListener)
            etxtProductType.setOnFocusChangeListener(::etxtAutoCompleteFocusChangeListener)
            etxtPackage.setOnFocusChangeListener(::etxtAutoCompleteFocusChangeListener)

            chbZ.setOnClickListener(::setCheckResult)
            chbExclamation.setOnClickListener(::setCheckResult)
            chbCurrency.setOnClickListener(::setCheckResult)
            chbArrow.setOnClickListener(::setCheckResult)
            chbBrand.setOnClickListener(::setCheckResult)

//            btnSave.setOnClickListener {
//                createUpdateAcceptance()
//            }
//            btnSaveCopy.setOnClickListener {
//                createUpdateAcceptance()
//            }
            btnPrint.setOnClickListener {
                if (etxtDocumentNumber.text?.isNotEmpty() == true) {
                    val intent = Intent(requireContext(), PrinterActivity::class.java)
                    intent.putExtra("docNumber", etxtDocumentNumber.text.toString())
                    intent.putExtra("clientNumber", etxtCodeClient.text.toString())
                    intent.putExtra("seatsNumber", etxtSeatsNumber.text.toString())
                    intent.putExtra("date", acceptance.date)
                    val macAddress = getMacAddressFromCache(requireActivity())
                    if (macAddress != null) {
                        intent.putExtra("macAddress", macAddress)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(context, R.string.text_empty_document, Toast.LENGTH_SHORT).show()
                }
            }
            btnCopy.setOnClickListener {
                createCopyForm()
            }
            btnCop.setOnClickListener {
                createCopyForm()
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
                    if (acceptanceCopyList.isNotEmpty()) {
                        acceptanceCopyList.forEach { acceptance ->
                            viewModel.createUpdateAcceptance(acceptance)
                        }
                    }
                    createUpdateAcceptance()
                }
            }
        }
    }

    private fun createUpdateAcceptance() {
        if (documentCreate) return
        documentCreate = !documentCreate
        fillAcceptance()
        viewModel.createUpdateAcceptance(acceptance)
    }

    private fun createCopyForm() {
        with(binding) {
            fillAcceptance()
            acceptanceCopyList.add(acceptance)
            val innerBundle = Bundle()
            innerBundle.putString("acceptanceIdCard", etxtCardNumber.text.toString())
            innerBundle.putString("acceptanceAutoNum", etxtAutoNumber.text.toString())
            innerBundle.putString("acceptanceAddress", etxtStoreAddress.text.toString())
            innerBundle.putString("acceptanceStore", etxtStoreNumber.text.toString())
            innerBundle.putString("acceptanceRepresentative", etxtRepresentative.text.toString())
            innerBundle.putString("acceptancePhoneNum", etxtStorePhone.text.toString())
            innerBundle.putString("acceptanceProductType", etxtProductType.text.toString())
            innerBundle.putString("acceptancePackage", etxtPackage.text.toString())
            if (NEXT_IS_NEED) {
                innerBundle.putString("acceptancePackageCount", etxtPackageCount.text.toString())
                innerBundle.putString("acceptanceZone", etxtZone.text.toString())
                innerBundle.putString("acceptanceClient", etxtCodeClient.text.toString())
            }
            findNavController().navigate(
                R.id.action_acceptanceFragment_to_acceptanceFragment,
                innerBundle
            )
        }
    }

    private fun fillAcceptance() {
        with(binding) {
            acceptance.client = etxtCodeClient.text.toString()
            acceptance.autoNumber = etxtAutoNumber.text.toString()
            acceptance.idCard = etxtCardNumber.text.toString()
            acceptance.storeName = etxtStoreNumber.text.toString()
            acceptance.representativeName = etxtRepresentative.text.toString()
            acceptance.phoneNumber = etxtStorePhone.text.toString()
            val seatCount = etxtSeatsNumberCopy.text.toString()
            if (seatCount.isNotEmpty())
                acceptance.countSeat = seatCount.toInt()
            val packageCount = etxtPackageCount.text.toString()
            if (packageCount.isNotEmpty())
                acceptance.countPackage = packageCount.toInt()
            val countInPackage = etxtCountInPackage.text.toString()
            if (countInPackage.isNotEmpty())
                acceptance.countInPackage = countInPackage.toInt()
        }
    }

    private fun loadCopyForm() {
        arguments.let { args ->
            if (args != null) {
                acceptance.idCard = args.getString("acceptanceIdCard").toString()
                acceptance.autoNumber = args.getString("acceptanceAutoNum").toString()
                acceptance.storeAddressName = args.getString("acceptanceAddress").toString()
                acceptance.storeName = args.getString("acceptanceStore").toString()
                acceptance.representativeName =
                    args.getString("acceptanceRepresentative").toString()
                acceptance.phoneNumber = args.getString("acceptancePhoneNum").toString()
                acceptance.productTypeName = args.getString("acceptanceProductType").toString()
                acceptance._package = args.getString("acceptancePackage").toString()
                if (NEXT_IS_NEED) {
                    acceptance.countPackage = args.getString("acceptancePackageCount")!!.toInt()
                    acceptance.zone = args.getString("acceptanceZone").toString()
                    acceptance.client = args.getString("acceptanceClient").toString()
                    acceptance.productTypeName = ""
                    acceptance._package = ""
                    acceptance.batchGuid = BATCH_GUID
                    viewModel.getClient(acceptance.client)
                }
                isCopiedAcceptance = true
            }
        }
    }

    private fun setCheckResult(view: View) {
        view as CheckBox
        with(binding) {
            when (view) {
                chbZ -> {
                    acceptance.z = chbZ.isChecked
                }
                chbExclamation -> {
                    acceptance.glass = chbExclamation.isChecked
                }
                chbCurrency -> {
                    acceptance.expensive = chbCurrency.isChecked
                }
                chbArrow -> {
                    acceptance.notTurnOver = chbArrow.isChecked
                }
                chbBrand -> {
                    acceptance.brand = chbBrand.isChecked
                }
            }
        }
    }

    private fun autoCompleteOnKeyListener(view: View, key: Int, keyEvent: KeyEvent): Boolean {
        if (key == 66 && keyEvent.action == KeyEvent.ACTION_UP) {
            view as AutoCompleteTextView
            with(binding) {
                when (view) {
                    etxtZone -> {
                        findAndFillAnyModel(
                            Utils.zones,
                            Utils.ObjectModelType.ZONE,
                            view
                        )
                        if (etxtCodeClient.isEnabled && etxtCodeClient.isVisible) {
                            etxtCodeClient.requestFocus()
                            return true
                        }
                        return false
                    }
                    etxtStoreAddress -> {
                        findAndFillAnyModel(
                            Utils.addressess,
                            Utils.ObjectModelType.ADDRESS,
                            view
                        )
                        if (etxtStoreNumber.isEnabled && etxtStoreNumber.isVisible) {
                            etxtStoreNumber.requestFocus()
                            return true
                        }
                        return false
                    }
                    etxtProductType -> {
                        findAndFillAnyModel(
                            Utils.productTypes,
                            Utils.ObjectModelType.PRODUCT_TYPE,
                            view
                        )
                        if (etxtPackage.isEnabled && etxtPackage.isVisible) {
                            etxtPackage.requestFocus()
                            return true
                        }
                        return false
                    }
                    etxtPackage -> {
                        findAndFillAnyModel(
                            Utils.packages,
                            Utils.ObjectModelType._PACKAGE,
                            view
                        )
                        if (etxtSeatsNumberCopy.isEnabled && etxtSeatsNumberCopy.isVisible) {
                            etxtSeatsNumberCopy.requestFocus()
                            return true
                        }
                        return false
                    }
                    else -> return false
                }
            }
        } else if (key == 66 && keyEvent.action == KeyEvent.ACTION_DOWN) {
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


    private fun findAndFillAnyModel(
        anyModelList: List<AnyModel>,
        model: Int,
        view: AutoCompleteTextView,
    ) {
        var textElement = when {
            view.text.isNotEmpty() -> view.text.toString()
            view.text.isEmpty() -> return
            else -> ""
        }
        textElementFound(anyModelList, model, view, textElement)
        if (view.text.isEmpty()) {
            textElement = when {
                view.adapter.count != 0 -> view.adapter.getItem(0).toString()
                else -> ""
            }
            textElementFound(anyModelList, model, view, textElement)
        }
    }

    private fun findAndFillAnyCopiedModel(vararg modelArgs: Triple<List<AnyModel>, Int, AutoCompleteTextView>) {
        if (isCopiedAcceptance) {
            for (modelArg in modelArgs) {
                val anyModelList = modelArg.first
                val model = modelArg.second
                val view = modelArg.third

                var textElement = when {
                    view.text.isNotEmpty() -> view.text.toString()
                    view.text.isEmpty() -> return
                    else -> ""
                }
                textElementFound(anyModelList, model, view, textElement)
                if (view.text.isEmpty()) {
                    textElement = when {
                        view.adapter.count != 0 -> view.adapter.getItem(0).toString()
                        else -> ""
                    }
                    textElementFound(anyModelList, model, view, textElement)
                }
            }
        }
    }

    private fun findAndFillAnySelectedModel(
        anyModelList: List<AnyModel>,
        model: Int,
        view: AutoCompleteTextView,
        selectedModel: String
    ) {
        var textElement = when {
            selectedModel.isNotEmpty() -> selectedModel
            view.text.isNotEmpty() -> view.text.toString()
            view.text.isEmpty() -> return
            else -> ""
        }
        textElementFound(anyModelList, model, view, textElement)
        if (view.text.isEmpty()) {
            textElement = when {
                view.adapter.count != 0 -> view.adapter.getItem(0).toString()
                else -> ""
            }
            textElementFound(anyModelList, model, view, textElement)
        }
    }

    private fun textElementFound(
        anyModelList: List<AnyModel>,
        model: Int,
        view: AutoCompleteTextView,
        textElement: String,
    ) {
        val element = anyModelList.find {
            when (it) {
                is AnyModel.ProductType -> it.name == textElement
                is AnyModel.PackageModel -> it.name == textElement
                is AnyModel.Zone -> it.name == textElement
                is AnyModel.AddressModel -> it.name == textElement
            }
        }
        when (model) {
            Utils.ObjectModelType.PRODUCT_TYPE -> {
                if (element != null) {
                    element as AnyModel.ProductType
                    acceptance.productTypeName = element.name
                    acceptance.productType = element.ref
                    view.setText(element.name)
                } else {
                    acceptance.productTypeName = ""
                    acceptance.productType = ""
                    view.text.clear()
                }
            }
            Utils.ObjectModelType.ADDRESS -> {
                if (element != null) {
                    element as AnyModel.AddressModel
                    acceptance.storeAddressName = element.name
                    acceptance.storeUid = element.ref
                    view.setText(element.name)
                } else {
                    acceptance.storeAddressName = ""
                    acceptance.storeUid = ""
                    view.text.clear()
                }
            }
            Utils.ObjectModelType.ZONE -> {
                if (element != null) {
                    element as AnyModel.Zone
                    acceptance.zone = element.name
                    acceptance.zoneUid = element.ref
                    view.setText(element.name)
                } else {
                    acceptance.zone = ""
                    acceptance.zoneUid = ""
                    view.text.clear()
                }
            }
            Utils.ObjectModelType._PACKAGE -> {
                if (element != null) {
                    element as AnyModel.PackageModel
                    acceptance._package = element.name
                    acceptance.packageUid = element.ref
                    view.setText(element.name)
                } else {
                    acceptance._package = ""
                    acceptance.packageUid = ""
                    view.text.clear()
                }
            }
            else -> {}
        }
    }

    private fun customSetOnKeyListener(view: View, key: Int, keyEvent: KeyEvent): Boolean {
        if (key == 66 && keyEvent.action == KeyEvent.ACTION_UP) {
            with(binding) {
                val etxtView = view as TextInputEditText
//                if (etxtView.text!!.isEmpty()) {
//                    etxtView.error = resources.getString(R.string.text_field_is_empyt)
//                    return true
//                }
                when (etxtView) {
                    etxtCodeClient -> {
                        showDialogLoading()
                        viewModel.getClient(etxtView.text.toString())
                        return true
                    }
                    etxtCardNumber -> {
                        if (etxtStoreAddress.isEnabled && etxtStoreAddress.isVisible) {
                            etxtStoreAddress.requestFocus()
                            return true
                        }
                        return false
                    }
                    etxtStorePhone -> {
                        if (etxtProductType.isEnabled && etxtProductType.isVisible) {
                            etxtProductType.requestFocus()
                            return true
                        }
                        return false
                    }
                    etxtSeatsNumberCopy -> {
                        if (etxtPackageCount.isEnabled && etxtPackageCount.isVisible) {
                            etxtPackageCount.requestFocus()
                            return true
                        }
                        return false
                    }
                    etxtPackageCount -> {
                        if (etxtCountInPackage.isEnabled && etxtCountInPackage.isVisible) {
                            etxtCountInPackage.requestFocus()
                            return true
                        }
                        return false
                    }
                    etxtCountInPackage -> {
                        hasFocusCanSave = true
                        etxtSave.requestFocus()
                        return true
                    }
                    else -> {
                        return false
                    }
                }
            }
        } else if (key == 66 && keyEvent.action == KeyEvent.ACTION_DOWN) {
            with(binding) {
                //                if (etxtView.text!!.isEmpty()) {
//                    etxtView.error = resources.getString(R.string.text_field_is_empyt)
//                    return true
//                }
                when (view as TextInputEditText) {
                    etxtCountInPackage -> {
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

    private fun enableFieldsAfterFieldClient(enable: Boolean) {
        with(binding) {
            etxtAutoNumber.isEnabled = enable
            etxtCardNumber.isEnabled = enable
            etxtStoreNumber.isEnabled = enable
            etxtRepresentative.isEnabled = enable
            etxtStorePhone.isEnabled = enable
            etxtSeatsNumber.isEnabled = enable
            etxtPackageCount.isEnabled = enable
            etxtCountInPackage.isEnabled = enable
            etxtSeatsNumberCopy.isEnabled = enable
            chbBrand.isEnabled = enable
            chbArrow.isEnabled = enable
            chbCurrency.isEnabled = enable
            chbExclamation.isEnabled = enable
            chbZ.isEnabled = enable
            chPassport.isEnabled = enable
        }
    }

    private fun fieldsEnable(enable: Boolean) {
        with(binding) {
            etxtZone.isEnabled = enable
            etxtSeatsNumber.isEnabled = enable
            etxtCodeClient.isEnabled = enable
            etxtAutoNumber.isEnabled = enable
            chPassport.isEnabled = enable
            etxtCardNumber.isEnabled = enable
            chbArrow.isEnabled = enable
            chbBrand.isEnabled = enable
            chbCurrency.isEnabled = enable
            chbExclamation.isEnabled = enable
            chbZ.isEnabled = enable
            etxtStoreAddress.isEnabled = enable
            etxtStoreNumber.isEnabled = enable
            etxtRepresentative.isEnabled = enable
            etxtStorePhone.isEnabled = enable
            etxtPackage.isEnabled = enable
            etxtProductType.isEnabled = enable
            etxtSeatsNumberCopy.isEnabled = enable
            etxtPackageCount.isEnabled = enable
            etxtCountInPackage.isEnabled = enable
        }
    }

    private fun showPbLoading(show: Boolean) {
        with(binding) {
            pbLoading.isVisible = show
            scrollMain.isVisible = !show
        }
    }

    private fun closeActivity() {
        acceptanceCopyList.clear()
        activity?.onBackPressed()
    }

    private fun showDetails(pair: Pair<Acceptance, List<AcceptanceEnableVisible>>) {
        acceptance = pair.first
        ACCEPTANCE = acceptance
        propertyList = pair.second
        if (this.acceptance.ref.isEmpty()) {
            binding.pbLoading.isVisible = false
            return
        }
        clientFound = true
        showAcceptance()
        setInitFocuses()
        showPbLoading(false)
        enableVisibleList()
        if (acceptance.ref.isNotEmpty()) {
            viewModel.getFieldsAccess(acceptance.ref, Utils.OperationType.ACCEPTANCE)
        }
    }

    private fun fieldsAccess(fieldAccess: FieldsAccess) {
        with(binding) {
            fieldsAccess = fieldAccess
            if ((!user.isAdmin || !fieldAccess.isCreator) && !IS_TODAY) {
                fieldsEnable(false)
            }
            if(acceptance.isPrinted){
            etxtCodeClient.isEnabled = false
            }
            etxtCountInPackage.isEnabled = false
            if (fieldsAccess.chBoxEnable) {
                chbArrow.isEnabled = true
                chbBrand.isEnabled = true
                chbCurrency.isEnabled = true
                chbExclamation.isEnabled = true
                chbZ.isEnabled = true
            }
            if (fieldsAccess.properties) {
                etxtPackage.isEnabled = true
                etxtProductType.isEnabled = true
            }
            if (fieldsAccess.zoneEnable) {
                etxtZone.isEnabled = true
            }
            if (fieldsAccess.packageEnable) {
                etxtCountInPackage.isEnabled = true
            }
        }
    }

    private fun enableVisibleList() {
        val enableVisibleMap = mutableMapOf<View, AcceptanceEnableVisible>()
        with(binding) {
            propertyList.forEach { enableVisible ->
                when (enableVisible.field) {
                    AcceptanceRepository.CLIENT_KEY -> enableVisibleMap[etxtCodeClient] =
                        enableVisible
                    AcceptanceRepository.NUMBER_KEY -> {
                        enableVisibleMap[etxtDocumentNumber] = enableVisible
                        enableVisibleMap[etxtDocumentNumberCopy] = enableVisible
                    }
                    AcceptanceRepository.AUTO_NUMBER_KEY -> enableVisibleMap[etxtAutoNumber] =
                        enableVisible
                    AcceptanceRepository.ID_CARD_KEY -> enableVisibleMap[etxtCardNumber] =
                        enableVisible
                    AcceptanceRepository.ZONE_KEY -> enableVisibleMap[elayoutZone] =
                        enableVisible
                    AcceptanceRepository.COUNT_SEAT_KEY -> {
                        enableVisibleMap[etxtSeatsNumber] =
                            enableVisible
                        enableVisibleMap[etxtSeatsNumberCopy] =
                            enableVisible
                    }
                    AcceptanceRepository.COUNT_IN_PACKAGE_KEY -> enableVisibleMap[etxtCountInPackage] =
                        enableVisible
                    AcceptanceRepository.COUNT_PACKAGE_KEY -> enableVisibleMap[etxtPackageCount] =
                        enableVisible
                    AcceptanceRepository.PACKAGE_UID_KEY -> enableVisibleMap[elayoutPackage] =
                        enableVisible
                    AcceptanceRepository.PRODUCT_TYPE_KEY -> enableVisibleMap[elayoutProductType] =
                        enableVisible
                    AcceptanceRepository.STORE_UID_KEY -> enableVisibleMap[elayoutStoreAddress] =
                        enableVisible
                    AcceptanceRepository.PHONE_KEY -> enableVisibleMap[etxtStorePhone] =
                        enableVisible
                    AcceptanceRepository.STORE_NAME_KEY -> enableVisibleMap[etxtStoreNumber] =
                        enableVisible
                    AcceptanceRepository.REPRESENTATIVE_NAME_KEY -> enableVisibleMap[etxtRepresentative] =
                        enableVisible
                    AcceptanceRepository.Z_KEY -> enableVisibleMap[chbZ] =
                        enableVisible
                    AcceptanceRepository.BRAND_KEY -> enableVisibleMap[chbBrand] =
                        enableVisible
                    AcceptanceRepository.GLASS_KEY -> enableVisibleMap[chbExclamation] =
                        enableVisible
                    AcceptanceRepository.EXPENSIVE_KEY -> enableVisibleMap[chbCurrency] =
                        enableVisible
                    AcceptanceRepository.NOT_TURN_OVER_KEY -> enableVisibleMap[chbArrow] =
                        enableVisible
                    else -> {}
                }
            }
        }
        enableVisibleMap.forEach {
            val enableVisible = it.value
            val view = it.key
            view.isVisible = enableVisible.visible
            view.isEnabled = enableVisible.enable
        }
    }

    private fun showAcceptance() {
        with(binding) {
            etxtAutoNumber.setText(acceptance.autoNumber)
            etxtZone.setText(acceptance.zone)
            etxtCardNumber.setText(acceptance.idCard)
            etxtCodeClient.setText(acceptance.client)
            etxtSeatsNumber.setText(acceptance.countSeat.toString())
            etxtDocumentNumber.setText(acceptance.number)
            etxtStoreAddress.setText(acceptance.storeAddressName)
            etxtStoreNumber.setText(acceptance.storeName)
            etxtRepresentative.setText(acceptance.representativeName)
            etxtStorePhone.setText(acceptance.phoneNumber)
            etxtProductType.setText(acceptance.productTypeName)
            etxtPackage.setText(acceptance._package)
            etxtSeatsNumberCopy.setText(acceptance.countSeat.toString())
            etxtPackageCount.setText(acceptance.countPackage.toString())
            etxtCountInPackage.setText(acceptance.countInPackage.toString())
            etxtDocumentNumberCopy.setText(acceptance.number)
            chbExclamation.isChecked = acceptance.glass
            chbCurrency.isChecked = acceptance.expensive
            chbArrow.isChecked = acceptance.notTurnOver
            chbBrand.isChecked = acceptance.brand
            chbZ.isChecked = acceptance.z
        }
    }


    private fun setInitFocuses() {
        if (clientFound) return
        with(binding) {
            with(etxtZone) {
                requestFocus()
                val length = text?.length ?: 0
                if (length > 0) setSelection(length)
            }
        }
    }

    private fun etxtFocusChangeListener(view: View, hasFocus: Boolean) {
        if (hasFocus) {
            view as TextInputEditText
            view.text?.let {
                view.selectAll()
            }
        }
    }

    private fun etxtAutoCompleteFocusChangeListener(view: View, hasFocus: Boolean) {
        if (hasFocus) {
            view as AutoCompleteTextView
            view.text?.let {
                view.selectAll()
            }
        }
    }

    private fun getMacAddressFromCache(context: Context): String? {
        val cacheFile = File(context.cacheDir, "mac_address.txt")
        if (cacheFile.exists()) {
            return cacheFile.readText().trim()
        }
        return null
    }

    private fun checkEditRights() {
        if ((!fieldsAccess.isCreator && !user.isAdmin) || !user.acceptanceCargo) {
            binding.constraintLayout.isEnabled = false
            binding.btnClose.isEnabled = true
            binding.btnCloseCopy.isEnabled = true
        }
    }

    companion object {
        const val KEY_ACCEPTANCE_NUMBER = "acceptance_number"
        var ACCEPTANCE = Acceptance(number = "")
        var NEXT_IS_NEED = false
        var IS_TODAY = false
        var BATCH_GUID = ""
    }
}

