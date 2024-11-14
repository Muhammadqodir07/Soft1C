package com.example.soft1c.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
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
import com.example.soft1c.R
import com.example.soft1c.adapter.BarcodeAdapter
import com.example.soft1c.databinding.FragmentLoadingBinding
import com.example.soft1c.repository.model.Loading
import com.example.soft1c.repository.model.LoadingBarcode
import com.example.soft1c.repository.model.LoadingEnableVisible
import com.example.soft1c.repository.model.LoadingModel
import com.example.soft1c.utils.Utils
import com.example.soft1c.utils.Utils.cars
import com.example.soft1c.utils.Utils.user
import com.example.soft1c.utils.Utils.warehouse
import com.example.soft1c.utils.getDisplayWidth
import com.example.soft1c.viewmodel.LoadingViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import java.text.SimpleDateFormat
import java.util.Locale


class LoadingFragment : BaseFragment<FragmentLoadingBinding>(FragmentLoadingBinding::inflate) {

    private val ACTION_CHAINWAY = "com.example.soft1c"
    private val ACTION_ZEBRA = "com.Soft1C.ACTION"
    private val CATEGORY_DEFAULT = Intent.CATEGORY_DEFAULT

    // Объект класса позволяющего связывать штрих-коды с элементами интерфейса
    private var barcodeFrontAdapter: BarcodeAdapter = BarcodeAdapter { barcode ->
        onRemoveBarcodeClick(barcode)
    }
    private var barcodeBackAdapter: BarcodeAdapter = BarcodeAdapter { barcode ->
        onRemoveBarcodeClick(barcode)
    }

    private var barcodeFrontList: ArrayList<LoadingBarcode> = arrayListOf()
    private var barcodeBackList: ArrayList<LoadingBarcode> = arrayListOf()
    private lateinit var scannedData: String
    private var frontWeight = 0.0
    private var frontVolume = 0.0
    private var backWeight = 0.0
    private var backVolume = 0.0

    private val viewModel: LoadingViewModel by viewModels()
    private lateinit var loading: Loading

    private lateinit var senderWarehouse: AutoCompleteTextView
    private lateinit var getterWarehouse: AutoCompleteTextView
    private lateinit var dialog: AlertDialog

    private var documentCreate = false
    private val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val outputDateFormat = SimpleDateFormat("dd/MM/yy || HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loadingNumber = arguments?.getString(KEY_LOADING_NUMBER, "") ?: ""
        loading = if (loadingNumber.isNotEmpty()) {
            showDialogLoading()
            viewModel.getLoading(loadingNumber)
            Loading(number = loadingNumber)
        } else {
            Loading(number = "")
        }
    }

    //Насследуемая, главная, функция выполняющаяся когда форма создана
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //Регистрация приёмника broadcastReceiver с использованием фильтра filter
        registerBroadcastReceiver()
        //Устанавливание слушателей для поля НомераМашин
        initUI()
        observeViewModel()
    }

    private fun registerBroadcastReceiver() {
        val filter = IntentFilter().apply {
            addCategory(CATEGORY_DEFAULT)
        }

        val (action, broadcastReceiver) = if (Build.BRAND.uppercase() == "CHAINWAY") {
            ACTION_CHAINWAY to broadcastReceiver
        } else {
            ACTION_ZEBRA to broadcastReceiver
        }

        filter.addAction(action)
        context?.registerReceiver(broadcastReceiver, filter)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.action?.let { action ->
                when (action) {
                    ACTION_ZEBRA -> {
                        try {
                            intent.getStringExtra("com.symbol.datawedge.data_string")?.let { data ->
                                displayScanResult(data)
                            }
                        } catch (e: Exception) {
                            // Catch if the UI does not exist when we receive the broadcast
                        }
                    }

                    ACTION_CHAINWAY -> {
                        try {
                            intent.getStringExtra("data")?.let { data ->
                                if (data.isNotEmpty()) {
                                    displayScanResult(data)
                                }
                            }
                        } catch (e: Exception) {
                            // Catch if the UI does not exist when we receive the broadcast
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    //Функция устанавливания слушателей для поля НомераМашин
    private fun initUI() {
        showLoading()
        with(binding) {
            if (loading.date.isNotEmpty()) {
                txtDate.setText(inputDateFormat.parse(loading.date)
                    ?.let { outputDateFormat.format(it) })
            } else {
                elayoutTxtNumber.visibility = View.GONE
                elayoutTxtDate.visibility = View.GONE
            }
            tvAuto.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    cars.map { (it as LoadingModel.Car).number })
            )
            elayoutBarcode.setEndIconOnClickListener {
                etxtBarcode.clearFocus()
                requireContext().hideKeyboard(etxtBarcode)
                displayScanResult(etxtBarcode.text.toString())
            }
            etxtBarcode.setOnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    requireContext().hideKeyboard(etxtBarcode)
                    return@setOnKeyListener true
                } else {
                    false
                }
            }
            //Слушатель ПриВыбореЭлементаСписка
            tvAuto.setOnItemClickListener { parent, view, position, _ ->
                val selectedModel = parent.getItemAtPosition(position) as String
                //Выполняем функцию НайтиИЗаполнитьВыбранныйЭлемент куда передаем позицию выбранного элемента
                findAndFillAnySelectedModel(
                    cars,
                    Utils.ObjectModelType.CAR,
                    tvAuto,
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
            tvAuto.setOnKeyListener(::autoCompleteOnKeyListener)
            tvAuto.setOnFocusChangeListener { v, hasFocus ->
                if (v is MaterialAutoCompleteTextView && hasFocus && v.error != null) {
                    v.error = null
                }
            }
            btnShowHide.setOnClickListener {
                if (linearInformation.visibility == View.VISIBLE) {
                    constraintLayout.transitionToEnd()
                } else {
                    constraintLayout.transitionToStart()
                }
            }
            //Устанавливаем связь между только что созданным объектом и элементом интерфейса
            binding.rvBarcodeFront.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = barcodeFrontAdapter
            }

            binding.rvBarcodeBack.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = barcodeBackAdapter
            }

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
                            0 -> {
                                fullScroll(ScrollView.FOCUS_LEFT)
                            }

                            1 -> {
                                fullScroll(ScrollView.FOCUS_RIGHT)
                            }

                            else -> {}
                        }
                    }
                    updateWeightAndVolumeUI()
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

            etxtRoute.setOnClickListener {
                etxtRoute.error = null
                loadingRouteDialog()
            }
            etxtSave.setOnClickListener {
                createUpdateLoading()
            }
            btnClose.setOnClickListener { closeActivity() }
        }
    }

    private fun initRouteDialog(dialogView: View) {
        senderWarehouse = dialogView.findViewById(R.id.etxt_wareH_sender)
        getterWarehouse = dialogView.findViewById(R.id.etxt_wareH_getter)
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok)
        val btnClose = dialogView.findViewById<Button>(R.id.btn_close)



        btnOk.setOnClickListener {
            binding.etxtRoute.setText("${loading.senderWarehouse.prefix} — ${loading.getterWarehouse.prefix}")
            binding.tvRecipient.text = getterWarehouse.text
            dialog.dismiss()
        }
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        if (loading.senderWarehouse != LoadingModel.Warehouse()) {
            senderWarehouse.setText(loading.senderWarehouse.name)
        } else {
            val warehouse = Utils.warehouse.find {
                it is LoadingModel.Warehouse && it.ref == user.warehouse
            } as? LoadingModel.Warehouse
            warehouse?.let {
                findAndFillAnySelectedModel(
                    Utils.warehouse,
                    Utils.ObjectModelType.WAREHOUSE,
                    senderWarehouse,
                    it.name
                )
            }
        }
        if (loading.getterWarehouse != LoadingModel.Warehouse()) {
            getterWarehouse.setText(loading.getterWarehouse.name)
        }

        senderWarehouse.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                warehouse.map { (it as LoadingModel.Warehouse).name })
        )
        senderWarehouse.setOnItemClickListener { parent, view, position, _ ->
            val selectedModel = parent.getItemAtPosition(position) as String
            //Выполняем функцию НайтиИЗаполнитьВыбранныйЭлемент куда передаем позицию выбранного элемента
            findAndFillAnySelectedModel(
                warehouse,
                Utils.ObjectModelType.WAREHOUSE,
                senderWarehouse,
                selectedModel
            )
            view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
        senderWarehouse.setOnKeyListener(::autoCompleteOnKeyListener)

        getterWarehouse.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                warehouse.map { (it as LoadingModel.Warehouse).name })
        )
        getterWarehouse.setOnItemClickListener { parent, view, position, _ ->
            val selectedModel = parent.getItemAtPosition(position) as String
            //Выполняем функцию НайтиИЗаполнитьВыбранныйЭлемент куда передаем позицию выбранного элемента
            findAndFillAnySelectedModel(
                warehouse,
                Utils.ObjectModelType.WAREHOUSE,
                getterWarehouse,
                selectedModel
            )
            view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
        getterWarehouse.setOnKeyListener(::autoCompleteOnKeyListener)
    }

    private fun createUpdateLoading() {
        if (documentCreate) return
        loading.barcodesFront = barcodeFrontList
        loading.barcodesBack = barcodeBackList
        if (loading.car == LoadingModel.Car()) {
            binding.tvAuto.error = resources.getString(R.string.text_field_is_empyt)
        } else if (loading.getterWarehouse == LoadingModel.Warehouse()) {
            binding.etxtRoute.error = resources.getString(R.string.text_field_is_empyt)
        } else {
            documentCreate = !documentCreate
            showDialogLoading()
            binding.etxtSave.isEnabled = false
            viewModel.createUpdateLoading(loading)
        }
    }

    private fun getCreateUpdateLoadingInfo(pair: Pair<Loading, String>) {
        closeDialogLoading()
        binding.etxtSave.isEnabled = true
        if (pair.second.isNotEmpty()) {
            toast(pair.second)
            return
        }
        Utils.refreshList = true
        closeActivity()
    }


    private fun observeViewModel() {
        viewModel.loadingLiveData.observe(viewLifecycleOwner, ::showDetails)
        viewModel.toastLiveDat.observe(viewLifecycleOwner) {
            documentCreate = false
            errorDialog(it, true)
        }
        viewModel.toastResIdLiveData.observe(viewLifecycleOwner) {
            documentCreate = false
            errorDialog(requireContext().getString(it), false)
        }
        viewModel.createUpdateLiveDat.observe(viewLifecycleOwner, ::getCreateUpdateLoadingInfo)
        viewModel.barcodeListLiveData.observe(viewLifecycleOwner, ::fillBarcodeList)
    }

    private fun fillBarcodeList(barcodes: List<LoadingBarcode>?) {
        closeDialogLoading()
        barcodes?.forEach { barcode ->
            if (binding.radioAcceptance.isChecked && !(barcodeFrontList + barcodeBackList).contains(
                    barcode
                )
            ) {
                if (binding.tabLayout.selectedTabPosition == 0) {
                    barcodeFrontAdapter.addBarcodeData(barcode)
                    barcodeFrontList.add(barcode)
                } else {
                    barcodeBackAdapter.addBarcodeData(barcode)
                    barcodeBackList.add(barcode)
                }
            } else if (!binding.radioAcceptance.isChecked && barcode.barcode == scannedData) {
                if (!(barcodeFrontList + barcodeBackList).contains(barcode)) {
                    if (binding.tabLayout.selectedTabPosition == 0) {
                        barcodeFrontAdapter.addBarcodeData(barcode)
                        barcodeFrontList.add(barcode)
                    } else {
                        barcodeBackAdapter.addBarcodeData(barcode)
                        barcodeBackList.add(barcode)
                    }
                }
            }
        }

        updateWeightAndVolume()
    }

    private fun updateWeightAndVolume() {
        if (binding.tabLayout.selectedTabPosition == 0) {
            frontWeight = barcodeFrontList.sumOf { it.weight }
            frontVolume = barcodeFrontList.sumOf { it.volume }

        } else {
            backWeight = barcodeBackList.sumOf { it.weight }
            backVolume = barcodeBackList.sumOf { it.volume }
        }
        updateWeightAndVolumeUI()
    }

    private fun updateWeightAndVolumeUI() {
        if (binding.tabLayout.selectedTabPosition == 0) {
            binding.txtWeightValue.text = String.format("%.2f", frontWeight)
            binding.txtVolumeValue.text = String.format("%.6f", frontVolume)
        } else {
            binding.txtWeightValue.text = String.format("%.2f", backWeight)
            binding.txtVolumeValue.text = String.format("%.6f", backVolume)
        }
    }

    //Функция обработчика клавиш
    private fun autoCompleteOnKeyListener(view: View, key: Int, keyEvent: KeyEvent): Boolean {
        binding.tvAuto.error = null

        //Если нажата клавиша Enter
        if (key == 66 && keyEvent.action == KeyEvent.ACTION_UP) {
            requireContext().hideKeyboard(view)
            view as AutoCompleteTextView
            with(binding) {
                when (view) {
                    //Если это поле НомераМашин то
                    tvAuto -> {
                        //Выолнить Функцию НайтиИЗаполнить
                        findAndFillAnyModel(
                            cars,
                            Utils.ObjectModelType.CAR,
                            view
                        )
                        linearInformation.requestFocus()
                        return true
                    }

                    senderWarehouse -> {
                        findAndFillAnyModel(
                            warehouse,
                            Utils.ObjectModelType.WAREHOUSE,
                            view
                        )
                        if (getterWarehouse.isEnabled && getterWarehouse.isVisible) {
                            getterWarehouse.requestFocus()
                            return true
                        }
                        return false
                    }

                    getterWarehouse -> {
                        findAndFillAnyModel(
                            warehouse,
                            Utils.ObjectModelType.WAREHOUSE,
                            view
                        )
                        getterWarehouse.clearFocus()
                        return false
                    }

                    else -> return false
                }
            }
        } else if (key == 10036 && keyEvent.action == KeyEvent.ACTION_DOWN) {
            clearAllFocus()
            return true
        }
        return false
    }

    private fun findAndFillAnyModel(
        anyList: List<LoadingModel>,
        model: Int,
        view: AutoCompleteTextView,
    ) {
        val textElement = when {
            view.text.isNotEmpty() -> {
                if (!view.adapter.isEmpty) {
                    view.adapter.getItem(0).toString()
                } else {
                    view.text.clear()
                    return
                }
            }

            view.text.isEmpty() -> return
            else -> ""
        }
        //Найти номер из списка номеров с помощью текстЭлемента
        textElementFound(anyList, model, view, textElement)
    }


    //Функция НайтиИЗаполнитьВыбранныйЭлемент получает данные списка Машин, поле НомераМашин и текст выбранного элемента в списке
    private fun findAndFillAnySelectedModel(
        anyList: List<LoadingModel>,
        model: Int,
        view: AutoCompleteTextView,
        selectedModel: String
    ) {
        //Переменная текстЭлемента. Если текст выбранного элемента не пусто то передать его, если пусто то вернуться
        val textElement = when {
            selectedModel.isNotEmpty() -> selectedModel
            selectedModel.isEmpty() -> return
            else -> ""
        }
        //Найти номер из списка номеров с помощью текстЭлемента
        textElementFound(anyList, model, view, textElement)
    }

    //Находим номер из списка номеров с помощью текстЭлемента
    private fun textElementFound(
        anyList: List<LoadingModel>,
        model: Int,
        view: AutoCompleteTextView,
        textElement: String,
    ) {
        //Переменная элемент. Найти из списка машин номер по условию СписокМашин.Номер = текстЭлемента с помощью встроенной функции find
        val element = anyList.find {
            when (it) {
                is LoadingModel.Car -> it.number == textElement
                is LoadingModel.Warehouse -> it.name == textElement
                else -> false
            }
        }

        //Если выполняется условие то передать номер элемента в поле НомераМашин. Иначе очистить поле НомераМашин
        if (element != null) {
            when (model) {
                Utils.ObjectModelType.CAR -> {
                    element as LoadingModel.Car
                    loading.car = element
                    view.setText(element.number)
                }

                Utils.ObjectModelType.WAREHOUSE -> {
                    element as LoadingModel.Warehouse
                    when (view.id) {
                        R.id.etxt_wareH_sender -> {
                            loading.senderWarehouse = element
                        }

                        R.id.etxt_wareH_getter -> {
                            loading.getterWarehouse = element
                        }
                    }
                    view.setText(element.name)
                }
            }
        } else {
            when (model) {
                Utils.ObjectModelType.CAR -> {
                    view.text.clear()
                    loading.car = LoadingModel.Car()
                }

                Utils.ObjectModelType.WAREHOUSE -> {
                    view.text.clear()
                    when (view.id) {
                        R.id.etxt_wareH_sender -> {
                            loading.senderWarehouse = LoadingModel.Warehouse()
                        }

                        R.id.etxt_wareH_getter -> {
                            loading.getterWarehouse = LoadingModel.Warehouse()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showLoading() {
        with(binding) {
            if (loading.number.isNotEmpty() && loading.date.isNotEmpty()) {
                elayoutTxtNumber.visibility = View.VISIBLE
                elayoutTxtDate.visibility = View.VISIBLE
            }
            txtNumber.setText(loading.number)
            txtDate.setText(loading.date.replace("T", " — "))
            tvAuto.setText(loading.car.number)
            if (loading.getterWarehouse != LoadingModel.Warehouse())
                etxtRoute.setText("${loading.senderWarehouse.prefix} — ${loading.getterWarehouse.prefix}")
            tvRecipient.setText(loading.getterWarehouse.name)
            loading.barcodesFront.forEach {
                barcodeFrontList.add(it)
                barcodeFrontAdapter.addBarcodeData(it)

            }
            loading.barcodesBack.forEach {
                barcodeBackList.add(it)
                barcodeBackAdapter.addBarcodeData(it)
            }
            updateWeightAndVolume()
        }
    }

    private fun showDetails(pair: Pair<Loading, List<LoadingEnableVisible>>) {
        loading = pair.first
        closeDialogLoading()
        if (this.loading.ref.isEmpty()) {
            binding.pbLoading.isVisible = false
            return
        }
        showLoading()
    }

    //Функция добавления/удаления сканированного штрих-кода в объект BarcodeAdapter
    private fun displayScanResult(data: String) {
        with(binding) {
            scannedData = data.replace(Regex("[\\s\\n]"), "")

            if (::scannedData.isInitialized && scannedData.length >= 17) {
                scannedData = scannedData.takeLast(17)

                if (Utils.debugMode)
                    scannedData = "90000153429210421"

                if (chbDelete.isChecked) {
                    if (radioAcceptance.isChecked) {
                        val adapter =
                            if (tabLayout.selectedTabPosition == 0) barcodeFrontAdapter else barcodeBackAdapter
                        val documentUid =
                            adapter.find(scannedData)?.acceptanceUid
                        documentUid?.let { adapter.removeBarcodeByUid(it) }
                    } else {
                        val adapter =
                            if (tabLayout.selectedTabPosition == 0) barcodeFrontAdapter else barcodeBackAdapter
                        adapter.removeBarcodeByBarcode(scannedData)
                    }

                } else {
                    val adapter =
                        if (binding.tabLayout.selectedTabPosition == 0) barcodeFrontAdapter else barcodeBackAdapter
                    if (adapter.getList()
                            .find { it.barcode == scannedData } == null || radioAcceptance.isChecked
                    ) {
                        viewModel.getBarcodeList(scannedData, user.warehouse)
                        showDialogLoading()
                    } else {
                        toast(requireContext().getString(R.string.err_dublicate_barcode))
                    }
                }
            }
        }
    }

    private fun loadingRouteDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading_warehouse, null)
        initRouteDialog(dialogView)

        dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setView(dialogView)
            .create()

        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.TOP)
        dialog.show()
    }

    private fun onRemoveBarcodeClick(barcode: LoadingBarcode) {
        if (binding.tabLayout.selectedTabPosition == 0) {
            barcodeFrontList.remove(barcode)
        } else {
            barcodeBackList.remove(barcode)
        }
        updateWeightAndVolume()
    }

    private fun clearAllFocus() {
        with(binding) {
            tvAuto.clearFocus()
            if (::senderWarehouse.isInitialized) {
                senderWarehouse.clearFocus()
                getterWarehouse.clearFocus()
            }
        }
    }

    private fun closeActivity() {
        activity?.onBackPressed()
    }

    //При выходе из формы отсоединяем приёмник broadcastReceiver и очищаем список сканированных штрих-кодов
    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(broadcastReceiver)
        barcodeFrontAdapter.clearBarcodeData()
        barcodeBackAdapter.clearBarcodeData()
    }

    companion object {
        const val KEY_LOADING_NUMBER = "loading_number"
    }
}