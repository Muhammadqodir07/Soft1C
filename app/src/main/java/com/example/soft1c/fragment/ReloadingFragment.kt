package com.example.soft1c.fragment

import android.R
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soft1c.adapter.BarcodeAdapter
import com.example.soft1c.databinding.FragmentReloadingBinding
import com.example.soft1c.repository.model.ExpandableLoadingList
import com.example.soft1c.repository.model.Loading
import com.example.soft1c.repository.model.LoadingEnableVisible
import com.example.soft1c.repository.model.LoadingModel
import com.example.soft1c.utils.Utils
import com.example.soft1c.utils.Utils.cars
import com.example.soft1c.utils.Utils.container
import com.example.soft1c.utils.Utils.user
import com.example.soft1c.utils.Utils.warehouse
import com.example.soft1c.utils.getDisplayWidth
import com.example.soft1c.viewmodel.ReloadingViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText

class ReloadingFragment :
    BaseFragment<FragmentReloadingBinding>(FragmentReloadingBinding::inflate) {

    private val viewModel: ReloadingViewModel by viewModels()
    private lateinit var reloading: Loading
    private lateinit var loading: Loading
    private var isReloadRoute = true
    private var documentCreate = false

    private var barcodeKeepAdapter: BarcodeAdapter = BarcodeAdapter()
    private var barcodeAddAdapter: BarcodeAdapter = BarcodeAdapter()
    private var barcodeKeepList: ArrayList<ExpandableLoadingList> = arrayListOf()
    private var barcodeAddList: ArrayList<ExpandableLoadingList> = arrayListOf()
    private lateinit var dialog: AlertDialog

    private lateinit var senderWarehouse: AutoCompleteTextView
    private lateinit var getterWarehouse: AutoCompleteTextView
    private lateinit var scannedData: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loadingNumber = arguments?.getString(LoadingFragment.KEY_LOADING_NUMBER, "") ?: ""
        reloading = if (loadingNumber.isNotEmpty()) {
            showDialogLoading()
            viewModel.getReloading(loadingNumber)
            Loading(number = loadingNumber)
        } else {
            Loading(number = "")
        }
        loading = Loading(number = "")
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val filter = IntentFilter()
        filter.addCategory(Intent.CATEGORY_DEFAULT)
        filter.addAction("com.Soft1C.ACTION")
        context?.registerReceiver(broadcastReceiver, filter)

        initUI()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.toastLiveDat.observe(viewLifecycleOwner) {
            documentCreate = false
            toast(it)
        }
        viewModel.toastResIdLiveData.observe(viewLifecycleOwner) {
            documentCreate = false
            errorDialog(requireContext().getString(it), true)
        }
        viewModel.barcodeListLiveData.observe(viewLifecycleOwner, ::fillBarcodeList)
        viewModel.reloadingLiveData.observe(viewLifecycleOwner, ::showDetails)
        viewModel.createUpdateLiveDat.observe(viewLifecycleOwner, ::createUpdateLoading)
    }

    private fun initUI() {
        with(binding) {
            for (i in 0 until linearScrollChild.childCount) {
                val childView = linearScrollChild.getChildAt(i)
                val layoutParams = LinearLayout.LayoutParams(
                    getDisplayWidth(requireContext()), // Width size here
                    LinearLayout.LayoutParams.MATCH_PARENT // Height size here
                )
                childView.layoutParams = layoutParams
            }
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    with(binding.scrollRvLayout) {
                        when (tab.position) {
                            0 -> fullScroll(ScrollView.FOCUS_LEFT)
                            1 -> fullScroll(ScrollView.FOCUS_RIGHT)
                            else -> {}
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

            btnShowHide.setOnClickListener {
                if (linearInformation.visibility == View.VISIBLE) constraintLayout.transitionToEnd()
                else constraintLayout.transitionToStart()
            }

            btnClose.setOnClickListener {
                closeActivity()
            }

            etxtSave.setOnClickListener {
                if (documentCreate) return@setOnClickListener
                fillLoadingBarcodes()
                if (loading.car == LoadingModel.Car()) {
                    binding.tvAutoLoad.error =
                        resources.getString(com.example.soft1c.R.string.text_field_is_empyt)
                } else if (loading.getterWarehouse == LoadingModel.Warehouse()) {
                    binding.etxtRouteLoad.error =
                        resources.getString(com.example.soft1c.R.string.text_field_is_empyt)
                } else if (loading.container == LoadingModel.Container()) {
                    binding.etxtContainerLoad.error =
                        resources.getString(com.example.soft1c.R.string.text_field_is_empyt)
                } else {
                    documentCreate = !documentCreate
                    viewModel.createUpdateLoading(loading)
                }
            }

            rvBarcodeKeep.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = barcodeKeepAdapter
            }
            rvBarcodeAdd.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = barcodeAddAdapter
            }


            initLoad()
            initReload()
        }
    }


    private fun initLoad() {
        with(binding) {
            tvAutoLoad.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.simple_list_item_1,
                    cars.map { (it as LoadingModel.Car).number })
            )
            //Слушатель ПриВыбореЭлементаСписка
            tvAutoLoad.setOnItemClickListener { parent, view, position, _ ->
                val selectedModel = parent.getItemAtPosition(position) as String
                //Выполняем функцию НайтиИЗаполнитьВыбранныйЭлемент куда передаем позицию выбранного элемента
                findAndFillAnySelectedModel(
                    cars,
                    Utils.ObjectModelType.CAR,
                    selectedModel
                )
                view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                //Если кнопка сохранения видна и доступна то перевести фокус на него
                if (linearInformation.isEnabled && linearInformation.isVisible) {
                    view.clearFocus()
                    linearInformation.requestFocus()
                }
            }
            //Обработчик нажатия клавиш
            tvAutoLoad.setOnKeyListener(::autoCompleteOnKeyListener)
            tvAutoLoad.setOnFocusChangeListener { v, hasFocus ->
                if (v is MaterialAutoCompleteTextView && hasFocus && v.error != null) {
                    v.error = null
                }
            }
            etxtContainerLoad.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.simple_list_item_1,
                    container.map { (it as LoadingModel.Container).name }
                )
            )
            etxtContainerLoad.setOnItemClickListener { parent, view, position, _ ->
                val selectedModel = parent.getItemAtPosition(position) as String
                findAndFillAnySelectedModel(
                    container,
                    Utils.ObjectModelType.CONTAINER,
                    selectedModel
                )
                view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                if (tvAutoLoad.isEnabled && tvAutoLoad.isVisible) {
                    view.clearFocus()
                    tvAutoLoad.requestFocus()
                }
            }
            etxtContainerLoad.setOnKeyListener(::autoCompleteOnKeyListener)
            etxtContainerLoad.setOnFocusChangeListener { v, hasFocus ->
                if (v is MaterialAutoCompleteTextView && hasFocus && v.error != null) {
                    v.error = null
                }
            }
            etxtRouteLoad.setOnFocusChangeListener(::etxtFocusChangeListener)
            etxtRouteLoad.setOnClickListener {
                etxtRouteLoad.error = null
                isReloadRoute = false
                routeDialog()
            }
        }
    }

    private fun initReload() {
        with(binding) {
            tvAutoReload.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.simple_list_item_1,
                    cars.map { (it as LoadingModel.Car).number })
            )
            //Слушатель ПриВыбореЭлементаСписка
            tvAutoReload.setOnItemClickListener { parent, view, position, _ ->
                val selectedModel = parent.getItemAtPosition(position) as String
                //Выполняем функцию НайтиИЗаполнитьВыбранныйЭлемент куда передаем позицию выбранного элемента
                findAndFillAnySelectedModel(
                    cars,
                    Utils.ObjectModelType.CAR,
                    selectedModel
                )
                view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                //Если кнопка сохранения видна и доступна то перевести фокус на него
                if (linearInformation.isEnabled && linearInformation.isVisible) {
                    view.clearFocus()
                    linearInformation.requestFocus()
                }
            }
            //Обработчик нажатия клавиш
            tvAutoReload.setOnKeyListener(::autoCompleteOnKeyListener)
            tvAutoReload.setOnFocusChangeListener { v, hasFocus ->
                if (v is MaterialAutoCompleteTextView && hasFocus && v.error != null) {
                    v.error = null
                }
            }
            etxtContainerReload.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.simple_list_item_1,
                    container.map { (it as LoadingModel.Container).name }
                )
            )
            etxtContainerReload.setOnItemClickListener { parent, view, position, _ ->
                val selectedModel = parent.getItemAtPosition(position) as String
                findAndFillAnySelectedModel(
                    container,
                    Utils.ObjectModelType.CONTAINER,
                    selectedModel
                )
                view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                if (tvAutoReload.isEnabled && tvAutoReload.isVisible) {
                    view.clearFocus()
                    tvAutoReload.requestFocus()
                }
            }
            etxtContainerReload.setOnKeyListener(::autoCompleteOnKeyListener)
            etxtContainerReload.setOnFocusChangeListener { v, hasFocus ->
                if (v is MaterialAutoCompleteTextView && hasFocus && v.error != null) {
                    v.error = null
                }
            }
            etxtRouteReload.setOnFocusChangeListener(::etxtFocusChangeListener)
            etxtRouteReload.setOnClickListener {
                etxtRouteReload.error = null
                isReloadRoute = true
                routeDialog()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initRouteDialog(dialogView: View) {
        senderWarehouse = dialogView.findViewById(com.example.soft1c.R.id.etxt_wareH_sender)
        getterWarehouse = dialogView.findViewById(com.example.soft1c.R.id.etxt_wareH_getter)
        val btnOk = dialogView.findViewById<Button>(com.example.soft1c.R.id.btn_ok)
        val btnClose = dialogView.findViewById<Button>(com.example.soft1c.R.id.btn_close)
        val doc = if (isReloadRoute) reloading else loading

        btnOk.setOnClickListener {
            if (isReloadRoute) {
                binding.etxtRouteReload.setText("${doc.senderWarehouse.prefix} — ${doc.getterWarehouse.prefix}")
                binding.etxtContainerReload.requestFocus()
            } else
                binding.etxtRouteLoad.setText("${doc.senderWarehouse.prefix} — ${doc.getterWarehouse.prefix}")
            dialog.dismiss()
        }
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        if (doc.senderWarehouse != LoadingModel.Warehouse()) {
            senderWarehouse.setText(doc.senderWarehouse.name)
        } else {
            val warehouse = Utils.warehouse.find {
                it is LoadingModel.Warehouse && it.ref == user.warehouse
            } as? LoadingModel.Warehouse
            warehouse?.let {
                findAndFillAnySelectedModel(
                    Utils.warehouse,
                    Utils.ObjectModelType.WAREHOUSE,
                    it.name
                )
            }
        }
        if (doc.getterWarehouse != LoadingModel.Warehouse()) {
            getterWarehouse.setText(doc.getterWarehouse.name)
        }

        senderWarehouse.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.simple_list_item_1,
                Utils.warehouse.map { (it as LoadingModel.Warehouse).name })
        )
        senderWarehouse.setOnItemClickListener { parent, view, position, _ ->
            val selectedModel = parent.getItemAtPosition(position) as String
            val model =
                findAndFillAnySelectedModel(
                    warehouse,
                    Utils.ObjectModelType.WAREHOUSE,
                    selectedModel
                ) as? LoadingModel.Warehouse ?: LoadingModel.Warehouse()
            if (model != LoadingModel.Warehouse()) {
                doc.senderWarehouse = model
                senderWarehouse.setText(model.name)
                if (getterWarehouse.isEnabled && getterWarehouse.isVisible) {
                    getterWarehouse.requestFocus()
                }
            } else {
                doc.senderWarehouse = LoadingModel.Warehouse()
                senderWarehouse.text.clear()
            }

            view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))

            if (isReloadRoute) reloading = doc else loading = doc
        }
        senderWarehouse.setOnKeyListener(::autoCompleteOnKeyListener)

        getterWarehouse.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.simple_list_item_1,
                warehouse.map { (it as LoadingModel.Warehouse).name })
        )
        getterWarehouse.setOnItemClickListener { parent, view, position, _ ->
            val selectedModel = parent.getItemAtPosition(position) as String
            //Выполняем функцию НайтиИЗаполнитьВыбранныйЭлемент куда передаем позицию выбранного элемента
            val model = findAndFillAnySelectedModel(
                warehouse,
                Utils.ObjectModelType.WAREHOUSE,
                selectedModel
            ) as? LoadingModel.Warehouse ?: LoadingModel.Warehouse()
            if (model != LoadingModel.Warehouse()) {
                doc.getterWarehouse = model
                getterWarehouse.setText(model.name)
            } else {
                doc.getterWarehouse = LoadingModel.Warehouse()
                getterWarehouse.text.clear()
            }
            view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))

            if (isReloadRoute) reloading = doc else loading = doc
        }
        getterWarehouse.setOnKeyListener(::autoCompleteOnKeyListener)
    }

    //Диалог маршрута
    private fun routeDialog() {
        val dialogView =
            LayoutInflater.from(requireContext())
                .inflate(com.example.soft1c.R.layout.dialog_loading_warehouse, null)
        initRouteDialog(dialogView)

        dialog = AlertDialog.Builder(requireContext(), com.example.soft1c.R.style.CustomDialogTheme)
            .setView(dialogView)
            .create()

        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.TOP)
        dialog.show()
    }

    private fun fillBarcodeList(barcodes: List<ExpandableLoadingList>?) {
        closeDialogLoading()
        with(binding) {
            if (barcodes != null) {
                val targetList = if (radioAcceptance.isChecked) {
                    if (tabLayout.selectedTabPosition == 0) barcodeKeepList else barcodeAddList
                } else null

                barcodes.forEach { barcode ->
                    if (targetList != null && !targetList.contains(barcode)) {
                        targetList.add(barcode)
                        if (targetList == barcodeKeepList) {
                            if ((reloading.barcodesFront+reloading.barcodesBack).contains(barcode))
                                barcodeKeepAdapter.addBarcodeData(barcode)
                        } else {
                            barcodeAddAdapter.addBarcodeData(barcode)
                        }
                    } else if (!binding.radioAcceptance.isChecked && barcode.loadingChild.barcode == scannedData) {
                        if (targetList != null && !targetList.contains(barcode)) {
                            targetList.add(barcode)
                            if (targetList == barcodeKeepList) {
                                barcodeKeepAdapter.addBarcodeData(barcode)
                            } else {
                                barcodeKeepAdapter.addBarcodeData(barcode)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun fillLoadingBarcodes() {
        val frontList =
            if (binding.linearFront.isVisible)
                if (binding.spinnerFront.selectedItemPosition == 0)
                    reloading.barcodesFront.toMutableList()
                else reloading.barcodesBack.toMutableList()
            else mutableListOf()

        val backList = if (binding.linearBack.isVisible)
            if (binding.spinnerBack.selectedItemPosition == 0)
                reloading.barcodesFront.toMutableList()
            else reloading.barcodesBack.toMutableList()
        else mutableListOf()

        frontList.addAll(barcodeAddList)
        frontList.removeAll(barcodeKeepList)
        backList.removeAll(barcodeKeepList)
        loading.barcodesFront = frontList
        loading.barcodesBack = backList
    }

    private fun createUpdateLoading(pair: Pair<Loading, String>) {
        if (pair.second.isNotEmpty()) {
            toast(pair.second)
            return
        }
        Utils.refreshList = true
        closeActivity()
    }

    private fun showDetails(pair: Pair<Loading, List<LoadingEnableVisible>>) {
        reloading = pair.first
        closeDialogLoading()
        if (this.reloading.ref.isEmpty()) {
            binding.pbLoading.isVisible = false
            return
        }
        showReloading()
    }

    @SuppressLint("SetTextI18n")
    private fun showReloading() {
        with(binding) {
            txtNumberReload.setText(reloading.number)
            etxtContainerReload.setText(reloading.container.name)
            etxtRouteReload.setText("${reloading.senderWarehouse.prefix} — ${reloading.getterWarehouse.prefix}")
            tvAutoReload.setText(reloading.car.number)

            if (reloading.barcodesFront.isEmpty()) {
                linearFront.visibility = View.GONE
            }
            if (reloading.barcodesBack.isEmpty()) {
                linearBack.visibility = View.GONE
            }
        }
    }

    private fun autoCompleteOnKeyListener(view: View, key: Int, keyEvent: KeyEvent): Boolean {
        if (key == 66 && keyEvent.action == KeyEvent.ACTION_UP) {
            requireContext().hideKeyboard(view)
            view as AutoCompleteTextView
            view.error = null
            with(binding) {
                when (view) {
                    tvAutoReload -> {
                        val model = findAndFillAnyModel(
                            cars,
                            Utils.ObjectModelType.CAR,
                            view
                        ) as? LoadingModel.Car ?: LoadingModel.Car()

                        if (model != LoadingModel.Car()) {
                            view.setText(model.number)
                            if (etxtContainerLoad.isEnabled && etxtContainerLoad.isVisible) {
                                etxtContainerLoad.requestFocus()
                                return true
                            }
                        } else {
                            view.text.clear()
                        }
                        return false
                    }

                    tvAutoLoad -> {
                        val model = findAndFillAnyModel(
                            cars,
                            Utils.ObjectModelType.CAR,
                            view
                        ) as? LoadingModel.Car ?: LoadingModel.Car()

                        if (model != LoadingModel.Car()) {

                            linearInformation.requestFocus()
                            view.setText(model.number)
                            loading.car = model

                            return true
                        } else {
                            view.text.clear()
                            loading.car = LoadingModel.Car()
                        }
                        return false
                    }

                    etxtContainerLoad -> {
                        val model =
                            findAndFillAnyModel(
                                container,
                                Utils.ObjectModelType.CONTAINER,
                                view
                            ) as? LoadingModel.Container ?: LoadingModel.Container()
                        if (model != LoadingModel.Container()) {
                            view.setText(model.name)
                            loading.container = model
                            if (tvAutoLoad.isEnabled && tvAutoLoad.isVisible) {
                                tvAutoLoad.requestFocus()
                                return true
                            }
                        } else {
                            loading.container = LoadingModel.Container()
                            view.text.clear()
                        }
                        return false
                    }

                    etxtContainerReload -> {
                        val model =
                            findAndFillAnyModel(
                                container,
                                Utils.ObjectModelType.CONTAINER,
                                view
                            ) as? LoadingModel.Container ?: LoadingModel.Container()
                        if (model != LoadingModel.Container()) {
                            view.setText(model.name)
                            reloading.container = model
                            if (tvAutoReload.isEnabled && tvAutoReload.isVisible) {
                                tvAutoReload.requestFocus()
                                return true
                            }
                        } else {
                            reloading.container = LoadingModel.Container()
                            view.text.clear()
                        }
                        return false
                    }

                    senderWarehouse -> {
                        val model =
                            findAndFillAnyModel(
                                warehouse,
                                Utils.ObjectModelType.WAREHOUSE,
                                view
                            ) as? LoadingModel.Warehouse ?: LoadingModel.Warehouse()
                        if (isReloadRoute) reloading.senderWarehouse =
                            model else loading.senderWarehouse = model
                        if (model != LoadingModel.Warehouse()) {
                            view.setText(model.name)
                            if (getterWarehouse.isEnabled && getterWarehouse.isVisible) {
                                getterWarehouse.requestFocus()
                                return true
                            }
                        } else {
                            view.text.clear()
                        }
                        return false
                    }

                    getterWarehouse -> {
                        val model =
                            findAndFillAnyModel(
                                warehouse,
                                Utils.ObjectModelType.WAREHOUSE,
                                view
                            ) as? LoadingModel.Warehouse ?: LoadingModel.Warehouse()
                        if (isReloadRoute) reloading.getterWarehouse =
                            model else loading.getterWarehouse = model
                        if (model != LoadingModel.Warehouse()) {
                            getterWarehouse.clearFocus()
                            view.setText(model.name)
                        } else {
                            view.text.clear()
                        }
                        return false
                    }

                    else -> return false
                }
            }
        } else if (key == 10036 && keyEvent.action == KeyEvent.ACTION_DOWN) {
            //TODO clearAllFocus()
            return true
        }
        return false
    }

    //Функция добавления/удаления сканированного штрих-кода в объект BarcodeAdapter
    private fun displayScanResult(initiatingIntent: Intent) {
        with(binding) {
            scannedData =
                initiatingIntent.getStringExtra("com.symbol.datawedge.data_string").toString()

            if (Utils.debugMode)
                scannedData = "90000153429210421"

            if (::scannedData.isInitialized && scannedData.length >= 17) {
                val last17Chars = scannedData.takeLast(17)

                if (chbDelete.isChecked) {
                    if (radioAcceptance.isChecked) {
                        val adapter =
                            if (tabLayout.selectedTabPosition == 0) barcodeKeepAdapter else barcodeAddAdapter
                        val documentUid =
                            adapter.find(scannedData)?.loadingChild?.parentUid
                        documentUid?.let { adapter.removeBarcodeByUid(it) }
                    } else {
                        val adapter =
                            if (tabLayout.selectedTabPosition == 0) barcodeKeepAdapter else barcodeAddAdapter
                        adapter.removeBarcodeByBarcode(scannedData)
                    }
                } else {
                    if (tabLayout.selectedTabPosition == 0) {
                        if (radioAcceptance.isChecked) {
                            viewModel.getBarcodeList(last17Chars, user.warehouse)
                            showDialogLoading()
                        } else {
                            (reloading.barcodesFront + reloading.barcodesBack)
                                .find { it.loadingChild.barcode == scannedData && it.type == ExpandableLoadingList.CHILD }
                                ?.let {
                                    barcodeKeepAdapter.addBarcodeData(it)
                                    barcodeKeepList.add(it)
                                }
                        }
                    } else {
                        if (barcodeAddAdapter.find(scannedData) == null) {
                            viewModel.getBarcodeList(last17Chars, user.warehouse)
                            showDialogLoading()
                        } else {
                            toast(requireContext().getString(com.example.soft1c.R.string.err_dublicate_barcode))
                        }
                    }
                }
            }
        }
    }

    private fun closeActivity() {
        activity?.onBackPressed()
    }

    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == "com.Soft1C.ACTION") {
                try {
                    displayScanResult(intent)
                } catch (e: Exception) {
                    // Catch if the UI does not exist when we receive the broadcast
                }
            }
        }
    }

    fun findAndFillAnySelectedModel(
        anyList: List<LoadingModel>,
        model: Int,
        selectedModel: String
    ): LoadingModel? {
        //Переменная текстЭлемента. Если текст выбранного элемента не пусто то передать его, если пусто то вернуться
        val textElement = when {
            selectedModel.isNotEmpty() -> selectedModel
            selectedModel.isEmpty() -> return null
            else -> ""
        }
        //Найти номер из списка номеров с помощью текстЭлемента
        return textElementFound(anyList, model, textElement)
    }

    fun findAndFillAnyModel(
        anyList: List<LoadingModel>,
        model: Int,
        view: AutoCompleteTextView,
    ): LoadingModel? {
        val textElement = when {
            view.text.isNotEmpty() -> {
                if (!view.adapter.isEmpty) {
                    view.adapter.getItem(0).toString()
                } else {
                    view.text.clear()
                    return null
                }
            }

            view.text.isEmpty() -> return null
            else -> ""
        }
        //Найти номер из списка номеров с помощью текстЭлемента
        return textElementFound(anyList, model, textElement)
    }

    //Находим номер из списка номеров с помощью текстЭлемента
    private fun textElementFound(
        anyList: List<LoadingModel>,
        model: Int,
        textElement: String,
    ): LoadingModel? {
        //Переменная элемент. Найти из списка машин номер по условию СписокМашин.Номер = текстЭлемента с помощью встроенной функции find
        val element = anyList.find {
            when (it) {
                is LoadingModel.Car -> it.number == textElement
                is LoadingModel.Warehouse -> it.name == textElement
                is LoadingModel.Container -> it.name == textElement
            }
        }

        return when (element) {
            is LoadingModel.Car -> element
            is LoadingModel.Container -> element
            is LoadingModel.Warehouse -> element
            else -> when (model) {
                Utils.ObjectModelType.CAR -> LoadingModel.Car()
                Utils.ObjectModelType.CONTAINER -> LoadingModel.Container()
                Utils.ObjectModelType.WAREHOUSE -> LoadingModel.Warehouse()
                else -> null
            }
        }
    }

    fun etxtFocusChangeListener(view: View, hasFocus: Boolean) {
        if (hasFocus) {
            view as TextInputEditText
            view.text?.let {
                view.selectAll()
            }
        }
    }

    //При выходе из формы отсоединяем приёмник broadcastReceiver и очищаем список сканированных штрих-кодов
    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(broadcastReceiver)
        barcodeKeepAdapter.clearBarcodeData()
        barcodeAddAdapter.clearBarcodeData()
    }

    companion object {
        fun newInstance() = ReloadingFragment()
    }
}