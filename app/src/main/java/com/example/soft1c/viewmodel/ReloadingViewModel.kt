package com.example.soft1c.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.soft1c.R
import com.example.soft1c.network.Network
import com.example.soft1c.repository.ReloadingRepository
import com.example.soft1c.repository.model.ExpandableLoadingList
import com.example.soft1c.repository.model.Loading
import com.example.soft1c.repository.model.Loading.Companion.CAR_KEY
import com.example.soft1c.repository.model.Loading.Companion.DATE_KEY
import com.example.soft1c.repository.model.Loading.Companion.GUID_KEY
import com.example.soft1c.repository.model.Loading.Companion.ITEMS_BACK_KEY
import com.example.soft1c.repository.model.Loading.Companion.ITEMS_FRONT_KEY
import com.example.soft1c.repository.model.Loading.Companion.REF_KEY
import com.example.soft1c.repository.model.Loading.Companion.WAREHOUSE_BEGIN_KEY
import com.example.soft1c.repository.model.Loading.Companion.WAREHOUSE_END_KEY
import com.example.soft1c.repository.model.LoadingBarcode.Companion.BARCODE_KEY
import com.example.soft1c.repository.model.LoadingEnableVisible
import com.example.soft1c.utils.SingleLiveEvent
import com.example.soft1c.utils.Utils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.util.UUID

class ReloadingViewModel(application: Application): AndroidViewModel(application) {
    private val repository = ReloadingRepository()

    private val exceptionScope = CoroutineExceptionHandler { coroutineContext, throwable ->
        if (throwable.stackTraceToString().startsWith("java.net.SocketTimeoutException")) {
            toastResIdMutableData.postValue(R.string.socket_timeout_exception)
        } else if (throwable.stackTraceToString().startsWith("java.net.ConnectException")) {
            toastResIdMutableData.postValue(R.string.no_connection_exception)
        } else {
            toastMutableData.postValue("Error on $coroutineContext , error message ${throwable.localizedMessage}")
        }
        Network.refreshConnection(Utils.clientTimeout)
    }

    private val toastMutableData = SingleLiveEvent<String>()
    private val toastResIdMutableData = SingleLiveEvent<Int>()
    private val createUpdateMutableData = SingleLiveEvent<String>()
    private val reloadingListMutableData = MutableLiveData<List<Loading>>()
    private val barcodeListMutableData = MutableLiveData<List<ExpandableLoadingList>>()
    private val reloadingMutableData = SingleLiveEvent<Pair<Loading, List<LoadingEnableVisible>>>()

    val toastLiveDat: LiveData<String>
        get() = toastMutableData
    val toastResIdLiveData: LiveData<Int>
        get() = toastResIdMutableData
    val createUpdateLiveDat: LiveData<String>
        get() = createUpdateMutableData
    val barcodeListLiveData: LiveData<List<ExpandableLoadingList>>
        get() = barcodeListMutableData
    val reloadingLiveData: LiveData<Pair<Loading, List<LoadingEnableVisible>>>
        get() = reloadingMutableData
    val reloadingListLiveData: LiveData<List<Loading>>
        get() = reloadingListMutableData

    fun createUpdateLoading(loading: Loading) {
        val jsonBody = getJsonBody(loading)

        viewModelScope.launch((exceptionScope + Dispatchers.IO)) {
            createUpdateMutableData.postValue(repository.createUpdateLoadingApi(jsonBody))
        }
    }

    fun getReloadingList() {
        viewModelScope.launch((exceptionScope+ Dispatchers.IO)){
            reloadingListMutableData.postValue(repository.getReloadingListApi())
        }
    }

    fun getReloading(number: String){
        viewModelScope.launch((exceptionScope + Dispatchers.IO)){
            reloadingMutableData.postValue(repository.getReloadingApi(number))
        }
    }

    fun getBarcodeList(code: String, warehouse: String){
        viewModelScope.launch((exceptionScope+ Dispatchers.IO)){
            barcodeListMutableData.postValue(repository.getBarcodeListApi(code, warehouse))
        }
    }

    fun getJsonBody(loading: Loading): JSONObject {
        val jsonObject = JSONObject()
        if (loading.ref.isNotEmpty())
            jsonObject.put(REF_KEY, loading.ref)
        else
            jsonObject.put(REF_KEY, UUID.randomUUID().toString())
        if (loading.guid.isNotEmpty())
            jsonObject.put(GUID_KEY, loading.guid)
        else
            jsonObject.put(GUID_KEY, null)
        if (loading.date.isNotEmpty()) {
            jsonObject.put(DATE_KEY, loading.date)
        } else {
            jsonObject.put(DATE_KEY, LocalDateTime.now().format(Utils.dateFormat))
        }
        jsonObject.put(CAR_KEY, loading.car.ref)
        jsonObject.put(WAREHOUSE_BEGIN_KEY, loading.senderWarehouse.ref)
        jsonObject.put(WAREHOUSE_END_KEY, loading.getterWarehouse.ref)
        // Создаем массив товаров и заполняем его штрих-кодами
        val frontBarcodeArray = JSONArray()
        for (barcode in loading.barcodesFront) {
            val barcodeObject = JSONObject()
            barcodeObject.put(BARCODE_KEY, barcode.loadingChild.barcode)
            frontBarcodeArray.put(barcodeObject)
        }
        jsonObject.put(ITEMS_FRONT_KEY, frontBarcodeArray)
        val backBarcodeArray = JSONArray()
        for (barcode in loading.barcodesBack) {
            val barcodeObject = JSONObject()
            barcodeObject.put(BARCODE_KEY, barcode.loadingChild.barcode)
            backBarcodeArray.put(barcodeObject)
        }
        // Добавляем массив товаров в объект JSON
        jsonObject.put(ITEMS_BACK_KEY, backBarcodeArray)

        return jsonObject
    }
}