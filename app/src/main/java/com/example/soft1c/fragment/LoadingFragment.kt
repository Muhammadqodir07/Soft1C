package com.example.soft1c.fragment

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
import com.example.soft1c.viewmodel.LoadingViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.absoluteValue


class LoadingFragment : BaseFragment<FragmentLoadingBinding>(FragmentLoadingBinding::inflate) {

    // Объект класса позволяющего связывать штрих-коды с элементами интерфейса
    private var barcodeFrontAdapter: BarcodeAdapter = BarcodeAdapter{
        onRemoveBarcodeClick(it)
    }
    private var barcodeBackAdapter: BarcodeAdapter = BarcodeAdapter{
        onRemoveBarcodeClick(it)
    }
    private var barcodeFrontList: ArrayList<LoadingBarcode> = arrayListOf()
    private var barcodeBackList: ArrayList<LoadingBarcode> = arrayListOf()
    private var weight = 0.0
    private var volume = 0.0

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
        val filter = IntentFilter()
        filter.addCategory(Intent.CATEGORY_DEFAULT)
        filter.addAction("com.Soft1C.ACTION")
        context?.registerReceiver(broadcastReceiver, filter)
        //Устанавливание слушателей для поля НомераМашин
        initUI()
        observeViewModel()
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
                if (tvRecipient.isEnabled && tvRecipient.isVisible) {
                    tvRecipient.requestFocus()
                }
            }
            //Обработчик нажатия клавиш
            tvAuto.setOnKeyListener(::autoCompleteOnKeyListener)

            tvRecipient.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    warehouse.map { (it as LoadingModel.Warehouse).name }
                )
            )
            tvRecipient.setOnItemClickListener{
                    parent, view, position, _ ->
                val selectedModel = parent.getItemAtPosition(position) as String
                //Выполняем функцию НайтиИЗаполнитьВыбранныйЭлемент куда передаем позицию выбранного элемента
                findAndFillAnySelectedModel(
                    warehouse,
                    Utils.ObjectModelType.WAREHOUSE,
                    tvRecipient,
                    selectedModel
                )
                view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                view.clearFocus()
            }
            tvRecipient.setOnKeyListener(::autoCompleteOnKeyListener)
            btnShowHide.setOnClickListener{
                if (linearInformation.visibility == View.VISIBLE){
                    constraintLayout.transitionToEnd()
                }else{
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

            radioFront.setOnClickListener {
                constraintRv.transitionToStart()
            }

            radioBack.setOnClickListener {
                constraintRv.transitionToEnd()
            }

            etxtRoute.setOnClickListener {
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
            //Задать текст поле Mаршрут TODO
            dialog.dismiss()
        }
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        if (loading.senderWarehouseUid.isNotEmpty()) {
            senderWarehouse.setText(loading.senderWarehouse)
        }
        if (loading.getterWarehouseUid.isNotEmpty()) {
            getterWarehouse.setText(loading.getterWarehouse)
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
        } else {
            documentCreate = !documentCreate
            viewModel.createUpdateLoading(loading)
        }
    }

    private fun getCreateUpdateLoadingInfo(pair: Pair<Loading, String>) {
        if (pair.second.isNotEmpty()) {
            toast(pair.second)
            return
        }
        closeActivity()
    }


    private fun observeViewModel() {
        viewModel.loadingLiveData.observe(viewLifecycleOwner, ::showDetails)
        viewModel.toastLiveDat.observe(viewLifecycleOwner) {
            documentCreate = false
            toast(it)
        }
        viewModel.createUpdateLiveDat.observe(viewLifecycleOwner, ::getCreateUpdateLoadingInfo)
        viewModel.barcodeListLiveData.observe(viewLifecycleOwner, ::fillBarcodeList)
    }

    private fun fillBarcodeList(barcodes: List<LoadingBarcode>?) {
        closeDialogLoading()
        barcodes?.forEach { barcode ->
            if (binding.radioFront.isChecked) {
                barcodeFrontList.add(barcode)
                barcodeFrontAdapter.addBarcodeData(barcode)
            }else{
                barcodeBackList.add(barcode)
                barcodeBackAdapter.addBarcodeData(barcode)
            }
            weight+=barcode.weight
            volume+=barcode.volume
        }
        binding.txtWeightValue.text= String.format("%.2f", weight)
        binding.txtVolumeValue.text = String.format("%.6f", volume)
    }

    //Функция обработчика клавиш
    private fun autoCompleteOnKeyListener(view: View, key: Int, keyEvent: KeyEvent): Boolean {
        binding.tvAuto.error = null
        //Если нажата клавиша Enter
        if (key == 66 && keyEvent.action == KeyEvent.ACTION_UP) {
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
                        //Если кнопка сохранения видна и доступна то перевести фокус на него
                        if (tvRecipient.isEnabled && tvRecipient.isVisible) {
                            tvRecipient.requestFocus()
                            return true
                        }
                        return false
                    }
                    tvRecipient -> {
                        findAndFillAnyModel(
                            warehouse,
                            Utils.ObjectModelType.WAREHOUSE,
                            view
                        )
                        tvRecipient.clearFocus()
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
                        return true
                    }
                    else -> return false
                }
            }
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
                    when(view.id){
                        R.id.etxt_wareH_sender -> {
                            loading.senderWarehouseUid = element.ref
                            loading.senderWarehouse = element.name
                        }
                        R.id.etxt_wareH_getter -> {
                            loading.getterWarehouseUid = element.ref
                            loading.getterWarehouse = element.name
                        }
                        R.id.tv_recipient ->{
                            loading.recipient = element
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
                    when(view.id){
                        R.id.etxt_wareH_sender -> {
                            loading.senderWarehouseUid = ""
                            loading.senderWarehouse = ""
                        }
                        R.id.etxt_wareH_getter -> {
                            loading.getterWarehouseUid = ""
                            loading.getterWarehouse = ""
                        }
                        R.id.tv_recipient ->{
                            loading.recipient = LoadingModel.Warehouse()
                        }
                    }
                }
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun showLoading() {
        with(binding) {
            txtNumber.setText(loading.number)
            txtDate.setText(loading.date)
            tvAuto.setText(loading.car.number)
            tvRecipient.setText(loading.recipient.name)
            loading.barcodesFront.forEach {
                barcodeFrontList.add(it)
                barcodeFrontAdapter.addBarcodeData(it)
                weight+=it.weight
                volume+=it.volume
            }
            loading.barcodesBack.forEach {
                barcodeBackList.add(it)
                barcodeBackAdapter.addBarcodeData(it)
                weight+=it.weight
                volume+=it.volume
            }
            txtWeightValue.text= String.format("%.2f", weight)
            txtVolumeValue.text = String.format("%.6f", volume)
        }
    }

    private fun showDetails(pair: Pair<Loading, List<LoadingEnableVisible>>) {
        loading = pair.first
        if (this.loading.ref.isEmpty()) {
            binding.pbLoading.isVisible = false
            return
        }
        showLoading()
    }

    //Функция добавление сканированного штрих-кода в объект BarcodeAdapter
    private fun displayScanResult(initiatingIntent: Intent) {
        binding.tvAuto.isEnabled = false
        var decodedData = initiatingIntent.getStringExtra("com.symbol.datawedge.data_string")
        decodedData = "90000153429210421"

        if (decodedData != null && decodedData.length>=17) {
            viewModel.getBarcodeList(decodedData.takeLast(17), user.warehouse)
            showDialogLoading()
        }
        binding.tvAuto.isEnabled = true
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

    private fun onRemoveBarcodeClick(barcode: LoadingBarcode){
        weight-=barcode.weight
        volume-=barcode.volume
        binding.txtWeightValue.text= String.format("%.2f", weight.absoluteValue)
        binding.txtVolumeValue.text = String.format("%.6f", volume.absoluteValue)
    }

    private fun closeActivity() {
        activity?.onBackPressed()
    }

    //При выходе из формы отсоединяем приёмник broadcastReceiver и очищаем список сканированных штрих-кодов
    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(broadcastReceiver)
        barcodeFrontAdapter.clearBarcodeData()
    }

    companion object {
        const val KEY_LOADING_NUMBER = "loading_number"
    }
}