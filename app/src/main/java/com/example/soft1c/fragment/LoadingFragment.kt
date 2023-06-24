package com.example.soft1c.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soft1c.R
import com.example.soft1c.adapter.BarcodeAdapter
import com.example.soft1c.databinding.FragmentLoadingBinding
import com.example.soft1c.repository.model.Loading
import com.example.soft1c.repository.model.LoadingEnableVisible
import com.example.soft1c.repository.model.LoadingModel
import com.example.soft1c.utils.Utils
import com.example.soft1c.utils.Utils.cars
import com.example.soft1c.utils.Utils.warehouse
import com.example.soft1c.viewmodel.LoadingViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*


class LoadingFragment : BaseFragment<FragmentLoadingBinding>(FragmentLoadingBinding::inflate) {

    // Объект класса позволяющего связывать штрих-коды с элементами интерфейса
    private lateinit var barcodeAdapter: BarcodeAdapter
    private val viewModel: LoadingViewModel by viewModels()
    private var barcodeList: ArrayList<String> = arrayListOf()
    private lateinit var loading: Loading
    private lateinit var senderWarehouse: AutoCompleteTextView
    private lateinit var getterWarehouse: AutoCompleteTextView
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Получаем объект BarcodeAdapter
        barcodeAdapter = BarcodeAdapter()
        //Устанавливаем связь между только что созданным объектом и элементом интерфейса
        binding.rvBarcode.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = barcodeAdapter
        }

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
                // Received a barcode scan
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
                if (binding.btnClose.isEnabled && binding.btnClose.isVisible) {
                    binding.btnClose.requestFocus()
                }
            }
            //Обработчик нажатия клавиш
            tvAuto.setOnKeyListener(::autoCompleteOnKeyListener)
            btnInfo.setOnClickListener {
                loadingInfoDialog()
            }
            etxtSave.setOnClickListener {
                createUpdateLoading()
            }
            btnClose.setOnClickListener { closeActivity() }

        }
    }

    private fun initDialog(dialogView: View) {
        val txtNumber = dialogView.findViewById<TextInputEditText>(R.id.txt_number)
        val layoutNumber = dialogView.findViewById<TextInputLayout>(R.id.elayout_txt_number)
        val txtDate = dialogView.findViewById<TextInputEditText>(R.id.txt_date)
        val layoutDate = dialogView.findViewById<TextInputLayout>(R.id.elayout_txt_date)
        senderWarehouse = dialogView.findViewById(R.id.etxt_wareH_sender)
        getterWarehouse = dialogView.findViewById(R.id.etxt_wareH_getter)

        txtNumber.setText(loading.number)
        if (loading.date.isNotEmpty()) {
            txtDate.setText(inputDateFormat.parse(loading.date)
                ?.let { outputDateFormat.format(it) })
        } else {
            layoutNumber.visibility = View.GONE
            layoutDate.visibility = View.GONE
        }

        if (loading.senderWarehUid.isNotEmpty()) {
            senderWarehouse.setText(loading.senderWareh)
        }
        if (loading.getterWarehUid.isNotEmpty()) {
            getterWarehouse.setText(loading.getterWareh)
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
        loading.barcodes = barcodeList
        if (loading.car.isEmpty()) {
            binding.tvAuto.error = resources.getString(R.string.text_field_is_empyt)
        } else {
            documentCreate = !documentCreate
            viewModel.createUpdateLoading(loading)
        }
    }

    private fun createUpdateLoading(pair: Pair<Loading, String>) {
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
        viewModel.createUpdateLiveDat.observe(viewLifecycleOwner, ::createUpdateLoading)
        viewModel.barcodeListLiveData.observe(viewLifecycleOwner, ::getBarcodeList)
    }

    private fun getBarcodeList(barcodes: List<String>?) {
        if (barcodes != null) {
            for (barcode in 0 until barcodes.count()) {
                barcodeList.add(barcodes[barcode])
                barcodeAdapter.addBarcodeData(barcodes[barcode])
            }
        }
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
                        if (btnClose.isEnabled && btnClose.isVisible) {
                            btnClose.requestFocus()
                            return true
                        }
                        return false
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
                    loading.carUid = element.ref
                    loading.car = element.number
                    view.setText(element.number)
                }
                Utils.ObjectModelType.WAREHOUSE -> {
                    element as LoadingModel.Warehouse
                    if (view.id == R.id.etxt_wareH_sender) {
                        loading.senderWarehUid = element.ref
                        loading.senderWareh = element.name
                    }else if(view.id == R.id.etxt_wareH_getter){
                        loading.getterWarehUid = element.ref
                        loading.getterWareh = element.name
                    }
                    view.setText(element.name)
                }
            }
        } else {
            when (model) {
                Utils.ObjectModelType.CAR -> {
                    view.text.clear()
                    loading.car = ""
                    loading.carUid = ""
                }
                Utils.ObjectModelType.WAREHOUSE -> {
                    view.text.clear()
                    loading.senderWareh = ""
                    loading.senderWarehUid = ""
                }
            }

        }
    }

    private fun showLoading() {
        with(binding) {
            tvAuto.setText(loading.car)
            for (i in 0 until loading.barcodes.count()) {
                barcodeAdapter.addBarcodeData(loading.barcodes[i])
                barcodeList.add(loading.barcodes[i])
            }
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
        val decodedData = initiatingIntent.getStringExtra("com.symbol.datawedge.data_string")

        if (decodedData != null) {
            viewModel.getBarcodeList(decodedData)
        }
        binding.tvAuto.isEnabled = true
    }

    private fun loadingInfoDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading_warehouse, null)
        initDialog(dialogView)

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setView(dialogView)
            .create()

        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.TOP)
        dialog.show()
    }

    private fun closeActivity() {
        activity?.onBackPressed()
    }

    //При выходе из формы отсоединяем приёмник broadcastReceiver и очищаем список сканированных штрих-кодов
    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(broadcastReceiver)
        barcodeAdapter.clearBarcodeData()
    }

    companion object {
        const val KEY_LOADING_NUMBER = "loading_number"
    }
}